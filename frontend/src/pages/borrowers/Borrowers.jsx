import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import {
    HiOutlinePlus,
    HiOutlineSearch,
    HiOutlinePencil,
    HiOutlineTrash,
    HiOutlineLink,
    HiOutlineMail,
} from 'react-icons/hi';

/**
 * Borrowers — The borrower management page.
 * 
 * Features:
 *   - List all borrowers (GET /api/borrowers)
 *   - Search/Filter locally
 *   - Add New Borrower (Modal)
 *   - Edit Borrower (Modal)
 *   - Delete Borrower
 *   - Link to User Account (Modal)
 */
const Borrowers = () => {
    const [borrowers, setBorrowers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');

    // Modal states
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [isLinkOpen, setIsLinkOpen] = useState(false);
    const [selectedBorrower, setSelectedBorrower] = useState(null);

    // Data for the form
    const [formData, setFormData] = useState({
        name: '',
        phone: '',
        address: '',
        notes: ''
    });

    // Data for the link form
    const [linkEmail, setLinkEmail] = useState('');

    useEffect(() => {
        fetchBorrowers();
    }, []);

    const fetchBorrowers = async () => {
        try {
            setLoading(true);
            const response = await api.get('/borrowers');
            setBorrowers(response.data);
        } catch (error) {
            toast.error('Failed to load borrowers');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this borrower? All associated loans will also be affected.')) return;

        try {
            await api.delete(`/borrowers/${id}`);
            toast.success('Borrower deleted');
            fetchBorrowers();
        } catch (error) {
            toast.error('Failed to delete borrower');
        }
    };

    const handleOpenForm = (borrower = null) => {
        if (borrower) {
            setSelectedBorrower(borrower);
            setFormData({
                name: borrower.name,
                phone: borrower.phone || '',
                address: borrower.address || '',
                notes: borrower.notes || ''
            });
        } else {
            setSelectedBorrower(null);
            setFormData({ name: '', phone: '', address: '', notes: '' });
        }
        setIsFormOpen(true);
    };

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        try {
            if (selectedBorrower) {
                await api.put(`/borrowers/${selectedBorrower.id}`, formData);
                toast.success('Borrower updated');
            } else {
                await api.post('/borrowers', formData);
                toast.success('Borrower added');
            }
            setIsFormOpen(false);
            fetchBorrowers();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Action failed');
        }
    };

    const handleOpenLink = (borrower) => {
        setSelectedBorrower(borrower);
        setLinkEmail('');
        setIsLinkOpen(true);
    };

    const handleLinkSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/borrowers/${selectedBorrower.id}/link`, { email: linkEmail });
            toast.success('Borrower linked to user account');
            setIsLinkOpen(false);
            fetchBorrowers();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Linking failed');
        }
    };

    const filteredBorrowers = borrowers.filter(b =>
        b.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (b.phone && b.phone.includes(searchQuery))
    );

    return (
        <div className="fade-in">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Borrower Management</h1>
                    <p className="page-subtitle">Add and manage your borrower list</p>
                </div>
                <button className="btn btn-primary" onClick={() => handleOpenForm()}>
                    <HiOutlinePlus /> Add Borrower
                </button>
            </div>

            <div className="card" style={{ marginBottom: '24px' }}>
                <div className="input-with-icon" style={{ maxWidth: '400px' }}>
                    <HiOutlineSearch className="input-icon" />
                    <input
                        type="text"
                        className="form-input"
                        placeholder="Search by name or phone..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
            </div>

            {loading ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: '40px' }}>
                    <div className="spinner"></div>
                </div>
            ) : filteredBorrowers.length > 0 ? (
                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Borrower Name</th>
                                <th>Phone</th>
                                <th>Status</th>
                                <th>Portal Access</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredBorrowers.map((borrower) => (
                                <tr key={borrower.id}>
                                    <td>
                                        <div style={{ fontWeight: 600 }}>{borrower.name}</div>
                                        <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                                            {borrower.address || 'No address'}
                                        </div>
                                    </td>
                                    <td>{borrower.phone || 'N/A'}</td>
                                    <td>
                                        <span className={`badge ${borrower.status === 'ACTIVE' ? 'badge-success' : 'badge-error'}`}>
                                            {borrower.status}
                                        </span>
                                    </td>
                                    <td>
                                        {borrower.linkedToUser ? (
                                            <span className="badge badge-info">Linked</span>
                                        ) : (
                                            <button
                                                className="btn btn-sm btn-secondary"
                                                title="Link to portal"
                                                onClick={() => handleOpenLink(borrower)}
                                            >
                                                <HiOutlineLink /> Link
                                            </button>
                                        )}
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            <button
                                                className="btn btn-sm btn-secondary"
                                                onClick={() => handleOpenForm(borrower)}
                                                title="Edit"
                                            >
                                                <HiOutlinePencil />
                                            </button>
                                            <button
                                                className="btn btn-sm btn-danger"
                                                onClick={() => handleDelete(borrower.id)}
                                                title="Delete"
                                            >
                                                <HiOutlineTrash />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div className="empty-state card">
                    <div className="empty-state-icon">👤</div>
                    <h3>No Borrowers Found</h3>
                    <p>Start by adding your first borrower.</p>
                    <button className="btn btn-primary" style={{ marginTop: '16px' }} onClick={() => handleOpenForm()}>
                        Add Borrower
                    </button>
                </div>
            )}

            {/* Add/Edit Modal */}
            {isFormOpen && (
                <div className="modal-overlay">
                    <div className="modal-content card fade-in">
                        <div className="card-header">
                            <h3 className="card-title">{selectedBorrower ? 'Edit Borrower' : 'Add New Borrower'}</h3>
                            <button className="btn btn-secondary btn-sm" onClick={() => setIsFormOpen(false)}>×</button>
                        </div>
                        <form onSubmit={handleFormSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">Full Name</label>
                                <input
                                    className="form-input"
                                    required
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    placeholder="John Doe"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Phone Number</label>
                                <input
                                    className="form-input"
                                    value={formData.phone}
                                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                    placeholder="+91 9876543210"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Address</label>
                                <textarea
                                    className="form-input"
                                    value={formData.address}
                                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                                    placeholder="Home address..."
                                    rows="2"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Notes (Optional)</label>
                                <textarea
                                    className="form-input"
                                    value={formData.notes}
                                    onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                                    placeholder="Personal notes about this borrower..."
                                    rows="2"
                                />
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
                                <button type="button" className="btn btn-secondary" onClick={() => setIsFormOpen(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary">
                                    {selectedBorrower ? 'Update Borrower' : 'Save Borrower'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Link to User Modal */}
            {isLinkOpen && (
                <div className="modal-overlay">
                    <div className="modal-content card fade-in" style={{ maxWidth: '400px' }}>
                        <div className="card-header">
                            <h3 className="card-title">Link Borrower Portal</h3>
                            <button className="btn btn-secondary btn-sm" onClick={() => setIsLinkOpen(false)}>×</button>
                        </div>
                        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: '12px 0' }}>
                            Link <strong>{selectedBorrower?.name}</strong> to their registered User account to give them access to their own portal.
                        </p>
                        <form onSubmit={handleLinkSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">User Email</label>
                                <div className="input-with-icon">
                                    <HiOutlineMail className="input-icon" />
                                    <input
                                        type="email"
                                        className="form-input"
                                        required
                                        value={linkEmail}
                                        onChange={(e) => setLinkEmail(e.target.value)}
                                        placeholder="borrower@example.com"
                                    />
                                </div>
                                <small style={{ color: 'var(--gray-500)' }}>
                                    This user must have already registered with the role 'BORROWER'.
                                </small>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
                                <button type="button" className="btn btn-secondary" onClick={() => setIsLinkOpen(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary">Link Account</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Modal CSS - Inline for simplicity or add to Layout.css */}
            <style>{`
        .modal-overlay {
          position: fixed;
          inset: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 1000;
          padding: 20px;
        }
        .modal-content {
          width: 100%;
          max-width: 500px;
          max-height: 90vh;
          overflow-y: auto;
          box-shadow: var(--shadow-xl);
          animation: slideUp 0.3s ease-out;
        }
        @keyframes slideUp {
          from { transform: translateY(20px); opacity: 0; }
          to { transform: translateY(0); opacity: 1; }
        }
      `}</style>
        </div>
    );
};

export default Borrowers;
