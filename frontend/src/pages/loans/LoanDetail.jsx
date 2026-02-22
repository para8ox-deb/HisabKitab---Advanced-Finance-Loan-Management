import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import {
    HiOutlineArrowLeft,
    HiOutlineCash,
    HiOutlineCalendar,
    HiOutlineCheckCircle,
    HiOutlineBan,
    HiOutlineInformationCircle,
    HiOutlineLightningBolt,
    HiOutlineTrash,
    HiOutlinePencil,
} from 'react-icons/hi';

/**
 * LoanDetail — Comprehensive view of a single loan.
 * 
 *   - Fetch full loan details
 *   - Partial Prepayment (Recalculates schedule)
 */
const LoanDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [loan, setLoan] = useState(null);
    const [loading, setLoading] = useState(true);

    // Prepayment Modal
    const [isPrepayOpen, setIsPrepayOpen] = useState(false);
    const [prepayAmount, setPrepayAmount] = useState('');
    const [prepayDate, setPrepayDate] = useState(new Date().toISOString().split('T')[0]);
    const [prepayLoading, setPrepayLoading] = useState(false);

    // Schedule Modal
    const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
    const [selectedScheduleId, setSelectedScheduleId] = useState(null);
    const [scheduleNote, setScheduleNote] = useState('');
    const [scheduleLoading, setScheduleLoading] = useState(false);

    // Settle Modal
    const [isSettleOpen, setIsSettleOpen] = useState(false);
    const [settleLoading, setSettleLoading] = useState(false);

    useEffect(() => {
        fetchLoanDetails();
    }, [id]);

    const fetchLoanDetails = async () => {
        try {
            setLoading(true);
            const loanRes = await api.get(`/loans/${id}`);

            setLoan(loanRes.data);
        } catch (error) {
            toast.error('Failed to load loan details');
            navigate('/loans');
        } finally {
            setLoading(false);
        }
    };

    const handlePrepay = async (e) => {
        e.preventDefault();
        setPrepayLoading(true);
        try {
            await api.post(`/loans/${id}/prepay`, {
                amount: parseFloat(prepayAmount),
                date: prepayDate
            });
            toast.success('Prepayment successful! Schedule updated.');
            setIsPrepayOpen(false);
            setPrepayAmount('');
            setPrepayDate(new Date().toISOString().split('T')[0]);
            fetchLoanDetails();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Prepayment failed');
        } finally {
            setPrepayLoading(false);
        }
    };

    const handlePaySchedule = async (e) => {
        e.preventDefault();
        setScheduleLoading(true);
        try {
            await api.patch(`/loans/schedule/${selectedScheduleId}/pay`, { note: scheduleNote });
            toast.success('Payment recorded successfully!');
            setIsScheduleModalOpen(false);
            setScheduleNote('');
            setSelectedScheduleId(null);
            fetchLoanDetails();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Payment failed');
        } finally {
            setScheduleLoading(false);
        }
    };

    const handleSettleLoan = async () => {
        setSettleLoading(true);
        try {
            await api.post(`/loans/${id}/settle`);
            toast.success('Loan settled completely!');
            setIsSettleOpen(false);
            fetchLoanDetails();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to settle loan');
        } finally {
            setSettleLoading(false);
        }
    };

    const handleDeleteLoan = async () => {
        if (window.confirm("Are you sure you want to completely delete this loan? This action cannot be undone and will delete all payment history.")) {
            try {
                await api.delete(`/loans/${id}`);
                toast.success('Loan deleted successfully');
                navigate('/loans');
            } catch (error) {
                toast.error(error.response?.data?.message || 'Failed to delete loan');
            }
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
        }).format(amount || 0);
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
                <div className="spinner"></div>
            </div>
        );
    }

    if (!loan) return null;

    // Calculate true settlement amount (remaining balance minus future unbilled interest)
    const today = new Date().toISOString().split('T')[0];
    const futureInterest = (loan.schedules || [])
        .filter(s => s.status === 'PENDING' && s.dueDate > today)
        .reduce((sum, s) => sum + s.amountDue, 0);
    const settlementAmount = loan.remainingAmount - futureInterest;

    return (
        <div className="fade-in">
            <button
                className="btn btn-secondary"
                onClick={() => navigate('/loans')}
                style={{ marginBottom: '20px' }}
            >
                <HiOutlineArrowLeft /> Back to Loans
            </button>

            <div className="page-header">
                <div>
                    <h1 className="page-title">{loan.borrowerName}'s Loan</h1>
                    <p className="page-subtitle">Loan ID: #{loan.id} · Issued on {loan.startDate}</p>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                    <button className="btn btn-secondary" onClick={() => window.print()}>Print Summary</button>
                    <button
                        className="btn btn-secondary"
                        onClick={handleDeleteLoan}
                        style={{ color: '#dc2626', borderColor: '#fecaca' }}
                    >
                        <HiOutlineTrash />
                    </button>
                    {loan.status === 'ACTIVE' && (
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsSettleOpen(true)}
                            style={{ color: '#10b981', borderColor: '#a7f3d0' }}
                        >
                            <HiOutlineCheckCircle /> Settle Now
                        </button>
                    )}
                    <button
                        className="btn btn-primary"
                        onClick={() => setIsPrepayOpen(true)}
                        disabled={loan.status === 'COMPLETED'}
                    >
                        <HiOutlineLightningBolt /> Partial Prepayment
                    </button>
                    {loan.status === 'ACTIVE' && (
                        <button
                            className="btn btn-secondary"
                            onClick={() => navigate(`/loans/${loan.id}/edit`)}
                            title="Edit Loan Terms"
                        >
                            <HiOutlinePencil /> Edit
                        </button>
                    )}
                </div>
            </div>

            {/* Financial Summary Cards */}
            <div className="stats-grid" style={{ marginBottom: '24px' }}>
                <div className="stat-card">
                    <div className="stat-icon" style={{ background: '#eef2ff', color: '#3d5af8' }}><HiOutlineCash /></div>
                    <div>
                        <div className="stat-value">{formatCurrency(loan.principalAmount)}</div>
                        <div className="stat-label">Principal</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon" style={{ background: '#ecfdf5', color: '#059669' }}><HiOutlineCheckCircle /></div>
                    <div>
                        <div className="stat-value">{formatCurrency(loan.paidAmount)}</div>
                        <div className="stat-label">Total Paid</div>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon" style={{ background: '#fef2f2', color: '#dc2626' }}><HiOutlineInformationCircle /></div>
                    <div>
                        <div className="stat-value">{formatCurrency(loan.remainingAmount)}</div>
                        <div className="stat-label">Remaining</div>
                    </div>
                </div>
            </div>

            {/* Loan Metadata */}
            <div className="card" style={{ marginBottom: '24px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '20px' }}>
                <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600, textTransform: 'uppercase' }}>Interest Type</div>
                    <div style={{ fontWeight: 600 }}>{loan.interestType} ({loan.interestRate}% /mo)</div>
                </div>
                <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600, textTransform: 'uppercase' }}>Duration</div>
                    <div style={{ fontWeight: 600 }}>{loan.durationMonths} Months</div>
                </div>
                <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600, textTransform: 'uppercase' }}>Monthly Interest</div>
                    <div style={{ fontWeight: 600, color: 'var(--primary-600)' }}>
                        {loan.interestType === 'SIMPLE' ? formatCurrency(loan.monthlyEmi) : 'Varies'}
                    </div>
                </div>
                <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600, textTransform: 'uppercase' }}>Total Amount</div>
                    <div style={{ fontWeight: 600 }}>{formatCurrency(loan.totalAmount)}</div>
                </div>
            </div>

            {/* Monthly Schedule Tracker */}
            {loan.schedules && loan.schedules.length > 0 && (
                <div className="card" style={{ marginBottom: '24px' }}>
                    <div className="card-header" style={{ borderBottom: '1px solid var(--border)', paddingBottom: '16px', marginBottom: '16px' }}>
                        <h2 className="card-title" style={{ fontSize: '1.25rem' }}>Monthly Schedule Tracker</h2>
                    </div>
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                            <thead>
                                <tr style={{ borderBottom: '1px solid var(--border)' }}>
                                    <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.875rem' }}>Month #</th>
                                    <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.875rem' }}>Due Date</th>
                                    <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.875rem' }}>Amount expected</th>
                                    <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.875rem' }}>Status</th>
                                    <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.875rem' }}>Note & Date</th>
                                    <th style={{ padding: '12px 16px', textAlign: 'right', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.875rem' }}>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loan.schedules.map((schedule) => (
                                    <tr key={schedule.id} className={schedule.status === 'PAID' ? 'row-paid' : ''} style={{ borderBottom: '1px solid var(--border)' }}>
                                        <td style={{ padding: '16px', fontWeight: 500 }}>Month {schedule.monthNumber}</td>
                                        <td style={{ padding: '16px', whiteSpace: 'nowrap' }}>{schedule.dueDate}</td>
                                        <td style={{ padding: '16px', fontWeight: 600 }}>{formatCurrency(schedule.amountDue)}</td>
                                        <td style={{ padding: '16px' }}>
                                            {schedule.status === 'PAID' ? (
                                                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', color: 'var(--success-600)', background: 'var(--success-100)', padding: '4px 8px', borderRadius: '999px', fontSize: '0.75rem', fontWeight: 600 }}>
                                                    <HiOutlineCheckCircle /> PAID
                                                </span>
                                            ) : (
                                                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', color: 'var(--warning-600)', background: 'var(--warning-100)', padding: '4px 8px', borderRadius: '999px', fontSize: '0.75rem', fontWeight: 600 }}>
                                                    PENDING
                                                </span>
                                            )}
                                        </td>
                                        <td style={{ padding: '16px', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                                            {schedule.status === 'PAID' ? (
                                                <div>
                                                    <div style={{ fontWeight: 500, color: 'var(--text-primary)' }}>{schedule.paidDate}</div>
                                                    {schedule.note && <div style={{ fontSize: '0.75rem' }}>"{schedule.note}"</div>}
                                                </div>
                                            ) : '-'}
                                        </td>
                                        <td style={{ padding: '16px', textAlign: 'right' }}>
                                            {schedule.status === 'PENDING' && (
                                                <button
                                                    className="btn btn-sm btn-secondary"
                                                    onClick={() => {
                                                        setSelectedScheduleId(schedule.id);
                                                        setIsScheduleModalOpen(true);
                                                    }}
                                                >
                                                    Mark Paid
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Prepayment Modal */}
            {isPrepayOpen && (
                <div className="modal-overlay">
                    <div className="modal-content card fade-in" style={{ maxWidth: '400px' }}>
                        <div className="card-header">
                            <h3 className="card-title">Partial Prepayment</h3>
                            <button className="btn btn-secondary btn-sm" onClick={() => setIsPrepayOpen(false)}>×</button>
                        </div>
                        <div style={{ background: 'var(--warning-50)', padding: '12px', borderRadius: 'var(--radius-md)', margin: '16px 0', border: '1px solid var(--warning-500)', display: 'flex', gap: '10px' }}>
                            <HiOutlineInformationCircle style={{ color: 'var(--warning-600)', fontSize: '1.5rem', flexShrink: 0 }} />
                            <p style={{ fontSize: '0.8125rem', color: 'var(--warning-600)' }}>
                                Prepayment reduces the principal. The remaining interest schedule will be automatically recalculated based on the <strong>reduced principal</strong>.
                            </p>
                        </div>
                        <form onSubmit={handlePrepay} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">Prepayment Amount (₹)</label>
                                <input
                                    type="number"
                                    className="form-input"
                                    required
                                    min="1"
                                    max={loan.principalAmount - 1}
                                    value={prepayAmount}
                                    onChange={(e) => setPrepayAmount(e.target.value)}
                                    placeholder="Enter amount to pay towards principal"
                                />
                                <small style={{ color: 'var(--text-secondary)' }}>
                                    Max: {formatCurrency(loan.principalAmount - 1)}
                                </small>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Prepayment Date</label>
                                <input
                                    type="date"
                                    className="form-input"
                                    required
                                    min={loan.startDate}
                                    max={new Date().toISOString().split('T')[0]}
                                    value={prepayDate}
                                    onChange={(e) => setPrepayDate(e.target.value)}
                                />
                                <small style={{ color: 'var(--text-secondary)' }}>
                                    Interest savings will be calculated from this date forward.
                                </small>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
                                <button type="button" className="btn btn-secondary" onClick={() => setIsPrepayOpen(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={prepayLoading}>
                                    {prepayLoading ? <div className="spinner spinner-sm"></div> : 'Confirm Payment'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Settle Loan Modal */}
            {isSettleOpen && (
                <div className="modal-overlay">
                    <div className="modal-content card fade-in" style={{ maxWidth: '400px' }}>
                        <div className="card-header">
                            <h3 className="card-title">Settle Loan</h3>
                            <button className="btn btn-secondary btn-sm" onClick={() => setIsSettleOpen(false)}>×</button>
                        </div>
                        <div style={{ background: 'var(--success-50)', padding: '16px', borderRadius: 'var(--radius-md)', margin: '16px 0', border: '1px solid var(--success-500)', textAlign: 'center' }}>
                            <div style={{ fontSize: '1rem', color: 'var(--success-700)', marginBottom: '4px' }}>True Settlement Amount</div>
                            <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--success-700)' }}>{formatCurrency(settlementAmount)}</div>
                            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '12px' }}>
                                This will pay off the remaining principal and any past-due interest. All future unbilled interest ({formatCurrency(futureInterest)}) will be forgiven.
                            </p>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
                            <button type="button" className="btn btn-secondary" onClick={() => setIsSettleOpen(false)}>Cancel</button>
                            <button type="button" className="btn btn-primary" onClick={handleSettleLoan} disabled={settleLoading} style={{ background: 'var(--success-600)' }}>
                                {settleLoading ? <div className="spinner spinner-sm"></div> : 'Confirm Settlement'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Schedule Payment Modal */}
            {isScheduleModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content card fade-in" style={{ maxWidth: '400px' }}>
                        <div className="card-header">
                            <h3 className="card-title">Mark Month as Paid</h3>
                            <button className="btn btn-secondary btn-sm" onClick={() => setIsScheduleModalOpen(false)}>×</button>
                        </div>
                        <form onSubmit={handlePaySchedule} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">Add a small Note (Optional)</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    maxLength="100"
                                    value={scheduleNote}
                                    onChange={(e) => setScheduleNote(e.target.value)}
                                    placeholder="e.g. Paid in cash, Transferred via UPI..."
                                />
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
                                <button type="button" className="btn btn-secondary" onClick={() => setIsScheduleModalOpen(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={scheduleLoading}>
                                    {scheduleLoading ? <div className="spinner spinner-sm"></div> : 'Confirm Payment'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <style>{`
        .modal-overlay {
          position: fixed;
          inset: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 1000;
        }
        .modal-content {
          width: 100%;
          max-width: 500px;
          animation: slideUp 0.3s ease-out;
        }
        .row-paid {
          background-color: var(--success-50) !important;
          opacity: 0.8;
        }
        @media print {
          .btn, .sidebar, .mobile-toggle, .modal-overlay { display: none !important; }
          .main-content { margin-left: 0 !important; padding: 0 !important; }
          .card { border: none !important; box-shadow: none !important; }
        }
      `}</style>
        </div>
    );
};

export default LoanDetail;
