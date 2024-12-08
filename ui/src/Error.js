import React from 'react';
import './ErrorBox.css';

const ErrorBox = ({ message }) => {
    if (!message) return null;

    return (
        <div className="error-box">
            <span>{message}</span>
        </div>
    );
};

export default ErrorBox;