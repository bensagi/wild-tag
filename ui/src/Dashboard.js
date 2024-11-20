import React from 'react';
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
                    <UserManagement />
                </div>
            </div>
        </div>
    );
}

export default Dashboard;