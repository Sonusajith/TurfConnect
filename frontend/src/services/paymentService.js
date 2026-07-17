import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

export const paymentService = {
  initiate: async (bookingId, amount, currency = 'INR', provider = 'MOCK') => {
    return apiClient.post(API_ENDPOINTS.PAYMENTS.INITIATE, {
      bookingId,
      amount,
      currency,
      provider,
    });
  },

  // Simulates gateway success webhook for testing
  simulateWebhookSuccess: async (transactionId) => {
    return apiClient.post(API_ENDPOINTS.PAYMENTS.WEBHOOK_MOCK, {
      transactionId,
      status: 'success',
    }, {
      headers: {
        'X-Mock-Signature': 'mock_signature_123',
      },
    });
  },
};
