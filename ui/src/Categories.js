import React, { useEffect, useState, useRef } from 'react';
import { FaEllipsisV } from "react-icons/fa";
import apiCall from "./services/api";

const Categories = () => {
    const [categories, setCategories] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showAddCategoryModal, setShowAddCategoryModal] = useState(false);
    const [newCategory, setNewCategory] = useState("");
    const [editedCategoryId, setEditedCategoryId] = useState("");
    const [showEditCategoryModal, setShowEditCategoryModal] = useState(false);
    const [visibleDropdown, setVisibleDropdown] = useState(null);
    const dropdownlRef = useRef();
    const editModalRef = useRef();
    const addModalRef = useRef();

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const result = await apiCall('/wild-tag/categories', 'GET', null, {
                    Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                });

                setCategories(result.entries || {});
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchCategories();
    }, []);

    const handleAddCategory = async () => {
        try {
            if (!categories || typeof categories !== 'object') return;

            let maxId = 0;
            if (Object.keys(categories).length > 0) {
                const ids = Object.keys(categories).map(Number);
                maxId = Math.max(...ids);
            }

            const newId = maxId + 1;
            categories[newId] = newCategory;

            await apiCall('/wild-tag/categories', 'PUT', { entries: categories }, {
                Authorization: `Bearer ${localStorage.getItem('authToken')}`,
            }, "text");
            setCategories({ ...categories });
            setShowAddCategoryModal(false);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEditCategory = async () => {
        try {
            if (!categories || typeof categories !== 'object') return;

            categories[editedCategoryId] = newCategory;
            await apiCall('/wild-tag/categories', 'PUT', { entries: categories }, {
                Authorization: `Bearer ${localStorage.getItem('authToken')}`,
            }, "text");
            setCategories({ ...categories });
            setShowEditCategoryModal(false);
        } catch (err) {
            setError(err.message);
        }
    };

    const toggleDropdown = (categoryId) => {
        setVisibleDropdown((prev) => (prev === categoryId ? null : categoryId));
    };

    const editPressed = (id) => {
        setVisibleDropdown(null);
        setNewCategory(categories[id]);
        setShowEditCategoryModal(true);
        setEditedCategoryId(id);
    };

    const addPressed = (id) => {
        setNewCategory("");
        setShowAddCategoryModal(true);
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownlRef.current && !dropdownlRef.current.contains(event.target)) {
                setVisibleDropdown(null);
            }

            if (editModalRef.current && !editModalRef.current.contains(event.target)) {
                setShowEditCategoryModal(false);
            }

            if (addModalRef.current && !addModalRef.current.contains(event.target)) {
                setShowAddCategoryModal(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    if (loading) {
        return <div>Loading categories...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="management-page">
            <h1>Categories</h1>
            <div className="add-entity-container">
                <button className="add-users-btn" onClick={addPressed}>
                    Add Category
                </button>
            </div>
            <table className="user-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                {Object.entries(categories || {}).map(([id, name]) => (
                    <tr key={id}>
                        <td>{id}</td>
                        <td>{name}</td>
                        <td>
                            <div className="actions-dropdown">
                                <FaEllipsisV
                                    className="ellipsis-icon"
                                    onClick={() => toggleDropdown(id)}
                                />
                                {visibleDropdown === id && (
                                    <div className="dropdown-menu" ref={dropdownlRef}>
                                        <button onClick={() => editPressed(id)}>
                                            Edit
                                        </button>
                                    </div>
                                )}
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {showAddCategoryModal && (
                <div className="modal">
                    <div className="modal-content" ref={addModalRef}>
                        <h2>Add New Category</h2>
                        <p>Enter category name:</p>
                        <textarea
                            placeholder="animal name here"
                            value={newCategory}
                            onChange={(e) => setNewCategory(e.target.value)}
                        />
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={() => setShowAddCategoryModal(false)}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleAddCategory}>
                                Add Category
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {showEditCategoryModal && (
                <div className="modal">
                    <div className="modal-content" ref={editModalRef}>
                        <h2>Edit Category</h2>
                        <p>Enter category name:</p>
                        <textarea
                            placeholder="animal name here"
                            value={newCategory}
                            onChange={(e) => setNewCategory(e.target.value)}
                        />
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={() => setShowEditCategoryModal(false)}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleEditCategory}>
                                Edit Category
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Categories;