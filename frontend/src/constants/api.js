export const API_BASE_URL = 'http://localhost:8080'; // API Gateway URL

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/v1/auth/login',
    REGISTER: '/api/v1/auth/register',
    REFRESH: '/api/v1/auth/refresh',
  },
  TURFS: {
    LIST: '/api/v1/turfs',
    GET: '/api/v1/turfs',
  },
  SLOTS: {
    LIST: '/api/v1/turfs/:turfId/slots',
  },
  BOOKINGS: {
    CREATE: '/api/v1/bookings',
    GET: '/api/v1/bookings/:bookingId',
    MY: '/api/v1/bookings/my',
  },
  PAYMENTS: {
    INITIATE: '/api/v1/payments/initiate',
    VERIFY: '/api/v1/payments/verify',
    WEBHOOK_MOCK: '/api/v1/payments/webhook/mock',
  },
};

export const WEBSOCKET_URL = `${API_BASE_URL}/ws`;
