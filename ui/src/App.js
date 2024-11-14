import React from 'react';
import './App.css';
import { GoogleLogin } from '@react-oauth/google';
import ImageSlider from './ImageSlider'; // Import the ImageSlider component

const images = [
  { url: '/image_1.png', alt: 'Image 1' }, // Update the structure to match the ImageSlider prop
  { url: '/image_2.png', alt: 'Image 2' },
  { url: '/image_3.png', alt: 'Image 3' }
];

function App() {
  const handleLoginSuccess = (credentialResponse) => {
    console.log('Login Success:', credentialResponse);
  };

  const handleLoginFailure = (error) => {
    console.error('Login Failed:', error);
  };

  return (
    <div className="app-container">
      <div className="left-side">
        {/* Using the new ImageSlider component */}
        <ImageSlider images={images} />
      </div>
      <div className="right-side">
        <div className="card">
          <div className="logo">
            <img src="/paw.png" alt="Company Logo" className="paw-icon" />
          </div>
          <h1 className="company-name">Company name</h1>
          <p className="tagline">Create your free account</p>
          <div className="google-login-button">
            <GoogleLogin
              onSuccess={handleLoginSuccess}
              onError={handleLoginFailure}
            />
          </div>
          <p className="terms">
            By logging in or signing up, you agree to our policies, including our
            <a href="#"> Terms of Service </a> and <a href="#"> Privacy Policy </a>
          </p>
        </div>
      </div>
    </div>
  );
}

export default App;
