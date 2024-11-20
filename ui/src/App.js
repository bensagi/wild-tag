import React, { useState } from 'react';
import './App.css';
import SignupForGoogle from './SignupForGoogle';
import Dashboard from './Dashboard';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState('');

  const handleLoginSuccess = (email) => {
    setUsername(email);
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    setUsername(''); // Clear the username
    setIsLoggedIn(false); // Return to the login page
  };

  return (
    <div className="app-container">
      {isLoggedIn ? (
        <Dashboard username={username} handleLogout={handleLogout} />
      ) : (
        <SignupForGoogle onLoginSuccess={handleLoginSuccess} />
      )}
    </div>
  );
}

export default App;
