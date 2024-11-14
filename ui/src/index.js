import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { GoogleOAuthProvider } from '@react-oauth/google';

const root = ReactDOM.createRoot(document.getElementById('root'));

// Wrap the application with GoogleOAuthProvider
root.render(
  <React.StrictMode>
    <GoogleOAuthProvider clientId="828896021325-mpilg3tgvcdvm3q84kqbdful3bvvtt0i.apps.googleusercontent.com">
      <App />
    </GoogleOAuthProvider>
  </React.StrictMode>
);
