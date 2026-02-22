import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
    HiOutlineHome,
    HiOutlineUsers,
    HiOutlineCash,
    HiOutlineDocumentReport,
    HiOutlineLogout,
    HiOutlineMenu,
    HiOutlineX,
    HiOutlineUser,
} from 'react-icons/hi';
import './Layout.css';

/**
 * Layout — The main app shell with sidebar + content area.
 *
 * This component wraps ALL authenticated pages.
 * Structure:
 *   ┌──────────┬──────────────────────────────┐
 *   │          │                              │
 *   │ SIDEBAR  │     MAIN CONTENT             │
 *   │ (fixed)  │     (scrollable)             │
 *   │          │     <Outlet /> renders        │
 *   │          │     the current page here     │
 *   │          │                              │
 *   └──────────┴──────────────────────────────┘
 *
 * <Outlet /> is React Router's placeholder — it renders
 * whichever child route is active (Dashboard, Borrowers, etc.)
 */
const Layout = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // Get user initials for avatar
    const initials = user?.name
        ? user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
        : '?';

    // Navigation items based on role
    const lenderNav = [
        { to: '/dashboard', icon: <HiOutlineHome />, label: 'Dashboard' },
        { to: '/borrowers', icon: <HiOutlineUsers />, label: 'Borrowers' },
        { to: '/loans', icon: <HiOutlineCash />, label: 'Loans' },
        { to: '/reports', icon: <HiOutlineDocumentReport />, label: 'Reports' },
        { to: '/profile', icon: <HiOutlineUser />, label: 'Profile' },
    ];

    return (
        <div className="app-layout">
            {/* Mobile menu toggle */}
            <button
                className="mobile-toggle"
                onClick={() => setSidebarOpen(!sidebarOpen)}
            >
                {sidebarOpen ? <HiOutlineX /> : <HiOutlineMenu />}
            </button>

            {/* Mobile overlay */}
            <div
                className={`sidebar-overlay ${sidebarOpen ? 'visible' : ''}`}
                onClick={() => setSidebarOpen(false)}
            />

            {/* Sidebar */}
            <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
                <div className="sidebar-header">
                    <div className="sidebar-logo">
                        <span className="sidebar-logo-icon">📒</span>
                        HisabKitab
                    </div>
                    <div className="sidebar-role">{user?.role} Portal</div>
                </div>

                <nav className="sidebar-nav">
                    <div className="nav-section-label">Menu</div>
                    {lenderNav.map((item) => (
                        <NavLink
                            key={item.to}
                            to={item.to}
                            className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                            onClick={() => setSidebarOpen(false)}
                        >
                            <span className="nav-link-icon">{item.icon}</span>
                            {item.label}
                        </NavLink>
                    ))}
                </nav>

                <div className="sidebar-footer">
                    <div className="sidebar-user">
                        <div className="sidebar-avatar">{initials}</div>
                        <div className="sidebar-user-info">
                            <div className="sidebar-user-name">{user?.name}</div>
                            <div className="sidebar-user-email">{user?.email}</div>
                        </div>
                    </div>
                    <button className="logout-btn" onClick={handleLogout}>
                        <HiOutlineLogout />
                        Logout
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="main-content">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
