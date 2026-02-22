import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

/**
 * AuthContext — Global authentication state for the entire app.
 *
 * Problem it solves:
 *   Without context, every component that needs user info would need
 *   to read localStorage and pass props down through many levels.
 *
 * With context:
 *   Any component anywhere in the app can do:
 *     const { user, login, logout } = useAuth();
 *
 * Think of it like a building's PA system — any room can hear the announcement
 * without being individually wired.
 */

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [loading, setLoading] = useState(true);

    // On app start, check if there's a stored user
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser && token) {
            setUser(JSON.parse(storedUser));
        }
        setLoading(false);
    }, [token]);

    /**
     * LOGIN — Save user + token, update state.
     * Called after successful /api/auth/login response.
     */
    const login = (userData, authToken) => {
        localStorage.setItem('token', authToken);
        localStorage.setItem('user', JSON.stringify(userData));
        setToken(authToken);
        setUser(userData);
    };

    /**
     * REGISTER — Same as login (auto-login after registration).
     */
    const register = async (registerData) => {
        const response = await api.post('/auth/register', registerData);
        const { token: authToken, name, email, role } = response.data;
        login({ name, email, role }, authToken);
        return response.data;
    };

    /**
     * LOGIN REQUEST — Call auth API then save.
     */
    const loginUser = async (loginData) => {
        const response = await api.post('/auth/login', loginData);
        const { token: authToken, name, email, role } = response.data;
        login({ name, email, role }, authToken);
        return response.data;
    };

    /**
     * LOGOUT — Clear everything.
     */
    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    };

    const value = {
        user,
        token,
        loading,
        login,
        loginUser,
        register,
        logout,
        isAuthenticated: !!token,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

/**
 * useAuth() — Custom hook for easy access.
 * Usage: const { user, logout } = useAuth();
 */
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
