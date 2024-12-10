import React, { useEffect, useState, useRef } from 'react';
import { Stage, Layer, Rect, Text, Image as KonvaImage } from 'react-konva';
import './Tagging.css';
import ErrorBox from "./Error";
import apiCall from './services/api';

function TaggingPage() {
    const [imageSrc, setImageSrc] = useState('');
    const [error, setError] = useState(null);
    const [currentImageId, setCurrentImageId] = useState('');
    const [categories, setCategories] = useState({});
    const [animalColors, setAnimalColors] = useState({});
    const [boxes, setBoxes] = useState([]);
    const [originalBoxes, setOriginalBoxes] = useState([]);
    const [drawing, setDrawing] = useState(false);
    const [newBox, setNewBox] = useState(null);
    const [imageSize, setImageSize] = useState({ width: 0, height: 0 });
    const [canvasScale, setCanvasScale] = useState({ x: 1, y: 1 });
    const [history, setHistory] = useState([[]]);
    const [redoHistory, setRedoHistory] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [status, setStatus] = useState('');
    const [isEditing, setIsEditing] = useState(false);
    const [fetchedCoordinates, setFetchedCoordinates] = useState([]);
    const [fetchError, setFetchError] = useState('');

    const [showAnimalPopup, setShowAnimalPopup] = useState(false);
    const [popupPosition, setPopupPosition] = useState({ x: 0, y: 0 });
    const [pendingBoxIndex, setPendingBoxIndex] = useState(null);
    const [popupSelectedAnimal, setPopupSelectedAnimal] = useState('');

    const [hoveredBoxIndex, setHoveredBoxIndex] = useState(null);

    const MIN_BOX_SIZE = 20;

    const stageRef = useRef(null);
    const containerRef = useRef(null);
    const usedColors = useRef(new Set());

    const generateUniqueBrightColor = () => {
        let color;
        do {
            const r = Math.floor(Math.random() * 156 + 100);
            const g = Math.floor(Math.random() * 156 + 100);
            const b = Math.floor(Math.random() * 156 + 100);
            color = `rgb(${r}, ${g}, ${b})`;
        } while (usedColors.current.has(color));
        usedColors.current.add(color);
        return color;
    };

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const result = await apiCall('/wild-tag/categories', 'GET', null, {
                    Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                });

                const categoriesData = result.entries || {};
                setCategories(categoriesData);

                const colors = {};
                Object.values(categoriesData).forEach((animal) => {
                    colors[animal] = generateUniqueBrightColor();
                });

                setAnimalColors(colors);
            } catch (err) {
                setError("Failed to load categories. Please try again later.");
                console.error('Error fetching categories:', err);
            }
        };

        fetchCategories();
    }, []);

    const fetchNextTask = async () => {
        setIsLoading(true);
        setFetchError('');
        try {
            const imageResult = await apiCall('/wild-tag/images/next_task', 'GET', null, {
                Authorization: `Bearer ${localStorage.getItem('authToken')}`,
            });

            if (imageResult && imageResult.id) {
                setCurrentImageId(imageResult.id);
                setStatus(imageResult.status || '');
                setFetchedCoordinates(imageResult.coordinates || []);

                const imageResponse = await apiCall(
                    `/wild-tag/images/${imageResult.id}`,
                    'GET',
                    null,
                    {
                        Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                    },
                    'blob'
                );

                const blob = await imageResponse.blob();
                const url = URL.createObjectURL(blob);
                setImageSrc(url);

                const img = new window.Image();
                img.onload = () => {
                    setImageSize({ width: img.width, height: img.height });
                    setIsLoading(false); // Image loaded
                };
                img.onerror = () => {
                    setError("Failed to load image. Please try again later.");
                    setIsLoading(false);
                };
                img.src = url;
            } else {
                console.error('Failed to fetch image content');
                setFetchError('Trying to fetch image, will retry..');
                retryFetching();
            }
        } catch (error) {
            console.error('Error fetching image:', error);
            setError("Failed to load image. Please try again later.");
            retryFetching();
            setIsLoading(false);
        }
    };

    const retryFetching = () => {
        setIsLoading(true);
        setTimeout(() => {
            fetchNextTask();
        }, 60000);
    };

    useEffect(() => {
        fetchNextTask();
    }, []);

    useEffect(() => {
        const updateDimensions = () => {
            if (containerRef.current && stageRef.current && imageSize.width && imageSize.height) {
                const containerWidth = containerRef.current.offsetWidth;
                const containerHeight = containerRef.current.offsetHeight;
                const scale = Math.min(
                    containerWidth / imageSize.width,
                    containerHeight / imageSize.height
                );
                setCanvasScale({ x: scale, y: scale });
            }
        };
        updateDimensions();
        window.addEventListener('resize', updateDimensions);
        return () => window.removeEventListener('resize', updateDimensions);
    }, [imageSize]);

    useEffect(() => {
        if (
            status === 'TAGGED' &&
            fetchedCoordinates.length > 0 &&
            imageSize.width > 0 &&
            Object.keys(categories).length > 0 &&
            Object.keys(animalColors).length > 0
        ) {
            const mappedBoxes = fetchedCoordinates.map((coord) => {
                const animal = categories[coord.animalId];
                return {
                    x: coord.xCenter * imageSize.width - (coord.width * imageSize.width) / 2,
                    y: coord.yCenter * imageSize.height - (coord.height * imageSize.height) / 2,
                    width: coord.width * imageSize.width,
                    height: coord.height * imageSize.height,
                    animal,
                    color: animalColors[animal],
                };
            });
            setBoxes(mappedBoxes);
            setOriginalBoxes(mappedBoxes);
            setHistory([mappedBoxes]);
            setRedoHistory([]);
        } else if (status === 'PENDING' || status === '') {
            setBoxes([]);
            setOriginalBoxes([]);
            setHistory([[]]);
            setRedoHistory([]);
        }
    }, [status, fetchedCoordinates, imageSize, categories, animalColors]);

    const canDrawBoxes = () => {
        if (status === 'PENDING') return true;
        if (status === 'TAGGED' && isEditing) return true;
        return false;
    };

    let lastMouseUpPos = {x:0, y:0};

    const handleMouseDown = (e) => {
        if (!canDrawBoxes()) return;

        if (showAnimalPopup) {
            if (pendingBoxIndex !== null) {
                const newBoxes = [...boxes];
                newBoxes.splice(pendingBoxIndex, 1);
                setBoxes(newBoxes);
            }
            setShowAnimalPopup(false);
            setPendingBoxIndex(null);
            setPopupSelectedAnimal('');
        }

        const stage = e.target.getStage();
        const pos = stage.getPointerPosition();

        setDrawing(true);
        setNewBox({
            x: clampValue(pos.x),
            y: clampValue(pos.y),
            width: 0,
            height: 0,
            animal: null,
            color: null,
        });
    };

    const clampValue = (val) => Math.max(0, Math.min(val / canvasScale.x, imageSize.width));

    const handleMouseMove = (e) => {
        if (!drawing) {
            const stage = e.target.getStage();
            const pointer = stage ? stage.getPointerPosition() : null;
            if (!pointer) {
                setHoveredBoxIndex(null);
                return;
            }

            const pointerX = pointer.x / canvasScale.x;
            const pointerY = pointer.y / canvasScale.y;

            let hovered = null;
            for (let i = 0; i < boxes.length; i++) {
                const b = boxes[i];

                const insideBox = pointerX >= b.x && pointerX <= b.x + b.width &&
                    pointerY >= b.y && pointerY <= b.y + b.height;

                const insideButton = pointerX >= b.x + b.width - 65 && pointerX <= b.x + b.width - 5 &&
                    pointerY >= b.y - 65 && pointerY <= b.y - 5;

                if (insideBox || insideButton) {
                    hovered = i;
                    break;
                }
            }
            setHoveredBoxIndex(hovered);
            return;
        }

        const stage = e.target.getStage();
        const pos = stage.getPointerPosition();
        if (pos && newBox) {
            const clampedX = clampValue(pos.x);
            const clampedY = clampValue(pos.y);
            setNewBox((prev) => {
                if (!prev) return null;
                return {
                    ...prev,
                    width: clampedX - prev.x,
                    height: clampedY - prev.y,
                };
            });
        }
    };

    const handleMouseUp = (e) => {
        if (!drawing) return;
        setDrawing(false);

        const stage = e.target.getStage();
        const pos = stage.getPointerPosition();
        lastMouseUpPos = pos;

        if (newBox) {
            let { x, y, width, height } = newBox;
            if (width < 0) {
                x += width;
                width = Math.abs(width);
            }
            if (height < 0) {
                y += height;
                height = Math.abs(height);
            }

            if (width < MIN_BOX_SIZE || height < MIN_BOX_SIZE) {
                setNewBox(null);
                return;
            }

            const normalizedBox = { x, y, width, height, animal: null, color: null };
            const updatedBoxes = [...boxes, normalizedBox];
            setBoxes(updatedBoxes);
            setHistory((prev) => [...prev, updatedBoxes]);
            setRedoHistory([]);

            const boxIndex = updatedBoxes.length - 1;
            const box = updatedBoxes[boxIndex];
            showAnimalSelectionPopupNearBox(boxIndex, box);
        }
        setNewBox(null);
    };

    const showAnimalSelectionPopupNearBox = (index, box) => {
        if (!stageRef.current || !containerRef.current) return;

        const popupWidth = 200;
        const popupHeight = 200;
        const containerRect = containerRef.current.getBoundingClientRect();

        // Calculate the bottom-right corner of the box
        const boxEndX = box.x + box.width;
        const boxEndY = box.y + box.height;

        // Convert box coordinates to scaled positions
        const scaledBoxEndX = boxEndX * canvasScale.x;
        const scaledBoxEndY = boxEndY * canvasScale.y;

        // Calculate position relative to the container
        let px = scaledBoxEndX + 10; // 10px offset
        let py = scaledBoxEndY + 10; // 10px offset

        // Boundary checks to keep the popup within the container
        if (px + popupWidth > containerRect.width) {
            px = scaledBoxEndX - popupWidth - 10; // 10px offset from the box
        }
        if (py + popupHeight > containerRect.height) {
            py = scaledBoxEndY - popupHeight - 10; // 10px offset from the box
        }
        if (px < 0) px = 10;
        if (py < 0) py = 10;

        setPopupPosition({ x: px, y: py });
        setPendingBoxIndex(index);
        setShowAnimalPopup(true);
    };

    const handleMouseLeave = () => {
        if (drawing) {
            setDrawing(false);
            setNewBox(null);
        }
        setHoveredBoxIndex(null);
    };

    const handleClear = () => {
        setBoxes([]);
        setHistory([[]]);
        setRedoHistory([]);
    };

    const boxesAreDifferent = (a, b) => {
        if (a.length !== b.length) return true;
        for (let i = 0; i < a.length; i++) {
            const boxA = a[i];
            const boxB = b[i];
            if (
                boxA.animal !== boxB.animal ||
                boxA.x !== boxB.x ||
                boxA.y !== boxB.y ||
                boxA.width !== boxB.width ||
                boxA.height !== boxB.height
            ) {
                return true;
            }
        }
        return false;
    };

    const handleSubmit = async () => {
        if (!boxesAreDifferent(boxes, originalBoxes)) {
            alert("No changes detected. Please edit/draw boxes before submitting.");
            return;
        }

        if (boxes.length === 0) {
            alert("No boxes drawn. Please tag the image before submitting.");
            return;
        }

        const yoloData = boxes.map((box) => {
            const centerX = (box.x + box.width / 2) / imageSize.width;
            const centerY = (box.y + box.height / 2) / imageSize.height;
            const boxWidth = Math.abs(box.width) / imageSize.width;
            const boxHeight = Math.abs(box.height) / imageSize.height;

            const animalIdKey = Object.keys(categories).find(
                (key) => categories[key] === box.animal
            );

            return {
                animalId: animalIdKey,
                xCenter: centerX.toFixed(6),
                yCenter: centerY.toFixed(6),
                width: boxWidth.toFixed(6),
                height: boxHeight.toFixed(6),
            };
        });

        try {
            await apiCall(`/wild-tag/images/tag`, 'PUT', { id: currentImageId, coordinates: yoloData }, {
                Authorization: `Bearer ${localStorage.getItem('authToken')}`,
            });

            setOriginalBoxes(boxes);
            fetchNextTask();
        } catch (error) {
            console.error("Error submitting tagging data:", error);
            alert("Failed to submit tagging data. Please try again.");
        }
    };

    const validateImage = async () => {
        try {
            await apiCall(`/wild-tag/images/${currentImageId}/validate`, 'PUT', null, {
                Authorization: `Bearer ${localStorage.getItem('authToken')}`,
            });
            fetchNextTask();
        } catch (error) {
            console.error('Error validating image:', error);
            alert("Failed to validate image. Please try again.");
        }
    };

    const toggleEditMode = () => {
        setIsEditing((prev) => !prev);
    };

    const handleCancel = () => {
        setBoxes(originalBoxes);
        setHistory([originalBoxes]);
        setRedoHistory([]);
        setIsEditing(false);
    };

    const handleAnimalSelection = (animal) => {
        if (pendingBoxIndex !== null) {
            const newBoxes = [...boxes];
            const box = newBoxes[pendingBoxIndex];
            box.animal = animal;
            box.color = animalColors[animal];
            newBoxes[pendingBoxIndex] = box;
            setBoxes(newBoxes);
        }

        setShowAnimalPopup(false);
        setPendingBoxIndex(null);
        setPopupSelectedAnimal('');
    };

    const handleRemoveBox = (index) => {
        const newBoxes = [...boxes];
        newBoxes.splice(index, 1);
        setBoxes(newBoxes);
        setHistory((prev) => [...prev, newBoxes]);
        setRedoHistory([]);
        setHoveredBoxIndex(null);
    };

    if (error) {
        return <ErrorBox message={error} onClose={() => setError('')} />;
    }

    return (
        <div className="image-tag-page">
            {isLoading && (
                <div className="loading-overlay">
                    <div className="loader"></div>
                    {fetchError && <div style={{ marginTop: '16px', color: '#f00', fontWeight: 'bold' }}>{fetchError}</div>}
                </div>
            )}
            <div className="image-tag-content">
                {showAnimalPopup && (
                    <div
                        className="animal-popup"
                        style={{
                            top: popupPosition.y,
                            left: popupPosition.x,
                            width: '200px',
                            height: '200px'
                        }}
                    >
                        <h4>Select Animal</h4>
                        <div className="animal-list">
                            {Object.values(categories).map((animal) => (
                                <div
                                    key={animal}
                                    className={`animal-list-item ${popupSelectedAnimal === animal ? 'selected' : ''}`}
                                    onClick={() => handleAnimalSelection(animal)}
                                >
                                    <span
                                        className="color-indicator"
                                        style={{
                                            backgroundColor: animalColors[animal],
                                            marginRight: '8px'
                                        }}
                                    ></span>
                                    {animal}
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                <div className="image-tag-div" ref={containerRef}>
                    {imageSrc && (
                        <Stage
                            width={imageSize.width * canvasScale.x}
                            height={imageSize.height * canvasScale.y}
                            scale={canvasScale}
                            onMouseDown={handleMouseDown}
                            onMouseMove={handleMouseMove}
                            onMouseUp={handleMouseUp}
                            onMouseLeave={handleMouseLeave}
                            ref={stageRef}
                        >
                            <Layer listening={true}>
                                <KonvaImage
                                    image={(() => {
                                        const img = new window.Image();
                                        img.src = imageSrc;
                                        return img;
                                    })()}
                                    width={imageSize.width}
                                    height={imageSize.height}
                                />
                                {boxes.map((box, i) => (
                                    <React.Fragment key={i}>
                                        <Rect
                                            x={box.x}
                                            y={box.y}
                                            width={box.width}
                                            height={box.height}
                                            stroke={box.color || 'yellow'}
                                            strokeWidth={15}
                                            listening={true}
                                        />
                                        {box.animal && (
                                            <Text
                                                x={box.x + box.width / 2}
                                                y={box.y - 80}
                                                text={box.animal}
                                                fontSize={50}
                                                fill={box.color || 'yellow'}
                                                align="center"
                                                listening={true}
                                            />
                                        )}
                                    </React.Fragment>
                                ))}
                                {newBox && (
                                    <Rect
                                        x={newBox.x}
                                        y={newBox.y}
                                        width={newBox.width}
                                        height={newBox.height}
                                        stroke="yellow"
                                        strokeWidth={15}
                                        listening={true}
                                    />
                                )}
                            </Layer>
                            <Layer listening={true}>
                                {hoveredBoxIndex !== null && boxes[hoveredBoxIndex] && (
                                    <>
                                        <Rect
                                            x={boxes[hoveredBoxIndex].x + boxes[hoveredBoxIndex].width - 65}
                                            y={boxes[hoveredBoxIndex].y - 65}
                                            width={60}
                                            height={60}
                                            fill="#2980b9"
                                            stroke="#fff"
                                            strokeWidth={2}
                                            cornerRadius={30}
                                            shadowColor="black"
                                            shadowBlur={4}
                                            shadowOffset={{ x: 1, y: 1 }}
                                            shadowOpacity={0.3}
                                            listening={true}
                                            onClick={() => handleRemoveBox(hoveredBoxIndex)}
                                        />
                                        <Text
                                            x={boxes[hoveredBoxIndex].x + boxes[hoveredBoxIndex].width - 65}
                                            y={boxes[hoveredBoxIndex].y - 65}
                                            width={60}
                                            height={60}
                                            text="↺"
                                            fontSize={40}
                                            fill="#fff"
                                            align="center"
                                            verticalAlign="middle"
                                            fontStyle="bold"
                                            listening={true}
                                            onClick={() => handleRemoveBox(hoveredBoxIndex)}
                                        />
                                    </>
                                )}
                            </Layer>
                        </Stage>
                    )}
                </div>

                <div className="image-attributes">
                    <h3>Image Attributes</h3>
                    <div className="animal-counts">
                        <ul>
                            {Object.entries(animalColors).map(([animalColorName, color]) => {
                                const count = boxes.filter((b) => b.animal === animalColorName).length;
                                if (count > 0) {
                                    return (
                                        <li key={animalColorName}>
                                            <span
                                                className="color-indicator"
                                                style={{ backgroundColor: color }}
                                            ></span>
                                            <span className="animal-name">{animalColorName}</span>: {count}
                                        </li>
                                    );
                                } else {
                                    return null;
                                }
                            })}
                        </ul>
                    </div>
                    <div className="controls">
                        {status === 'TAGGED' ? (
                            isEditing ? (
                                <>
                                    <button onClick={handleCancel} className="button-cancel big-button">
                                        Cancel
                                    </button>
                                    <button onClick={handleClear} className="button-clear big-button">
                                        Clear
                                    </button>
                                    <button onClick={handleSubmit} className="button-submit big-button">
                                        Submit
                                    </button>
                                    <button onClick={fetchNextTask} className="button-next big-button">
                                        Next
                                    </button>
                                </>
                            ) : (
                                <>
                                    <button onClick={validateImage} className="button-validate big-button">
                                        Validate
                                    </button>
                                    <button onClick={toggleEditMode} className="button-edit big-button">
                                        Edit
                                    </button>
                                    <button onClick={fetchNextTask} className="button-next big-button">
                                        Next
                                    </button>
                                </>
                            )
                        ) : status === 'PENDING' || status === '' ? (
                            <>
                                <button onClick={handleClear} className="button-clear big-button">
                                    Clear
                                </button>
                                <button onClick={handleSubmit} className="button-submit big-button">
                                    Submit
                                </button>
                                <button onClick={fetchNextTask} className="button-next big-button">
                                    Next
                                </button>
                            </>
                        ) : (
                            <>
                                <button onClick={fetchNextTask} className="button-next big-button">
                                    Next
                                </button>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default TaggingPage;
