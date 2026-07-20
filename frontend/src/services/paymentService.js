import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/api';

const getConfiguredProvider = () => {
  const configuredProvider = import.meta.env.VITE_PAYMENT_PROVIDER?.trim().toUpperCase();
  if (configuredProvider) {
    return configuredProvider;
  }

  return import.meta.env.VITE_RAZORPAY_KEY_ID ? 'RAZORPAY' : 'MOCK';
};

export const paymentService = {
  getConfiguredProvider,

  initiate: async (bookingId, amount, currency = 'INR', provider = getConfiguredProvider()) => {
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
