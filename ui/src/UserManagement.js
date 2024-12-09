import React, { useEffect, useState, useRef } from 'react';
import './UserManagement.css';
import { FaEllipsisV } from 'react-icons/fa';
import apiCall from './services/api';
import ErrorBox from "./Error"; // Import the API utility

function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [addUserError, setAddUserError] = useState(null);
    const [changeRoleUserError, setChangeRoleUserError] = useState(null);
    const [deleteUserError, setDeleteUserError] = useState(null);
    const [visibleDropdown, setVisibleDropdown] = useState(null);
    const [showAddUsersModal, setShowAddUsersModal] = useState(false);
    const [showChangeRoleModal, setShowChangeRoleModal] = useState(false);
    const [showDeleteUserModal, setShowDeleteUserModal] = useState(false);
    const [newUsers, setNewUsers] = useState("");
    const [role, setRole] = useState("USER");
    const [selectedUser, setSelectedUser] = useState(null);

    const modalRef = useRef();

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const data = await apiCall('/wild-tag/users', 'GET', null, {
                    Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                });
                setUsers(data);
                setError('')
            } catch (err) {
                setError("Failed to load users. Please try again later.");
            } finally {
                setLoading(false);
            }
        };

        fetchUsers();
    }, []);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (modalRef.current && !modalRef.current.contains(event.target)) {
                setShowAddUsersModal(false);
                setShowChangeRoleModal(false);
                setShowDeleteUserModal(false);
                setVisibleDropdown(null);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const toggleDropdown = (email) => {
        setVisibleDropdown((prev) => (prev === email ? null : email));
    };

    const handleCancelAddUsers = () => {
        setShowAddUsersModal(false);
        setNewUsers("");
        setAddUserError('');
    }

    const handleAddUsers = async () => {
        const emails = newUsers.split(',').map((email) => email.trim());
        const validEmails = emails.filter((email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email));

        if (validEmails.length === 0) {
            alert('Please enter valid email addresses.');
            return;
        }

        const usersToCreate = validEmails.map((email) => ({
            name: email.split('@')[0],
            email,
            role,
        }));

        try {
            const createdUsers = await apiCall('/wild-tag/ausers/bulk', 'POST', usersToCreate, {
                Authorization: `Bearer ${localStorage.getItem('authToken')}`,
            });
            setUsers((prevUsers) => [...prevUsers, ...createdUsers]);
            setShowAddUsersModal(false);
            setNewUsers("");
            setAddUserError('');
        } catch (err) {
            setAddUserError("Failed to add users. Please try again later.");
        }
    };

    const handleChangeRole = async () => {
        if (selectedUser && role) {
            const updatedUserData = {
                name: selectedUser.name,
                email: selectedUser.email,
                role,
            };

            try {
                const updatedUser = await apiCall(`/wild-tag/users/${selectedUser.email}`, 'PUT', updatedUserData, {
                    Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                });

                setUsers((prevUsers) =>
                    prevUsers.map((user) =>
                        user.email === selectedUser.email ? updatedUser : user
                    )
                );
                setShowChangeRoleModal(false);
            } catch (err) {
                alert(err.message);
            }
        }
    };

    const handleCancelChangeRole = () => {
        setShowChangeRoleModal(false);
        setChangeRoleUserError('');
    }

    const handleDeleteUser = async () => {
        if (selectedUser) {
            try {
                await apiCall(`/wild-tag/users/${selectedUser.email}`, 'DELETE', null, {
                    Authorization: `Bearer ${localStorage.getItem('authToken')}`,
                });
                setUsers((prevUsers) => prevUsers.filter((user) => user.email !== selectedUser.email));
                setShowDeleteUserModal(false);
            } catch (err) {
                alert(err.message);
            }
        }
    };

    const handleCancelDeleteUser = () => {
        setShowDeleteUserModal(false);
        setDeleteUserError('');
    }

    if (loading) {
        return <div>Loading users...</div>;
    }

    if (error) {
        return <ErrorBox message={error} onClose={() => setError('')} />;
    }

    return (
        <div className="management-page">
            <h1>Users Management</h1>
            <div className="add-entity-container">
                <button className="add-users-btn" onClick={() => setShowAddUsersModal(true)}>
                    Add Users
                </button>
            </div>
            <table className="user-table">
                <thead>
                <tr>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                {users.map((user) => (
                    <tr key={user.email}>
                        <td>{user.name}</td>
                        <td>{user.email}</td>
                        <td>{user.role}</td>
                        <td>
                            <div className="actions-dropdown">
                                <FaEllipsisV
                                    className="ellipsis-icon"
                                    onClick={() => toggleDropdown(user.email)}
                                />
                                {visibleDropdown === user.email && (
                                    <div className="dropdown-menu">
                                        <button
                                            onClick={() => {
                                                setSelectedUser(user);
                                                setRole(user.role);
                                                setShowChangeRoleModal(true);
                                            }}
                                        >
                                            Change Role
                                        </button>
                                        <button
                                            onClick={() => {
                                                setSelectedUser(user);
                                                setShowDeleteUserModal(true);
                                            }}
                                        >
                                            Delete User
                                        </button>
                                    </div>
                                )}
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {showAddUsersModal && (
                <div className="modal">
                    <div className="modal-content" ref={modalRef}>
                        <h2>Add New Users</h2>
                        <textarea
                            placeholder="Enter valid emails, separated by commas"
                            value={newUsers}
                            onChange={(e) => setNewUsers(e.target.value)}
                        />
                        <select value={role} onChange={(e) => setRole(e.target.value)}>
                            <option value="ADMIN">Admin</option>
                            <option value="USER">User</option>
                        </select>
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={handleCancelAddUsers}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleAddUsers}>
                                Add Users
                            </button>
                        </div>
                        {addUserError && <ErrorBox message={addUserError} onClose={() => setAddUserError('')} />}
                    </div>
                </div>
            )}

            {showChangeRoleModal && (
                <div className="modal">
                    <div className="modal-content" ref={modalRef}>
                        <h2>Change Role for {selectedUser?.email}</h2>
                        <select value={role} onChange={(e) => setRole(e.target.value)}>
                            <option value="ADMIN">Admin</option>
                            <option value="USER">User</option>
                        </select>
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={handleCancelChangeRole}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleChangeRole}>
                                Update Role
                            </button>
                        </div>
                        {changeRoleUserError && <ErrorBox message={changeRoleUserError} onClose={() => setChangeRoleUserError('')} />}
                    </div>
                </div>
            )}

            {showDeleteUserModal && (
                <div className="modal">
                    <div className="modal-content" ref={modalRef}>
                        <h2>Delete User</h2>
                        <p>Are you sure you want to delete the user with email {selectedUser?.email}?</p>
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={handleCancelDeleteUser}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleDeleteUser}>
                                Confirm
                            </button>
                        </div>
                    </div>
                    {deleteUserError && <ErrorBox message={deleteUserError} onClose={() => setDeleteUserError('')} />}
                </div>
            )}
        </div>
    );
}

export default UserManagement;
