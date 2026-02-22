import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import { HiOutlineUser, HiOutlineLockClosed } from 'react-icons/hi';

/**
 * LenderProfile — Page for lenders to view and edit their profile.
 */
const LenderProfile = () => {
    const { user, login } = useAuth(); // We might need to update auth context if email/name changes
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        newPassword: '',
        confirmPassword: ''
    });

    useEffect(() => {
        fetchProfileData();
    }, []);

    const fetchProfileData = async () => {
        try {
            setLoading(true);
            const response = await api.get('/users/profile');
            setFormData(prev => ({
                ...prev,
                name: response.data.name || '',
                email: response.data.email || ''
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
            await api.put('/users/profile', {
                name: formData.name,
                email: formData.email,
                newPassword: formData.newPassword || null
            });
            toast.success('Profile updated successfully!');

            // If the user changed their email, the current JWT token might still work 
            // but the contextual user data needs updating. For a complete solution, 
            // the backend should issue a new token, but we'll reflect the visual changes here.
            // Ideally, the user logs out and logs back in if email/password changes heavily.

            setFormData(prev => ({ ...prev, newPassword: '', confirmPassword: '' }));

            // Informing user if email or password changed
            if (formData.email !== user?.email || formData.newPassword) {
                toast('Note: It is recommended to log out and log back in to apply all security changes.', {
                    icon: 'ℹ️',
                    duration: 5000
                });
            }

        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to update profile');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="fade-in" style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="fade-in" style={{ maxWidth: '800px' }}>
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
                                <label className="form-label">Email Address</label>
                                <input
                                    type="email"
                                    name="email"
                                    className="form-input"
                                    value={formData.email}
                                    onChange={handleChange}
                                    required
                                    placeholder="your.email@example.com"
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

                <div style={{ marginTop: '24px', display: 'flex', justifyContent: 'flex-start' }}>
                    <button type="submit" className="btn btn-primary" disabled={saving}>
                        {saving ? 'Saving...' : 'Save Changes'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default LenderProfile;
