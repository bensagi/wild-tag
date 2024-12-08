import React, {useEffect, useState} from 'react';
import './Tagging.css';
import apiCall from "./services/api";
import ErrorBox from "./Error";

function TaggingPage() {
    const [imageSrc, setImageSrc] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchImage = async () => {
            try {
                const imageResult = await apiCall('/wild-tag/images/next_task', 'GET', null, {
                    Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                });
                if (imageResult && imageResult.id) {
                    const imageResponse = await apiCall(`/wild-tag/images/${imageResult.id}`, 'GET', null, {
                        Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                    }, 'blob');
                    const blob = await imageResponse.blob();
                    const url = URL.createObjectURL(blob);
                    setImageSrc(url);
                } else {
                    console.error('Failed to fetch image content');
                }
            } catch (error) {
                setError("Failed to load image. Please try again later.");
                console.error('Error fetching image:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchImage();
    }, []);

    if (error) {
        return <ErrorBox message={error} onClose={() => setError('')} />;
    }

    if(loading) {
        return <div className="loading">Loading Image...</div>;
    }

    return (
        <div className="image-tag-page">
            {imageSrc && <img className="image-tag-div" src={imageSrc} alt="Fetched from GCP"/>}
        </div>
    );
}

export default TaggingPage;
