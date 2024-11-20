import React, { useEffect, useState } from 'react';
import './UserManagement.css';
import { FaEllipsisV } from 'react-icons/fa';

function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [visibleDropdown, setVisibleDropdown] = useState(null); // State to manage dropdown visibility

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

    const handleDeleteUser = async (email) => {
        if (window.confirm(`Are you sure you want to delete the user with email ${email}?`)) {
            try {
                const response = await fetch(`http://localhost:8080/wild-tag/users/${email}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to delete user.');
                }

                setUsers((prevUsers) => prevUsers.filter((user) => user.email !== email));
                alert(`User with email ${email} has been deleted.`);
            } catch (err) {
                alert(err.message);
            }
        }
    };

    const handleChangeRole = async (email) => {
        const newRole = prompt('Enter the new role for the user (e.g., Admin, Approver, Contributor):');
        if (newRole) {
            try {
                const response = await fetch(`http://localhost:8080/wild-tag/users/${email}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    },
                    body: JSON.stringify({ role: newRole }),
                });

                if (!response.ok) {
                    throw new Error('Failed to update user role.');
                }

                const updatedUser = await response.json();
                setUsers((prevUsers) =>
                    prevUsers.map((user) =>
                        user.email === email ? updatedUser : user
                    )
                );
                alert(`User role updated to ${newRole} for email: ${email}`);
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
            <button className="add-users-btn">Add Users</button>
            <table className="user-table">
                <thead>
                <tr>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Last login</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                {users.map((user) => (
                    <tr key={user.email}>
                        <td>{user.name}</td>
                        <td>{user.email}</td>
                        <td>{user.role}</td>
                        <td></td>
                        <td></td>
                        <td>
                            <div className="actions-dropdown">
                                <FaEllipsisV
                                    className="ellipsis-icon"
                                    onClick={() => toggleDropdown(user.email)}
                                />
                                {visibleDropdown === user.email && (
                                    <div className="dropdown-menu">
                                        <button onClick={() => handleChangeRole(user.email)}>Change Role</button>
                                        <button onClick={() => handleDeleteUser(user.email)}>Delete User</button>
                                    </div>
                                )}
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default UserManagement;
