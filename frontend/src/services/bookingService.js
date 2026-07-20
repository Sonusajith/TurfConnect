import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

export const bookingService = {
  create: async (slotId, totalPrice) => {
    return apiClient.post(API_ENDPOINTS.BOOKINGS.CREATE, { slotId, totalPrice });
  },

  get: async (bookingId) => {
    const url = API_ENDPOINTS.BOOKINGS.GET.replace(':bookingId', bookingId);
    return apiClient.get(url);
  },

  getMyBookings: async () => {
    return apiClient.get(API_ENDPOINTS.BOOKINGS.MY_BOOKINGS);
  },
};
