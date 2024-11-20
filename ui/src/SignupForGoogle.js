import React from 'react';
import './SignupForGoogle.css';
import { GoogleLogin } from '@react-oauth/google';
import ImageSlider from './ImageSlider';

const images = [
    { url: '/image_1.png', alt: 'Image 1' },
    { url: '/image_2.png', alt: 'Image 2' },
    { url: '/image_3.png', alt: 'Image 3' },
];

function SignupForGoogle({ onLoginSuccess }) {
    const handleLoginSuccess = async (credentialResponse) => {
        const jwtToken = credentialResponse.credential;

        // Save the JWT and send it to the server for validation
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
                const data = await response.json(); // Get the user data from the response
                console.log('Server Response:', data);

                // Notify parent component with the token and user data
                onLoginSuccess(jwtToken, data);
            } else {
                console.error('Failed to log in to server:', response.statusText);
            }
        } catch (error) {
            console.error('Error logging in to server:', error);
        }
    };

    const handleLoginFailure = (error) => {
        console.error('Login Failed:', error);
    };

    return (
        <div className="signup-container">
            <div className="left-side">
                <ImageSlider images={images} />
            </div>
            <div className="right-side">
                <div className="card">
                    <div className="logo">
                        <img
                            src="/Israel_NPA_2014_Logo.svg"
                            alt="Company Logo"
                            className="paw-icon"
                            style={{ width: '160px', height: '140px' }}
                        />
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
                        <button
                            className="link-button"
                            onClick={() => alert('Terms of Service')}
                        >
                            Terms of Service
                        </button>{' '}
                        and{' '}
                        <button
                            className="link-button"
                            onClick={() => alert('Privacy Policy')}
                        >
                            Privacy Policy
                        </button>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default SignupForGoogle;
