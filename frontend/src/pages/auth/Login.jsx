import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { HiOutlineMail, HiOutlineLockClosed, HiOutlineUser, HiOutlineEye, HiOutlineEyeOff } from 'react-icons/hi';
import './Auth.css';

const Login = () => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const { loginUser } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const data = await loginUser(formData);
            toast.success(`Welcome back, ${data.name}!`);

            // Redirect based on role
            if (data.role === 'ADMIN') navigate('/admin');
            else if (data.role === 'BORROWER') navigate('/borrower');
            else navigate('/dashboard');
        } catch (error) {
            const message = error.response?.data?.message || 'Login failed. Check your credentials.';
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
                        <p className="auth-tagline">Smart Lending Management</p>
                        <div className="auth-features">
                            <div className="auth-feature">
                                <span className="auth-feature-icon">💰</span>
                                <span>Track loans easily</span>
                            </div>
                            <div className="auth-feature">
                                <span className="auth-feature-icon">📊</span>
                                <span>Interest calculations made simple</span>
                            </div>
                            <div className="auth-feature">
                                <span className="auth-feature-icon">🔒</span>
                                <span>Secure & private data management</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right Side — Form */}
                <div className="auth-form-section">
                    <form className="auth-form" onSubmit={handleSubmit}>
                        <div className="auth-form-header">
                            <h2>Welcome Back</h2>
                            <p>Sign in to manage your lending portfolio</p>
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

                        <div className="form-group">
                            <label className="form-label">Password</label>
                            <div className="input-with-icon">
                                <HiOutlineLockClosed className="input-icon" />
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    className="form-input"
                                    placeholder="Enter your password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    required
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

                        <button
                            type="submit"
                            className="btn btn-primary btn-lg auth-submit"
                            disabled={loading}
                        >
                            {loading ? <span className="spinner spinner-sm"></span> : 'Sign In'}
                        </button>

                        <p className="auth-switch">
                            Don't have an account? <Link to="/register">Create one</Link>
                        </p>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Login;
