import React from 'react';
import './Sidebar.css';
import { FaHome, FaCog, FaFolderOpen, FaUpload } from 'react-icons/fa';
import { NavLink } from 'react-router-dom';

function Sidebar({ role }) {
    return (
        <div className="sidebar">
            <NavLink to="/" className="sidebar-item">
                <FaHome className="sidebar-icon" />
                <span>Home</span>
            </NavLink>
            {role === 'ADMIN' && (
                <>
                    <NavLink to="/settings" className="sidebar-item">
                        <FaCog className="sidebar-icon" />
                        <span>Settings</span>
                    </NavLink>
                    <NavLink to="/categories" className="sidebar-item">
                        <FaFolderOpen className="sidebar-icon" />
                        <span>Categories</span>
                    </NavLink>
                    <NavLink to="/upload" className="sidebar-item">
                        <FaUpload className="sidebar-icon" />
                        <span>Upload</span>
                    </NavLink>
                </>
            )}
        </div>
    );
}

export default Sidebar;
