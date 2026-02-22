import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import {
    HiOutlinePlus,
    HiOutlineSearch,
    HiOutlineEye,
    HiOutlineCurrencyRupee,
    HiOutlineCalendar,
    HiOutlineUser,
    HiOutlinePencil,
} from 'react-icons/hi';

/**
 * LoanList — Displays all loans for the lender.
 */
const LoanList = () => {
    const [loans, setLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');

    useEffect(() => {
        fetchLoans();
    }, []);

    const fetchLoans = async () => {
        try {
            setLoading(true);
            const response = await api.get('/loans');
            setLoans(response.data);
        } catch (error) {
            toast.error('Failed to load loans');
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(amount);
    };

    const filteredLoans = loans.filter(l =>
        l.borrowerName.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="fade-in">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Loans</h1>
                    <p className="page-subtitle">Manage and track all issued loans</p>
                </div>
                <Link to="/loans/new" className="btn btn-primary">
                    <HiOutlinePlus /> Create New Loan
                </Link>
            </div>

            <div className="card" style={{ marginBottom: '24px' }}>
                <div className="input-with-icon" style={{ maxWidth: '400px' }}>
                    <HiOutlineSearch className="input-icon" />
                    <input
                        type="text"
                        className="form-input"
                        placeholder="Search by borrower name..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
            </div>

            {loading ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: '40px' }}>
                    <div className="spinner"></div>
                </div>
            ) : filteredLoans.length > 0 ? (
                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Borrower</th>
                                <th>Principal</th>
                                <th>Interest</th>
                                <th>Status</th>
                                <th>End Date</th>
                                <th>Remaining</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredLoans.map((loan) => (
                                <tr key={loan.id}>
                                    <td>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                            <HiOutlineUser style={{ color: 'var(--gray-400)' }} />
                                            <div style={{ fontWeight: 600 }}>{loan.borrowerName}</div>
                                        </div>
                                    </td>
                                    <td>{formatCurrency(loan.principalAmount)}</td>
                                    <td>
                                        {loan.interestRate}% /mo
                                        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginLeft: '4px' }}>
                                            ({loan.interestType})
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`badge ${loan.status === 'ACTIVE' ? 'badge-success' : loan.status === 'COMPLETED' ? 'badge-info' : 'badge-error'}`}>
                                            {loan.status}
                                        </span>
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.8125rem' }}>
                                            <HiOutlineCalendar />
                                            {loan.endDate || 'N/A'}
                                        </div>
                                    </td>
                                    <td style={{ fontWeight: 600, color: 'var(--error-600)' }}>
                                        {formatCurrency(loan.remainingAmount)}
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            <Link to={`/loans/${loan.id}`} className="btn btn-sm btn-secondary">
                                                <HiOutlineEye /> View
                                            </Link>
                                            {loan.status === 'ACTIVE' && (
                                                <Link to={`/loans/${loan.id}/edit`} className="btn btn-sm btn-secondary" title="Edit Loan">
                                                    <HiOutlinePencil />
                                                </Link>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div className="empty-state card">
                    <div className="empty-state-icon">💰</div>
                    <h3>No Loans Found</h3>
                    <p>You haven't created any loans yet.</p>
                    <Link to="/loans/new" className="btn btn-primary" style={{ marginTop: '16px' }}>
                        Create New Loan
                    </Link>
                </div>
            )}
        </div>
    );
};

export default LoanList;
