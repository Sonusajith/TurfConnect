import axios from 'axios';
import { API_BASE_URL, API_ENDPOINTS } from '../constants/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Token if present
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle auth errors and refresh token or redirect
apiClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;

    // Check for 401 Unauthorized or 403 Forbidden (could represent expired session)
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          // Attempt refresh
          const response = await axios.post(`${API_BASE_URL}${API_ENDPOINTS.AUTH.REFRESH}`, {
            refreshToken,
          });
          
          if (response.data && response.data.success) {
            const { accessToken, refreshToken: newRefreshToken } = response.data.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', newRefreshToken);
            
            // Retry the original request
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            return axios(originalRequest).then(res => res.data);
          }
        } catch (refreshError) {
          // Token refresh failed, clean up and redirect
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      } else {
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
      }
    }

    // Extract error message safely
    const errorMessage = error.response?.data?.message || 'Something went wrong';
    return Promise.reject(new Error(errorMessage));
  }
);

export default apiClient;
