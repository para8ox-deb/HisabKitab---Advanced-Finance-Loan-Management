import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute — A security gate for pages.
 *
 * Usage:
 *   <Route path="/dashboard" element={
 *     <ProtectedRoute allowedRoles={['LENDER']}>
 *       <Dashboard />
 *     </ProtectedRoute>
 *   } />
 *
 * What it does:
 *   1. Not logged in? → Redirect to /login
 *   2. Wrong role? → Redirect to appropriate page
 *   3. Correct role? → Show the page
 */
const ProtectedRoute = ({ children, allowedRoles }) => {
    const { user, isAuthenticated, loading } = useAuth();

    // Still checking auth state
    if (loading) {
        return (
            <div className="loading-screen">
                <div className="spinner"></div>
            </div>
        );
    }

    // Not logged in
    if (!isAuthenticated || !user) {
        return <Navigate to="/login" replace />;
    }

    // Check role if roles are specified
    if (allowedRoles && !allowedRoles.includes(user.role)) {
        // Redirect to their appropriate dashboard
        if (user.role === 'LENDER') return <Navigate to="/dashboard" replace />;
        if (user.role === 'BORROWER') return <Navigate to="/borrower" replace />;
        if (user.role === 'ADMIN') return <Navigate to="/admin" replace />;
        return <Navigate to="/login" replace />;
    }

    return children;
};

export default ProtectedRoute;
