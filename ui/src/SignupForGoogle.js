import React from 'react';
import './SignupForGoogle.css';
import { GoogleLogin } from '@react-oauth/google';
import ImageSlider from './ImageSlider';

const images = [
    { url: '/image_1.png', alt: 'Image 1' },
    { url: '/image_2.png', alt: 'Image 2' },
    { url: '/image_3.png', alt: 'Image 3' },
];

function decodeJWT(token) {
    try {
        // Split the JWT into its parts
        const base64Payload = token.split('.')[1]; // Get the payload part
        const jsonPayload = atob(base64Payload); // Decode Base64
        return JSON.parse(jsonPayload); // Parse JSON
    } catch (error) {
        console.error('Failed to decode JWT:', error);
        return null;
    }
}

function SignupForGoogle({ onLoginSuccess }) {
    const handleLoginSuccess = async (credentialResponse) => {
        const jwtToken = credentialResponse.credential;

        // Save the JWT to localStorage for later use
        localStorage.setItem('authToken', jwtToken);

        const decodedToken = decodeJWT(jwtToken);
        if (decodedToken) {
            const email = decodedToken.email || 'Unknown Email';
            console.log('Email extracted from JWT:', email);

            // Send the token to the server with Authorization header
            try {
                const response = await fetch('http://localhost:8080/wild-tag/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwtToken}`,
                    },
                    body: JSON.stringify({}),
                });

                if (response.ok) {
                    const data = await response.json();
                    console.log('Server Response:', data);
                    onLoginSuccess(email); // Notify parent component or redirect
                } else {
                    console.error('Failed to log in to server:', response.statusText);
                }
            } catch (error) {
                console.error('Error logging in to server:', error);
            }
        } else {
            console.error('Failed to extract email from credential.');
        }
    };

    const handleLoginFailure = (error) => {
        console.error('Login Failed:', error);
    };

    return (
        <div className="signup-container">
            {/* Left Side: Image Slider */}
            <div className="left-side">
                <ImageSlider images={images} />
            </div>

            {/* Right Side: Signup Form */}
            <div className="right-side">
                <div className="card">
                    <div className="logo">
                        <img src="/Israel_NPA_2014_Logo.svg" alt="Company Logo" className="paw-icon" style={{ width: '160px', height: '140px' }}/>
                    </div>
                    <h1 className="company-name">Israel Nature and Parks Authority</h1>
                    <div className="google-login-button">
                        <GoogleLogin
                            onSuccess={handleLoginSuccess}
                            onError={handleLoginFailure}
                        />
                    </div>
                    <p className="terms">
                        By logging in or signing up, you agree to our policies, including our{' '}
                        <button className="link-button" onClick={() => alert('Terms of Service')}>
                            Terms of Service
                        </button>{' '}
                        and{' '}
                        <button className="link-button" onClick={() => alert('Privacy Policy')}>
                            Privacy Policy
                        </button>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default SignupForGoogle;
