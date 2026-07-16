import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

export const slotService = {
  getSlots: async (turfId, date) => {
    const url = API_ENDPOINTS.SLOTS.LIST.replace(':turfId', turfId);
    return apiClient.get(url, { params: { date } });
  },
};
