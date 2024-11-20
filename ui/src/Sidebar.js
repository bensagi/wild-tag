// Sidebar Component
import React from 'react';
import './Sidebar.css';
import { FaHome, FaProjectDiagram, FaCog } from 'react-icons/fa';

function Sidebar() {
    return (
        <div className="sidebar">
            <div className="sidebar-item">
                <FaHome className="sidebar-icon" />
                <span>Home</    span>
            </div>
            <div className="sidebar-item">
                <FaProjectDiagram className="sidebar-icon" />
                <span>Projects</span>
            </div>
            <div className="sidebar-item">
                <FaCog className="sidebar-icon" />
                <span>Settings</span>
            </div>
        </div>
    );
}

export default Sidebar;
