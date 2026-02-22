import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { HiOutlineArrowLeft, HiOutlineUser, HiOutlineLockClosed } from 'react-icons/hi';

/**
 * BorrowerProfile — Page for borrowers to view and edit their profile.
 */
const BorrowerProfile = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [formData, setFormData] = useState({
        name: '',
        phone: '',
        address: '',
        newPassword: '',
        confirmPassword: ''
    });

    useEffect(() => {
        fetchProfileData();
    }, []);

    const fetchProfileData = async () => {
        try {
            setLoading(true);
            const response = await api.get('/borrower-portal/dashboard');
            // Populate form with existing data
            setFormData(prev => ({
                ...prev,
                name: response.data.borrowerName || '',
                phone: response.data.phone || '',
                address: response.data.address || ''
            }));
        } catch (error) {
            toast.error('Failed to load profile data');
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validate password change
        if (formData.newPassword || formData.confirmPassword) {
            if (formData.newPassword !== formData.confirmPassword) {
                toast.error("New passwords do not match.");
                return;
            }
            if (formData.newPassword.length < 6) {
                toast.error("Password must be at least 6 characters long.");
                return;
            }
        }

        try {
            setSaving(true);
            await api.put('/borrower-portal/profile', {
                name: formData.name,
                phone: formData.phone,
                address: formData.address,
                newPassword: formData.newPassword || null
            });
            toast.success('Profile updated successfully!');
            // Clear password fields after success
            setFormData(prev => ({ ...prev, newPassword: '', confirmPassword: '' }));
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to update profile');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="fade-in container" style={{ padding: '40px 20px', maxWidth: '800px', margin: '0 auto' }}>
            <button className="btn btn-secondary" style={{ marginBottom: '24px' }} onClick={() => navigate('/borrower')}>
                <HiOutlineArrowLeft /> Back to Dashboard
            </button>

            <div className="page-header">
                <div>
                    <h1 className="page-title">My Profile</h1>
                    <p className="page-subtitle">Manage your personal information and security settings</p>
                </div>
            </div>

            <form onSubmit={handleSubmit}>
                <div className="dashboard-grid">
                    {/* Personal Info Card */}
                    <div className="card">
                        <div className="card-header" style={{ marginBottom: '20px' }}>
                            <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <HiOutlineUser /> Personal Information
                            </h3>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">Full Name</label>
                                <input
                                    type="text"
                                    name="name"
                                    className="form-input"
                                    value={formData.name}
                                    onChange={handleChange}
                                    required
                                    placeholder="Your full name"
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Phone Number</label>
                                <input
                                    type="text"
                                    name="phone"
                                    className="form-input"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    placeholder="Your phone number"
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Address</label>
                                <textarea
                                    name="address"
                                    className="form-input"
                                    value={formData.address}
                                    onChange={handleChange}
                                    placeholder="Your residential address"
                                    rows="3"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Security Card */}
                    <div className="card">
                        <div className="card-header" style={{ marginBottom: '20px' }}>
                            <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <HiOutlineLockClosed /> Security
                            </h3>
                        </div>

                        <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
                            Leave these fields blank if you do not wish to change your password.
                        </p>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div className="form-group">
                                <label className="form-label">New Password</label>
                                <input
                                    type="password"
                                    name="newPassword"
                                    className="form-input"
                                    value={formData.newPassword}
                                    onChange={handleChange}
                                    placeholder="Enter new password"
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Confirm New Password</label>
                                <input
                                    type="password"
                                    name="confirmPassword"
                                    className="form-input"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    placeholder="Re-enter new password"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div style={{ marginTop: '24px', display: 'flex', justifyContent: 'flex-end', gap: '16px' }}>
                    <button type="button" className="btn btn-secondary" onClick={() => navigate('/borrower')}>
                        Cancel
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={saving}>
                        {saving ? 'Saving...' : 'Save Changes'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default BorrowerProfile;
