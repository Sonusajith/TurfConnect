import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

export const paymentService = {
  initiate: async (bookingId, amount, currency = 'INR', provider = 'RAZORPAY') => {
    return apiClient.post(API_ENDPOINTS.PAYMENTS.INITIATE, {
      bookingId,
      amount,
      currency,
      provider,
    });
  },

  // Calls the backend verify endpoint after successful frontend checkout
  verifyPayment: async (transactionId) => {
    return apiClient.post(`${API_ENDPOINTS.PAYMENTS.VERIFY}?transactionId=${transactionId}`);
  },

  // Simulates gateway success webhook for testing (Legacy)
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
