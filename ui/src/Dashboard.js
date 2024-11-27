import React from 'react';
import { Routes, Route } from 'react-router-dom';
import './Dashboard.css';
import Topbar from './Topbar';
import Sidebar from './Sidebar';
import UserManagement from './UserManagement';
import Categories from "./Categories";
import ImagesUploader from './ImagesUploader';

function Dashboard({ username, role, handleLogout }) {
    return (
        <div className="dashboard-container">
            <Topbar username={username} handleLogout={handleLogout} />
            <div className="main-content">
                <Sidebar role={role}/>
                <div className="page-content">
                    <Routes>
                        <Route path="/" element={<div>Welcome to the Home Page</div>} />
                        {role === 'ADMIN' && (
                            <>
                                <Route path="/settings" element={<UserManagement />} />
                                <Route path="/categories" element={<Categories />} />
                                <Route path="/projects" element={<div>Projects Page</div>} />
                                <Route path="/upload" element={<ImagesUploader />} />
                            </>
                        )}
                    </Routes>
                </div>
            </div>
        </div>
    );
}

export default Dashboard;
