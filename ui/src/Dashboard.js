import React from 'react';
import { Routes, Route } from 'react-router-dom';
import './Dashboard.css';
import Topbar from './Topbar';
import Sidebar from './Sidebar';
import UserManagement from './UserManagement';

function Dashboard({ username, handleLogout }) {
    return (
        <div className="dashboard-container">
            <Topbar username={username} handleLogout={handleLogout} />
            <div className="main-content">
                <Sidebar />
                <div className="page-content">
                    <Routes>
                        <Route path="/" element={<div>Welcome to the Home Page</div>} />
                        <Route path="/settings" element={<UserManagement />} />
                    </Routes>
                </div>
            </div>
        </div>
    );
}

export default Dashboard;
