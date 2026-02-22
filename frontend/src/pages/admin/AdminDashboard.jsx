import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import {
    HiOutlineUserGroup,
    HiOutlineOfficeBuilding,
    HiOutlineChartPie,
    HiOutlineUsers,
    HiOutlineSwitchHorizontal,
    HiOutlineTrash,
} from 'react-icons/hi';

/**
 * AdminDashboard — Higher-level view for administrators.
 * 
 * Features:
 *   - System stats (Users, Lenders, Borrowers)
 *   - User management (List all users, toggle ACTIVE/INACTIVE)
 *   - Financial health metrics across the entire system
 */
const AdminDashboard = () => {
    const { user: currentUser } = useAuth();
    const [data, setData] = useState(null);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('dashboard'); // 'dashboard' or 'users'
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [userDetails, setUserDetails] = useState(null);

    useEffect(() => {
        if (!selectedUserId) {
            fetchAdminData();
        }
    }, [activeTab, selectedUserId]);

    const fetchAdminData = async () => {
        try {
            setLoading(true);
            if (activeTab === 'dashboard') {
                const response = await api.get('/admin/dashboard');
                setData(response.data);
            } else {
                const response = await api.get('/admin/users');
                setUsers(response.data);
            }
        } catch (error) {
            toast.error('Failed to load admin data');
        } finally {
            setLoading(false);
        }
    };

    const fetchUserDetails = async (userId) => {
        try {
            setLoading(true);
            const response = await api.get(`/admin/users/${userId}`);
            setUserDetails(response.data);
            setSelectedUserId(userId);
        } catch (error) {
            toast.error('Failed to load user details');
        } finally {
            setLoading(false);
        }
    };

    const handleToggleStatus = async (userId) => {
        try {
            await api.patch(`/admin/users/${userId}/toggle`);
            toast.success('User status updated');
            fetchAdminData();
        } catch (error) {
            toast.error('Failed to update status');
        }
    };

    const handleDeleteUser = async (userId) => {
        if (!window.confirm('WARNING: Are you absolutely sure? This will PERMANENTLY delete the user and ALL their borrowers and loans. This action cannot be undone.')) return;
        try {
            await api.delete(`/admin/users/${userId}`);
            toast.success('User permanently deleted');
            setSelectedUserId(null);
            fetchAdminData(); // Refresh list stats
        } catch (error) {
            toast.error('Failed to delete user');
        }
    };

    const handleDeleteBorrower = async (borrowerId) => {
        if (!window.confirm('WARNING: Are you sure? This will PERMANENTLY delete the borrower and ALL their loans.')) return;
        try {
            await api.delete(`/admin/borrowers/${borrowerId}`);
            toast.success('Borrower permanently deleted');
            fetchUserDetails(selectedUserId); // Refresh detail view
        } catch (error) {
            toast.error('Failed to delete borrower');
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(amount || 0);
    };

    if (loading && !data && users.length === 0) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="fade-in container" style={{ padding: '40px 20px', maxWidth: '1200px', margin: '0 auto' }}>

            <div className="page-header" style={{ marginBottom: '32px' }}>
                <div>
                    <h1 className="page-title">Admin Control Panel</h1>
                    <p className="page-subtitle">System-wide management • Logged in as: <strong style={{ color: 'var(--primary)' }}>{currentUser?.name}</strong> ({currentUser?.email})</p>
                </div>
                <div style={{ display: 'flex', gap: '8px', background: 'var(--gray-100)', padding: '4px', borderRadius: 'var(--radius-md)' }}>
                    <button
                        className={`btn btn-sm ${activeTab === 'dashboard' ? 'btn-primary' : ''}`}
                        style={{ background: activeTab === 'dashboard' ? '' : 'transparent', color: activeTab === 'dashboard' ? '' : 'var(--text-secondary)' }}
                        onClick={() => setActiveTab('dashboard')}
                    >
                        System Health
                    </button>
                    <button
                        className={`btn btn-sm ${activeTab === 'users' ? 'btn-primary' : ''}`}
                        style={{ background: activeTab === 'users' ? '' : 'transparent', color: activeTab === 'users' ? '' : 'var(--text-secondary)' }}
                        onClick={() => setActiveTab('users')}
                    >
                        User Accounts
                    </button>
                </div>
            </div>

            {activeTab === 'dashboard' && data && (
                <>
                    {/* Admin Stats Grid */}
                    <div className="stats-grid" style={{ marginBottom: '32px' }}>
                        <div className="stat-card">
                            <div className="stat-icon" style={{ background: '#eef2ff', color: '#3d5af8' }}><HiOutlineUserGroup /></div>
                            <div>
                                <div className="stat-value">{data.totalUsers}</div>
                                <div className="stat-label">Total Users</div>
                            </div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-icon" style={{ background: '#ecfdf5', color: '#059669' }}><HiOutlineUsers /></div>
                            <div>
                                <div className="stat-value">{data.totalLoans}</div>
                                <div className="stat-label">System Loans</div>
                            </div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-icon" style={{ background: '#fffbeb', color: '#d97706' }}><HiOutlineOfficeBuilding /></div>
                            <div>
                                <div className="stat-value">{formatCurrency(data.totalSystemPrincipal)}</div>
                                <div className="stat-label">System-wide Principal</div>
                            </div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-icon" style={{ background: '#f5f3ff', color: '#7c3aed' }}><HiOutlineChartPie /></div>
                            <div>
                                <div className="stat-value">{formatCurrency(data.totalSystemInterestEarned)}</div>
                                <div className="stat-label">Interest Earned</div>
                            </div>
                        </div>
                    </div>

                    <div className="dashboard-grid">
                        {/* Recent Registrations */}
                        <div className="card">
                            <div className="card-header">
                                <h3 className="card-title">Recent Registrations</h3>
                            </div>
                            <div className="table-container">
                                <table style={{ border: 'none' }}>
                                    <thead>
                                        <tr>
                                            <th>User</th>
                                            <th>Role</th>
                                            <th>Joined</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {data.recentUsers.map((user, i) => (
                                            <tr key={i}>
                                                <td style={{ fontSize: '0.8125rem' }}>
                                                    <div style={{ fontWeight: 600 }}>{user.name}</div>
                                                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{user.email}</div>
                                                </td>
                                                <td><span className="badge badge-info">{user.role}</span></td>
                                                <td style={{ fontSize: '0.75rem' }}>{new Date(user.createdAt).toLocaleDateString()}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {/* Top Lenders */}
                        <div className="card">
                            <div className="card-header">
                                <h3 className="card-title">Top Lenders</h3>
                            </div>
                            {data.topLenders.map((lender, i) => (
                                <div className="upcoming-emi-item" key={i}>
                                    <div>
                                        <div className="emi-borrower">{lender.lenderName}</div>
                                        <div className="emi-date">{lender.loanCount} active loans</div>
                                    </div>
                                    <div className="emi-amount">{formatCurrency(lender.totalLent)}</div>
                                </div>
                            ))}
                        </div>
                    </div>
                </>
            )}

            {activeTab === 'users' && !selectedUserId && (
                <div className="card fade-in">
                    <div className="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>User Name</th>
                                    <th>Email</th>
                                    <th>Role</th>
                                    <th>Account Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.map((user) => (
                                    <tr key={user.id}>
                                        <td style={{ fontWeight: 600 }}>{user.name}</td>
                                        <td>{user.email}</td>
                                        <td><span className="badge badge-info">{user.role}</span></td>
                                        <td>
                                            <span className={`badge ${user.active ? 'badge-success' : 'badge-error'}`}>
                                                {user.active ? 'ACTIVE' : 'DEACTIVATED'}
                                            </span>
                                        </td>
                                        <td>
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                <button
                                                    className="btn btn-sm btn-primary"
                                                    onClick={() => fetchUserDetails(user.id)}
                                                >
                                                    View Details
                                                </button>
                                                <button
                                                    className={`btn btn-sm ${user.active ? 'btn-secondary' : 'btn-success'}`}
                                                    style={{ border: '1px solid var(--border-light)' }}
                                                    onClick={() => handleToggleStatus(user.id)}
                                                    disabled={user.email === currentUser?.email}
                                                    title={user.email === currentUser?.email ? "Cannot modify your own account" : ""}
                                                >
                                                    <HiOutlineSwitchHorizontal /> {user.active ? 'Deactivate' : 'Activate'}
                                                </button>
                                                <button
                                                    className="btn btn-sm"
                                                    style={{ color: user.email === currentUser?.email ? 'var(--text-secondary)' : 'var(--danger)', border: user.email === currentUser?.email ? 'none' : '1px solid var(--danger-light)', background: 'transparent' }}
                                                    onClick={() => handleDeleteUser(user.id)}
                                                    disabled={user.email === currentUser?.email}
                                                    title={user.email === currentUser?.email ? "Cannot delete your own account" : ""}
                                                >
                                                    <HiOutlineTrash /> Delete
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {activeTab === 'users' && selectedUserId && userDetails && (
                <div className="fade-in">
                    <button
                        className="btn btn-secondary"
                        style={{ marginBottom: '20px' }}
                        onClick={() => setSelectedUserId(null)}
                    >
                        &larr; Back to Users List
                    </button>

                    <div className="card" style={{ marginBottom: '24px' }}>
                        <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                                <h3 className="card-title">{userDetails.name}'s Details</h3>
                                <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{userDetails.email} • Joined {new Date(userDetails.createdAt).toLocaleDateString()}</p>
                            </div>
                            <button
                                className="btn"
                                style={{ color: '#fff', background: userDetails.email === currentUser?.email ? 'var(--gray-400)' : 'var(--danger)', border: 'none' }}
                                onClick={() => handleDeleteUser(userDetails.id)}
                                disabled={userDetails.email === currentUser?.email}
                                title={userDetails.email === currentUser?.email ? "Cannot delete your own account" : ""}
                            >
                                <HiOutlineTrash /> Delete Entire Account
                            </button>
                        </div>
                        <div className="stats-grid" style={{ marginBottom: '20px' }}>
                            <div className="stat-card" style={{ padding: '16px' }}>
                                <div className="stat-value" style={{ fontSize: '1.5rem' }}>{userDetails.totalLoans}</div>
                                <div className="stat-label">Total Loans</div>
                            </div>
                            <div className="stat-card" style={{ padding: '16px' }}>
                                <div className="stat-value" style={{ fontSize: '1.5rem' }}>{formatCurrency(userDetails.totalPrincipalLent)}</div>
                                <div className="stat-label">Total Principal Lent</div>
                            </div>
                            <div className="stat-card" style={{ padding: '16px' }}>
                                <div className="stat-value" style={{ fontSize: '1.5rem' }}>{formatCurrency(userDetails.totalAmountCollected)}</div>
                                <div className="stat-label">Total Collected</div>
                            </div>
                        </div>
                    </div>

                    <div className="card">
                        <div className="card-header">
                            <h3 className="card-title">Managed Borrowers ({userDetails.borrowers?.length || 0})</h3>
                        </div>
                        <div className="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Borrower Name</th>
                                        <th>Phone</th>
                                        <th>Active Loans</th>
                                        <th>Total Owed</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {userDetails.borrowers && userDetails.borrowers.length > 0 ? (
                                        userDetails.borrowers.map((b) => (
                                            <tr key={b.id}>
                                                <td style={{ fontWeight: 600 }}>{b.name}</td>
                                                <td>{b.phone || 'N/A'}</td>
                                                <td>{b.activeLoanCount}</td>
                                                <td style={{ fontWeight: 600 }}>{formatCurrency(b.totalOwed)}</td>
                                                <td>
                                                    <button
                                                        className="btn btn-sm"
                                                        style={{ color: 'var(--danger)', background: 'rgba(239, 68, 68, 0.1)', border: 'none' }}
                                                        onClick={() => handleDeleteBorrower(b.id)}
                                                    >
                                                        <HiOutlineTrash />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="5" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>
                                                No borrowers found for this user.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {/* Basic logout for admin */}
            <div style={{ marginTop: '60px', textAlign: 'center', borderTop: '1px solid var(--border-light)', paddingTop: '20px' }}>
                <button className="btn btn-secondary" onClick={() => { localStorage.clear(); window.location.href = '/login'; }}>Logout System</button>
            </div>
        </div>
    );
};

export default AdminDashboard;
