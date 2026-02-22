import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { HiOutlineArrowLeft, HiOutlineSave } from 'react-icons/hi';

/**
 * LoanEdit — Form to edit an existing loan.
 */
const LoanEdit = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [borrowers, setBorrowers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [initLoading, setInitLoading] = useState(true);
    const [loan, setLoan] = useState(null);

    const [formData, setFormData] = useState({
        borrowerId: '',
        principalAmount: '',
        interestRate: '',
        durationMonths: '',
        startDate: '',
        interestType: 'SIMPLE'
    });

    useEffect(() => {
        fetchInitialData();
    }, [id]);

    const fetchInitialData = async () => {
        try {
            setInitLoading(true);
            const [borrowerRes, loanRes] = await Promise.all([
                api.get('/borrowers'),
                api.get(`/loans/${id}`)
            ]);

            setBorrowers(borrowerRes.data);
            setLoan(loanRes.data);

            setFormData({
                borrowerId: loanRes.data.borrowerId,
                principalAmount: loanRes.data.principalAmount,
                interestRate: loanRes.data.interestRate,
                durationMonths: loanRes.data.durationMonths,
                startDate: loanRes.data.startDate,
                interestType: loanRes.data.interestType
            });
        } catch (error) {
            toast.error('Failed to load data for editing');
            navigate('/loans');
        } finally {
            setInitLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await api.put(`/loans/${id}`, formData);
            toast.success('Loan updated successfully! Schedules have been recalculated.');
            navigate(`/loans/${id}`);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to update loan');
        } finally {
            setLoading(false);
        }
    };

    if (initLoading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="fade-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
            <button
                className="btn btn-secondary"
                onClick={() => navigate(`/loans/${id}`)}
                style={{ marginBottom: '20px' }}
            >
                <HiOutlineArrowLeft /> Back to Details
            </button>

            <div className="page-header">
                <div>
                    <h1 className="page-title">Edit Loan #{id}</h1>
                    <p className="page-subtitle">Update loan terms to recalculate schedules</p>
                </div>
            </div>

            <div className="card">
                <div className="alert alert-warning" style={{ marginBottom: '20px', padding: '16px', backgroundColor: '#fffbeb', color: '#b45309', borderRadius: '8px', border: '1px solid #fde68a' }}>
                    <strong>Note:</strong> Modifying the duration, principal, or interest rate will completely recalculate the math for all schedules. Already paid logs will be updated to reflect the new expected amounts.
                </div>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>

                    <div className="form-group">
                        <label className="form-label">Select Borrower</label>
                        <select
                            className="form-select"
                            required
                            value={formData.borrowerId}
                            onChange={(e) => setFormData({ ...formData, borrowerId: e.target.value })}
                        >
                            <option value="">Choose a borrower...</option>
                            {borrowers.map(b => (
                                <option key={b.id} value={b.id}>{b.name} ({b.phone})</option>
                            ))}
                        </select>
                    </div>

                    <div className="loan-form-row">
                        <div className="form-group">
                            <label className="form-label">Principal Amount (₹)</label>
                            <input
                                type="number"
                                className="form-input"
                                required
                                min="1"
                                placeholder="e.g. 50000"
                                value={formData.principalAmount}
                                onChange={(e) => setFormData({ ...formData, principalAmount: e.target.value })}
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Monthly Interest Rate (%)</label>
                            <input
                                type="number"
                                step="0.1"
                                className="form-input"
                                required
                                min="0.1"
                                placeholder="e.g. 2 (₹2 per ₹100/month)"
                                value={formData.interestRate}
                                onChange={(e) => setFormData({ ...formData, interestRate: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="loan-form-row">
                        <div className="form-group">
                            <label className="form-label">Duration (Months)</label>
                            <input
                                type="number"
                                className="form-input"
                                required
                                min="1"
                                placeholder="e.g. 12"
                                value={formData.durationMonths}
                                onChange={(e) => setFormData({ ...formData, durationMonths: e.target.value })}
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Start Date</label>
                            <input
                                type="date"
                                className="form-input"
                                required
                                value={formData.startDate}
                                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Interest Calculation Type</label>
                        <div className="role-selector" style={{ gridTemplateColumns: '1fr 1fr' }}>
                            <label className={`role-option ${formData.interestType === 'SIMPLE' ? 'active' : ''}`}>
                                <input
                                    type="radio"
                                    name="interestType"
                                    value="SIMPLE"
                                    checked={formData.interestType === 'SIMPLE'}
                                    onChange={(e) => setFormData({ ...formData, interestType: e.target.value })}
                                />
                                <span className="role-icon">📉</span>
                                <span className="role-label">Simple Interest</span>
                                <span className="role-desc">Fixed interest on principal</span>
                            </label>
                            <label className={`role-option ${formData.interestType === 'COMPOUND' ? 'active' : ''}`}>
                                <input
                                    type="radio"
                                    name="interestType"
                                    value="COMPOUND"
                                    checked={formData.interestType === 'COMPOUND'}
                                    onChange={(e) => setFormData({ ...formData, interestType: e.target.value })}
                                />
                                <span className="role-icon">📈</span>
                                <span className="role-label">Compound Interest</span>
                                <span className="role-desc">Interest calculated monthly</span>
                            </label>
                        </div>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '16px', marginTop: '12px', borderTop: '1px solid var(--border-light)', paddingTop: '24px' }}>
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => navigate(`/loans/${id}`)}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="btn btn-primary btn-lg"
                            disabled={loading || (loan && loan.status !== 'ACTIVE')}
                            style={{ minWidth: '200px' }}
                        >
                            {loading ? (
                                <>
                                    <div className="spinner spinner-sm"></div> Saving...
                                </>
                            ) : (
                                <>
                                    <HiOutlineSave /> Save Changes
                                </>
                            )}
                        </button>
                    </div>

                </form>
            </div>
        </div>
    );
};

export default LoanEdit;
