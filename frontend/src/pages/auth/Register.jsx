import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { HiOutlineMail, HiOutlineLockClosed, HiOutlineUser, HiOutlineEye, HiOutlineEyeOff } from 'react-icons/hi';
import './Auth.css';

const Register = () => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'LENDER',
    });
    const [adminCode, setAdminCode] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Client-side validation
        if (formData.password !== formData.confirmPassword) {
            toast.error('Passwords do not match!');
            return;
        }

        if (formData.password.length < 6) {
            toast.error('Password must be at least 6 characters');
            return;
        }

        if (formData.role === 'ADMIN' && adminCode !== 'VikkiAkki0210') {
            toast.error('Invalid Admin Registration Code');
            return;
        }

        setLoading(true);

        try {
            const { confirmPassword, ...registerPayload } = formData;
            const data = await register(registerPayload);
            toast.success(`Account created! Welcome, ${data.name}!`);

            if (data.role === 'BORROWER') navigate('/borrower');
            else navigate('/dashboard');
        } catch (error) {
            const message = error.response?.data?.message || 'Registration failed. Try again.';
            toast.error(message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                {/* Left Side — Branding */}
                <div className="auth-branding">
                    <div className="auth-branding-content">
                        <h1 className="auth-logo">📒 HisabKitab</h1>
                        <p className="auth-tagline">Start Your Lending Journey</p>
                        <div className="auth-features">
                            <div className="auth-feature">
                                <span className="auth-feature-icon">👤</span>
                                <span>Manage unlimited borrowers</span>
                            </div>
                            <div className="auth-feature">
                                <span className="auth-feature-icon">📋</span>
                                <span>Seamless loan management</span>
                            </div>
                            <div className="auth-feature">
                                <span className="auth-feature-icon">📥</span>
                                <span>Export reports to CSV</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right Side — Form */}
                <div className="auth-form-section">
                    <form className="auth-form" onSubmit={handleSubmit}>
                        <div className="auth-form-header">
                            <h2>Create Account</h2>
                            <p>Register to start managing your loans</p>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Full Name</label>
                            <div className="input-with-icon">
                                <HiOutlineUser className="input-icon" />
                                <input
                                    type="text"
                                    name="name"
                                    className="form-input"
                                    placeholder="Your full name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    required
                                    minLength={2}
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Email Address</label>
                            <div className="input-with-icon">
                                <HiOutlineMail className="input-icon" />
                                <input
                                    type="email"
                                    name="email"
                                    className="form-input"
                                    placeholder="you@example.com"
                                    value={formData.email}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Password</label>
                                <div className="input-with-icon">
                                    <HiOutlineLockClosed className="input-icon" />
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        name="password"
                                        className="form-input"
                                        placeholder="Min 6 characters"
                                        value={formData.password}
                                        onChange={handleChange}
                                        required
                                        minLength={6}
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle"
                                        onClick={() => setShowPassword(!showPassword)}
                                    >
                                        {showPassword ? <HiOutlineEyeOff /> : <HiOutlineEye />}
                                    </button>
                                </div>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Confirm Password</label>
                                <div className="input-with-icon">
                                    <HiOutlineLockClosed className="input-icon" />
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        name="confirmPassword"
                                        className="form-input"
                                        placeholder="Confirm password"
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">I am a...</label>
                            <div className="role-selector">
                                <label className={`role-option ${formData.role === 'LENDER' ? 'active' : ''}`}>
                                    <input
                                        type="radio"
                                        name="role"
                                        value="LENDER"
                                        checked={formData.role === 'LENDER'}
                                        onChange={handleChange}
                                    />
                                    <span className="role-icon">🏦</span>
                                    <span className="role-label">Lender</span>
                                    <span className="role-desc">I lend money</span>
                                </label>
                                <label className={`role-option ${formData.role === 'BORROWER' ? 'active' : ''}`}>
                                    <input
                                        type="radio"
                                        name="role"
                                        value="BORROWER"
                                        checked={formData.role === 'BORROWER'}
                                        onChange={handleChange}
                                    />
                                    <span className="role-icon">👤</span>
                                    <span className="role-label">Borrower</span>
                                    <span className="role-desc">I borrow money</span>
                                </label>
                                <label className={`role-option ${formData.role === 'ADMIN' ? 'active' : ''}`}>
                                    <input
                                        type="radio"
                                        name="role"
                                        value="ADMIN"
                                        checked={formData.role === 'ADMIN'}
                                        onChange={handleChange}
                                    />
                                    <span className="role-icon">👑</span>
                                    <span className="role-label">Admin</span>
                                    <span className="role-desc">I manage the platform</span>
                                </label>
                            </div>
                        </div>

                        {formData.role === 'ADMIN' && (
                            <div className="form-group fade-in">
                                <label className="form-label">Admin Registration Code</label>
                                <div className="input-with-icon">
                                    <HiOutlineLockClosed className="input-icon" />
                                    <input
                                        type="password"
                                        className="form-input"
                                        placeholder="Enter secret code"
                                        value={adminCode}
                                        onChange={(e) => setAdminCode(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>
                        )}

                        <button
                            type="submit"
                            className="btn btn-primary btn-lg auth-submit"
                            disabled={loading}
                        >
                            {loading ? <span className="spinner spinner-sm"></span> : 'Create Account'}
                        </button>

                        <p className="auth-switch">
                            Already have an account? <Link to="/login">Sign in</Link>
                        </p>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Register;
