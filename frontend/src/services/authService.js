import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

export const authService = {
  login: async (email, password) => {
    return apiClient.post(API_ENDPOINTS.AUTH.LOGIN, { email, password });
  },

  register: async (name, email, password, role) => {
    return apiClient.post(API_ENDPOINTS.AUTH.REGISTER, { name, email, password, role });
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },
};
