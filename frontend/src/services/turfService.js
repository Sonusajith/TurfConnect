import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

export const turfService = {
  list: async (params = {}) => {
    // Supports query parameters for search/filtering
    return apiClient.get(API_ENDPOINTS.TURFS.LIST, { params });
  },

  get: async (turfId) => {
    return apiClient.get(`${API_ENDPOINTS.TURFS.GET}/${turfId}`);
  },

  create: async (turfData) => {
    return apiClient.post(API_ENDPOINTS.TURFS.LIST, turfData);
  },
};
