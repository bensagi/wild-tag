import React, { useEffect, useState } from 'react';
import './UserManagement.css';
import { FaEllipsisV } from 'react-icons/fa';

function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [visibleDropdown, setVisibleDropdown] = useState(null);
    const [showAddUsersModal, setShowAddUsersModal] = useState(false);
    const [showChangeRoleModal, setShowChangeRoleModal] = useState(false);
    const [showDeleteUserModal, setShowDeleteUserModal] = useState(false);
    const [newUsers, setNewUsers] = useState("");
    const [role, setRole] = useState("USER");
    const [selectedUser, setSelectedUser] = useState(null);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const response = await fetch('http://localhost:8080/wild-tag/users', {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch users.');
                }

                const data = await response.json();
                setUsers(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchUsers();
    }, []);

    const toggleDropdown = (email) => {
        setVisibleDropdown((prev) => (prev === email ? null : email));
    };

    const handleAddUsers = async () => {
        const emails = newUsers.split(',').map((email) => email.trim());
        const usersToCreate = emails.map((email) => ({
            name: email.split('@')[0],
            email,
            role,
        }));

        try {
            const response = await fetch('http://localhost:8080/wild-tag/users/bulk', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                },
                body: JSON.stringify(usersToCreate),
            });

            if (!response.ok) {
                throw new Error('Failed to add users.');
            }

            const createdUsers = await response.json();
            setUsers((prevUsers) => [...prevUsers, ...createdUsers]);
            alert('Users added successfully!');
            setShowAddUsersModal(false);
            setNewUsers("");
        } catch (err) {
            alert(err.message);
        }
    };

    const handleChangeRole = async () => {
        if (selectedUser && role) {
            try {
                const response = await fetch(`http://localhost:8080/wild-tag/users/${selectedUser.email}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    },
                    body: JSON.stringify({ ...selectedUser, role }),
                });

                if (!response.ok) {
                    throw new Error('Failed to update user role.');
                }

                const updatedUser = await response.json();
                setUsers((prevUsers) =>
                    prevUsers.map((user) => (user.email === selectedUser.email ? updatedUser : user))
                );
                alert(`Role updated to ${role} for user: ${selectedUser.email}`);
                setShowChangeRoleModal(false);
            } catch (err) {
                alert(err.message);
            }
        }
    };

    const handleDeleteUser = async () => {
        if (selectedUser) {
            try {
                const response = await fetch(`http://localhost:8080/wild-tag/users/${selectedUser.email}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to delete user.');
                }

                setUsers((prevUsers) => prevUsers.filter((user) => user.email !== selectedUser.email));
                alert(`User with email ${selectedUser.email} has been deleted.`);
                setShowDeleteUserModal(false);
            } catch (err) {
                alert(err.message);
            }
        }
    };

    if (loading) {
        return <div>Loading users...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="user-management-page">
            <h1>Users Management</h1>
            <button className="add-users-btn" onClick={() => setShowAddUsersModal(true)}>
                Add Users
            </button>
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

            {/* Add Users Modal */}
            {showAddUsersModal && (
                <div className="modal">
                    <div className="modal-content">
                        <h2>Add New Users</h2>
                        <p>Enter email addresses separated by commas:</p>
                        <textarea
                            placeholder="username1@gmail.com, username2@gmail.com"
                            value={newUsers}
                            onChange={(e) => setNewUsers(e.target.value)}
                        />
                        <p>Select Role:</p>
                        <select value={role} onChange={(e) => setRole(e.target.value)}>
                            <option value="ADMIN">Admin</option>
                            <option value="USER">User</option>
                        </select>
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={() => setShowAddUsersModal(false)}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleAddUsers}>
                                Add Users
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Change Role Modal */}
            {showChangeRoleModal && (
                <div className="modal">
                    <div className="modal-content">
                        <h2>Change Role for {selectedUser?.email}</h2>
                        <p>Select a new role:</p>
                        <select value={role} onChange={(e) => setRole(e.target.value)}>
                            <option value="ADMIN">Admin</option>
                            <option value="USER">User</option>
                        </select>
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={() => setShowChangeRoleModal(false)}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleChangeRole}>
                                Update Role
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Delete User Modal */}
            {showDeleteUserModal && (
                <div className="modal">
                    <div className="modal-content">
                        <h2>Delete User</h2>
                        <p>Are you sure you want to delete the user with email {selectedUser?.email}?</p>
                        <div className="modal-actions">
                            <button className="cancel-btn" onClick={() => setShowDeleteUserModal(false)}>
                                Cancel
                            </button>
                            <button className="send-invite-btn" onClick={handleDeleteUser}>
                                Confirm
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default UserManagement;
