import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import SignupForGoogle from './SignupForGoogle';
import Dashboard from './Dashboard';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState('');
  const [role, setRole] = useState('');
  const [idleTimeout, setIdleTimeout] = useState(null);

  const INACTIVITY_LIMIT = 10 * 60 * 1000;

  const handleLoginSuccess = (token, userData) => {
    const base64Payload = token.split('.')[1];
    const payload = JSON.parse(atob(base64Payload));
    const expiryTime = payload.exp * 1000;

    localStorage.setItem('authToken', token);
    localStorage.setItem('userData', JSON.stringify(userData));
    localStorage.setItem('expiryTime', expiryTime);

    setUsername(userData.email);
    setRole(userData.role);
    setIsLoggedIn(true);

    const jwtTimeout = expiryTime - Date.now();
    setTimeout(() => handleLogout(), jwtTimeout);

    resetIdleTimeout();
  };

  const handleLogout = () => {
    localStorage.clear();
    setUsername('');
    setRole('');
    setIsLoggedIn(false);
    clearTimeout(idleTimeout);
  };

  const resetIdleTimeout = () => {
    if (idleTimeout) clearTimeout(idleTimeout);
    const timeout = setTimeout(() => {
      alert('Session expired due to inactivity.');
      handleLogout();
    }, INACTIVITY_LIMIT);
    setIdleTimeout(timeout);
  };

  useEffect(() => {
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

      resetIdleTimeout();
    } else {
      handleLogout();
    }

    const events = ['mousemove', 'keydown', 'click'];
    events.forEach((event) => window.addEventListener(event, resetIdleTimeout));

    return () => {
      events.forEach((event) => window.removeEventListener(event, resetIdleTimeout));
    };
  }, []);

  return (
      <Router>
        <div className="app-container">
          {isLoggedIn ? (
              <Dashboard username={username} handleLogout={handleLogout} />
          ) : (
              <SignupForGoogle onLoginSuccess={handleLoginSuccess} />
          )}
        </div>
      </Router>
  );
}

export default App;
