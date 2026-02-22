import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { HiOutlineArrowLeft, HiOutlineCalculator } from 'react-icons/hi';

/**
 * LoanCreate — Form to record a new loan.
 */
const LoanCreate = () => {
    const navigate = useNavigate();
    const [borrowers, setBorrowers] = useState([]);
    const [loading, setLoading] = useState(false);

    const [formData, setFormData] = useState({
        borrowerId: '',
        principalAmount: '',
        interestRate: '',
        durationMonths: '',
        startDate: new Date().toISOString().split('T')[0],
        interestType: 'SIMPLE'
    });

    useEffect(() => {
        fetchBorrowers();
    }, []);

    const fetchBorrowers = async () => {
        try {
            const response = await api.get('/borrowers');
            setBorrowers(response.data.filter(b => b.status === 'ACTIVE'));
        } catch (error) {
            toast.error('Failed to fetch borrowers');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await api.post('/loans', formData);
            toast.success('Loan created successfully!');
            navigate('/loans');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to create loan');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fade-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
            <button
                className="btn btn-secondary"
                onClick={() => navigate('/loans')}
                style={{ marginBottom: '20px' }}
            >
                <HiOutlineArrowLeft /> Back to List
            </button>

            <div className="page-header">
                <div>
                    <h1 className="page-title">Create New Loan</h1>
                    <p className="page-subtitle">Fill in the details to generate an interest schedule</p>
                </div>
            </div>

            <div className="card">
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
                            onClick={() => navigate('/loans')}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="btn btn-primary btn-lg"
                            disabled={loading}
                            style={{ minWidth: '200px' }}
                        >
                            {loading ? (
                                <>
                                    <div className="spinner spinner-sm"></div> Creating...
                                </>
                            ) : (
                                <>
                                    <HiOutlineCalculator /> Create Loan & Schedule
                                </>
                            )}
                        </button>
                    </div>

                </form>
            </div>
        </div>
    );
};

export default LoanCreate;
