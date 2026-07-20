import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

export const bookingService = {
  create: async (slotId, totalPrice, splitContribution) => {
    return apiClient.post(API_ENDPOINTS.BOOKINGS.CREATE, {
      slotId,
      totalPrice,
      ...(splitContribution ? { splitContribution } : {}),
    });
  },

  get: async (bookingId) => {
    const url = API_ENDPOINTS.BOOKINGS.GET.replace(':bookingId', bookingId);
    return apiClient.get(url);
  },

  getMyBookings: async () => {
    return apiClient.get(API_ENDPOINTS.BOOKINGS.MY_BOOKINGS);
  },

  cancel: async (bookingId) => {
    const url = API_ENDPOINTS.BOOKINGS.CANCEL.replace(':bookingId', bookingId);
    return apiClient.put(url);
  },

  updateSplitContribution: async (bookingId, splitContribution) => {
    const url = API_ENDPOINTS.BOOKINGS.UPDATE_SPLIT.replace(':bookingId', bookingId);
    return apiClient.put(url, splitContribution);
  },
};
