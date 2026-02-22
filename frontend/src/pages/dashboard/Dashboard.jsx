import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import {
    HiOutlineCash,
    HiOutlineUsers,
    HiOutlineCurrencyRupee,
    HiOutlineClipboardCheck,
    HiOutlineExclamationCircle,
    HiOutlineTrendingUp,
} from 'react-icons/hi';

/**
 * Dashboard — The lender's overview page.
 *
 * Shows:
 *   - Stat cards (total loans, borrowers, amounts)
 *   - Upcoming EMIs
 *   - Recent loans
 *
 * Data comes from GET /api/dashboard (DashboardController.java)
 */
const Dashboard = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboard();
    }, []);

    const fetchDashboard = async () => {
        try {
            const response = await api.get('/dashboard');
            setData(response.data);
        } catch (error) {
            toast.error('Failed to load dashboard');
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
                <div className="spinner"></div>
            </div>
        );
    }

    if (!data) return <div className="empty-state"><h3>Could not load dashboard</h3></div>;

    // Format currency
    const formatCurrency = (amount) => {
        if (!amount && amount !== 0) return '₹0';
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(amount);
    };

    const statCards = [
        {
            label: 'Total Loans',
            value: data.totalLoans || 0,
            icon: <HiOutlineCash />,
            color: '#3d5af8',
            bg: '#eef2ff',
        },
        {
            label: 'Active Loans',
            value: data.activeLoans || 0,
            icon: <HiOutlineTrendingUp />,
            color: '#059669',
            bg: '#ecfdf5',
        },
        {
            label: 'Total Borrowers',
            value: data.totalBorrowers || 0,
            icon: <HiOutlineUsers />,
            color: '#d97706',
            bg: '#fffbeb',
        },
        {
            label: 'Total Lent',
            value: formatCurrency(data.totalAmountLent),
            icon: <HiOutlineCurrencyRupee />,
            color: '#7c3aed',
            bg: '#f5f3ff',
        },
        {
            label: 'Collected',
            value: formatCurrency(data.totalCollected),
            icon: <HiOutlineClipboardCheck />,
            color: '#059669',
            bg: '#ecfdf5',
        },
        {
            label: 'Outstanding',
            value: formatCurrency(data.totalOutstanding),
            icon: <HiOutlineExclamationCircle />,
            color: '#dc2626',
            bg: '#fef2f2',
        },
    ];

    return (
        <div className="fade-in">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Dashboard</h1>
                    <p className="page-subtitle">Overview of your lending portfolio</p>
                </div>
            </div>

            {/* Stat Cards */}
            <div className="stats-grid">
                {statCards.map((stat, i) => (
                    <div className="stat-card" key={i}>
                        <div
                            className="stat-icon"
                            style={{ background: stat.bg, color: stat.color }}
                        >
                            {stat.icon}
                        </div>
                        <div>
                            <div className="stat-value">{stat.value}</div>
                            <div className="stat-label">{stat.label}</div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Single-column layout since Upcoming EMIs are removed */}
            <div className="dashboard-grid" style={{ gridTemplateColumns: '1fr' }}>


                {/* Recent Loans */}
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">Recent Loans</h3>
                    </div>
                    {data.recentLoans && data.recentLoans.length > 0 ? (
                        data.recentLoans.map((loan, i) => (
                            <div className="recent-loan-item" key={i}>
                                <div>
                                    <div className="loan-borrower">{loan.borrowerName}</div>
                                    <div className="loan-details">
                                        {loan.interestRate}% interest
                                    </div>
                                </div>
                                <div>
                                    <div className="loan-amount">{formatCurrency(loan.principalAmount)}</div>
                                    <div className="loan-status">
                                        <span className={`badge ${loan.status === 'ACTIVE' ? 'badge-success' : 'badge-info'}`}>
                                            {loan.status}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className="empty-state" style={{ padding: '30px' }}>
                            <p>No loans yet</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
