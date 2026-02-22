import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import {
    HiOutlineCash,
    HiOutlineCalendar,
    HiOutlineCheckCircle,
    HiOutlineCreditCard,
    HiOutlineArrowLeft
} from 'react-icons/hi';

/**
 * BorrowerDashboard — The portal view for a borrower.
 * 
 * Shows:
 *   - Current loan overview
 *   - Lenders info
 *   - List of all loans
 */
const BorrowerDashboard = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);


    useEffect(() => {
        fetchPortalData();
    }, []);

    const fetchPortalData = async () => {
        try {
            setLoading(true);
            const response = await api.get('/borrower-portal/dashboard');
            setData(response.data);
        } catch (error) {
            toast.error('Failed to load portal data');
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(amount || 0);
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
                <div className="spinner"></div>
            </div>
        );
    }

    if (!data || (!data.loans || data.loans.length === 0)) {
        return (
            <div className="empty-state container" style={{ padding: '80px 20px' }}>
                <div className="empty-state-icon">🛡️</div>
                <h3>Portal Not Linked or No Loans</h3>
                <p>Your account is not yet linked to any borrower record with active loans. Please contact your lender.</p>
            </div>
        );
    }

    return (
        <div className="fade-in container" style={{ padding: '40px 20px', maxWidth: '1000px', margin: '0 auto' }}>


            <>
                <div className="page-header">
                    <div>
                        <h1 className="page-title">Welcome, {data.borrowerName}</h1>
                        <p className="page-subtitle">Borrower Portal · Track your loans and upcoming payments</p>
                    </div>
                    <div style={{ display: 'flex', gap: '12px' }}>
                        <button className="btn btn-secondary" onClick={() => window.location.href = '/borrower/profile'}>My Profile</button>
                        <button className="btn btn-primary" onClick={() => window.location.reload()}>Refresh</button>
                    </div>
                </div>

                {/* Stat Cards */}
                <div className="stats-grid" style={{ marginBottom: '32px' }}>
                    <div className="stat-card">
                        <div className="stat-icon" style={{ background: '#eef2ff', color: '#3d5af8' }}><HiOutlineCash /></div>
                        <div>
                            <div className="stat-value">{formatCurrency(data.totalOutstanding)}</div>
                            <div className="stat-label">Total Outstanding</div>
                        </div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon" style={{ background: '#ecfdf5', color: '#059669' }}><HiOutlineCheckCircle /></div>
                        <div>
                            <div className="stat-value">{data.activeLoans}</div>
                            <div className="stat-label">Active Loans</div>
                        </div>
                    </div>

                </div>

                <div className="dashboard-grid" style={{ gridTemplateColumns: '1fr' }}>

                    {/* All Loans */}
                    <div className="card">
                        <div className="card-header">
                            <h3 className="card-title">My Loans</h3>
                        </div>
                        <div className="table-container">
                            <table style={{ border: 'none' }}>
                                <thead>
                                    <tr>
                                        <th>Lender</th>
                                        <th>Remaining</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {data.loans && data.loans.map((loan) => (
                                        <tr key={loan.loanId}>
                                            <td style={{ fontSize: '0.8125rem', fontWeight: 600 }}>{loan.lenderName}</td>
                                            <td style={{ fontSize: '0.8125rem' }}>{formatCurrency(loan.remainingAmount)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </>

            {/* Basic logout for portalled user */}
            <div style={{ marginTop: '60px', textAlign: 'center', borderTop: '1px solid var(--border-light)', paddingTop: '20px' }}>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                    Not your account? <button className="btn btn-sm btn-secondary" style={{ marginLeft: '10px' }} onClick={() => { localStorage.clear(); window.location.href = '/login'; }}>Logout</button>
                </p>
            </div>
        </div>
    );
};

export default BorrowerDashboard;
