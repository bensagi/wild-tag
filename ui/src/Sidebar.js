import React from 'react';
import './Sidebar.css';
import { FaHome, FaProjectDiagram, FaCog, FaFolderOpen, FaUpload } from 'react-icons/fa';
import { NavLink, useLocation } from 'react-router-dom';

function Sidebar({ role }) {
    const location = useLocation();

    return (
        <div className="sidebar">
            {/* Home is visible to all roles */}
            <NavLink
                to="/"
                className={`sidebar-item ${location.pathname === '/' ? 'active' : ''}`}
            >
                <FaHome className="sidebar-icon" />
                <span>Home</span>
            </NavLink>

            {/* Conditionally render other links based on role */}
            {role === 'ADMIN' && (
                <>
                    <NavLink
                        to="/projects"
                        className={`sidebar-item ${location.pathname === '/projects' ? 'active' : ''}`}
                    >
                        <FaProjectDiagram className="sidebar-icon" />
                        <span>Projects</span>
                    </NavLink>
                    <NavLink
                        to="/settings"
                        className={`sidebar-item ${location.pathname === '/settings' ? 'active' : ''}`}
                    >
                        <FaCog className="sidebar-icon" />
                        <span>Settings</span>
                    </NavLink>
                    <NavLink
                        to="/categories"
                        className={`sidebar-item ${location.pathname === '/categories' ? 'active' : ''}`}
                    >
                        <FaFolderOpen className="sidebar-icon" />
                        <span>Categories</span>
                    </NavLink>
                    <NavLink
                        to="/upload"
                        className={`sidebar-item ${location.pathname === '/upload' ? 'active' : ''}`}
                    >
                        <FaUpload className="sidebar-icon" />
                        <span>Upload</span>
                    </NavLink>
                </>
            )}
        </div>
    );
}

export default Sidebar;
