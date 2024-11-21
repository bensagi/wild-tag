import React, { useState } from 'react';
import './Topbar.css';
import { FaBell, FaQuestionCircle, FaUserCircle } from 'react-icons/fa';

function Topbar({ username, handleLogout }) {
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const toggleDropdown = (e) => {
        e.stopPropagation(); // Prevent event bubbling
        setIsDropdownOpen((prevState) => {
            console.log('Previous State:', prevState);
            return !prevState;
        });
    };

    return (
        <div className="topbar">
            <div className="left-section">
                <img src="/Israel_NPA_2014_Logo.svg" alt="Company Logo" className="paw-icon" />
                <span className="company-name">Israel Nature and Parks Authority</span>
            </div>
            <div className="right-section">
                <FaQuestionCircle className="topbar-icon" />
                <FaBell className="topbar-icon" />
                <div className="user-profile">
                    <div className="dropdown-wrapper" onClick={toggleDropdown}>
                        <span className="username">{username || 'Guest'}</span>
                        <span className="dropdown-arrow">▼</span>
                    </div>
                    {isDropdownOpen && (
                        <div className="dropdown-menu">
                            <button className="dropdown-item" onClick={handleLogout}>
                                Sign Out
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Topbar;