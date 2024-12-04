import React, { useState } from 'react';
import './ImagesUploader.css';
import apiCall from './services/api';

function ImagesUploader() {
    const [bucketLink, setBucketLink] = useState('');
    const [message, setMessage] = useState({ type: '', text: '' });

    const isValidURL = (url) => {
        try {
            const sanitizedUrl = url.trim().replace(/\s+/g, ''); // Remove spaces and control characters
            const parsedUrl = new URL(sanitizedUrl);
            return parsedUrl.protocol === 'http:' || parsedUrl.protocol === 'https:';
        } catch (e) {
            return false;
        }
    };

    const sanitizeInput = (input) => {
        return input.replace(/[\n\r\t]+/g, '').trim(); // Remove newlines, tabs, and extra spaces
    };

    const handleBucketSubmit = async () => {
        const sanitizedLink = sanitizeInput(bucketLink);

        if (!sanitizedLink) {
            setMessage({ type: 'error', text: 'Please enter a valid GCP bucket link.' });
            return;
        }

        try {
            await apiCall(
                '/wild-tag/images/upload',
                'POST',
                { bucketName: sanitizedLink },
                { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
            );
            setMessage({ type: 'success', text: 'Bucket link submitted successfully!' });
            setBucketLink(''); // Clear the text area on success
        } catch (error) {
            console.error('Error submitting bucket link:', error);
            setMessage({ type: 'error', text: `Error submitting bucket link: ${error.message}` });
        }
    };

    return (
        <div className="bucket-uploader-container">
            <textarea
                placeholder="Enter GCP bucket link"
                value={bucketLink}
                onChange={(e) => setBucketLink(e.target.value)}
                className="bucket-textarea"
            />
            <button onClick={handleBucketSubmit} className="bucket-submit-button">
                Submit
            </button>
            {message.text && (
                <div className={`bucket-message ${message.type}`}>
                    {message.text}
                </div>
            )}
        </div>
    );
}

export default ImagesUploader;
