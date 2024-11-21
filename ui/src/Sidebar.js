import React from 'react';
import './Sidebar.css';
import { FaHome, FaProjectDiagram, FaCog } from 'react-icons/fa';
import { NavLink, useLocation } from 'react-router-dom';

function Sidebar() {
    const location = useLocation();

    return (
        <div className="sidebar">
            <NavLink
                to="/"
                className={`sidebar-item ${location.pathname === '/' ? 'active' : ''}`}
            >
                <FaHome className="sidebar-icon" />
                <span>Home</span>
            </NavLink>
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
        </div>
    );
}

export default Sidebar;
