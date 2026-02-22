import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Dashboard from './pages/dashboard/Dashboard';
import Borrowers from './pages/borrowers/Borrowers';
import LoanList from './pages/loans/LoanList';
import LoanCreate from './pages/loans/LoanCreate';
import LoanEdit from './pages/loans/LoanEdit';
import LoanDetail from './pages/loans/LoanDetail';
import Reports from './pages/reports/Reports';
import BorrowerDashboard from './pages/borrower-portal/BorrowerDashboard';
import BorrowerProfile from './pages/borrower-portal/BorrowerProfile';
import LenderProfile from './pages/profile/LenderProfile';
import AdminDashboard from './pages/admin/AdminDashboard';
import './index.css';

/**
 * App — The root component of HisabKitab frontend.
 *
 * Routing Structure:
 *   /login           → Login page (public)
 *   /register        → Register page (public)
 *   /dashboard       → Lender Dashboard (requires LENDER role)
 *   /borrowers       → Borrower Management (coming soon)
 *   /loans           → Loan Management (coming soon)
 *   /reports         → Reports (coming soon)
 *   /borrower/*      → Borrower Portal (requires BORROWER role)
 *   /admin/*         → Admin Panel (requires ADMIN role)
 *
 * The Layout component wraps all authenticated pages and provides
 * the sidebar navigation. It uses <Outlet /> to render child routes.
 */

// Placeholder for pages not yet built
const ComingSoon = ({ title }) => (
  <div className="empty-state fade-in">
    <div className="empty-state-icon">🚧</div>
    <h3>{title}</h3>
    <p>This page is coming soon!</p>
  </div>
);

function App() {
  return (
    <AuthProvider>
      <Router>
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3000,
            style: {
              background: '#1e293b',
              color: '#f1f5f9',
              borderRadius: '8px',
              fontSize: '0.875rem',
            },
            success: { iconTheme: { primary: '#10b981', secondary: '#fff' } },
            error: { iconTheme: { primary: '#ef4444', secondary: '#fff' } },
          }}
        />
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Lender Routes — wrapped in Layout */}
          <Route element={
            <ProtectedRoute allowedRoles={['LENDER']}>
              <Layout />
            </ProtectedRoute>
          }>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/borrowers" element={<Borrowers />} />
            <Route path="/loans" element={<LoanList />} />
            <Route path="/loans/new" element={<LoanCreate />} />
            <Route path="/loans/:id/edit" element={<LoanEdit />} />
            <Route path="/loans/:id" element={<LoanDetail />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="/profile" element={<LenderProfile />} />
          </Route>

          {/* Borrower Portal */}
          <Route path="/borrower" element={
            <ProtectedRoute allowedRoles={['BORROWER']}>
              <BorrowerDashboard />
            </ProtectedRoute>
          } />
          <Route path="/borrower/profile" element={
            <ProtectedRoute allowedRoles={['BORROWER']}>
              <BorrowerProfile />
            </ProtectedRoute>
          } />

          {/* Admin Panel */}
          <Route path="/admin" element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AdminDashboard />
            </ProtectedRoute>
          } />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
