import axios from 'axios';

/**
 * Axios Instance — Our pre-configured HTTP client.
 *
 * Instead of writing the full URL and headers every time:
 *   axios.get('http://localhost:8080/api/borrowers', { headers: { Authorization: `Bearer ${token}` } })
 *
 * We write:
 *   api.get('/borrowers')
 *
 * The instance automatically:
 *   1. Prepends the base URL (http://localhost:8080/api)
 *   2. Attaches the JWT token from localStorage
 *   3. Handles 401 errors (expired token → redirect to login)
 */

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

/**
 * REQUEST INTERCEPTOR — Runs BEFORE every API call.
 *
 * Think of it as a security guard at the gate:
 * "Before you leave, let me stamp your passport (JWT token) on the request."
 */
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

/**
 * RESPONSE INTERCEPTOR — Runs AFTER every API response.
 *
 * If the server says "401 Unauthorized" (token expired/invalid),
 * we clear the stored token and redirect to login page.
 */
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;
