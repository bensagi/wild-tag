import React, { useState, useEffect } from 'react';
import './App.css';
import SignupForGoogle from './SignupForGoogle';
import Dashboard from './Dashboard';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState('');
  const [role, setRole] = useState('');
  const [idleTimeout, setIdleTimeout] = useState(null);

  const INACTIVITY_LIMIT = 10 * 60 * 1000; // 10 minutes

  const handleLoginSuccess = (token, userData) => {
    const base64Payload = token.split('.')[1]; // JWT payload is the second part
    const payload = JSON.parse(atob(base64Payload));
    const expiryTime = payload.exp * 1000; // Convert to milliseconds

    // Store token and user data in localStorage
    localStorage.setItem('authToken', token);
    localStorage.setItem('userData', JSON.stringify(userData));
    localStorage.setItem('expiryTime', expiryTime);

    setUsername(userData.email);
    setRole(userData.role);
    setIsLoggedIn(true);

    // Set JWT expiration timeout
    const jwtTimeout = expiryTime - Date.now();
    setTimeout(() => handleLogout(), jwtTimeout);

    // Start inactivity timeout
    resetIdleTimeout();
  };

  const handleLogout = () => {
    localStorage.clear(); // Clear all stored data
    setUsername('');
    setRole('');
    setIsLoggedIn(false);
    clearTimeout(idleTimeout); // Clear any active idle timeout
  };

  const resetIdleTimeout = () => {
    if (idleTimeout) clearTimeout(idleTimeout); // Clear the previous timeout
    const timeout = setTimeout(() => {
      alert('Session expired due to inactivity.');
      handleLogout();
    }, INACTIVITY_LIMIT);
    setIdleTimeout(timeout);
  };

  useEffect(() => {
    // Check if token is still valid on page load
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');
    const expiryTime = localStorage.getItem('expiryTime');

    if (token && userData && expiryTime && Date.now() < expiryTime) {
      const jwtTimeout = expiryTime - Date.now();
      setTimeout(() => handleLogout(), jwtTimeout);

      const parsedUserData = JSON.parse(userData);
      setUsername(parsedUserData.email);
      setRole(parsedUserData.role);
      setIsLoggedIn(true);

      // Start inactivity timeout
      resetIdleTimeout();
    } else {
      handleLogout(); // Clear any invalid data
    }

    // Add event listeners for user activity
    const events = ['mousemove', 'keydown', 'click'];
    events.forEach((event) => window.addEventListener(event, resetIdleTimeout));

    return () => {
      // Clean up event listeners
      events.forEach((event) => window.removeEventListener(event, resetIdleTimeout));
    };
  }, []);

  return (
      <div className="app-container">
        {isLoggedIn ? (
            <Dashboard username={username} role={role} handleLogout={handleLogout} />
        ) : (
            <SignupForGoogle onLoginSuccess={handleLoginSuccess} />
        )}
      </div>
  );
}

export default App;
