export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

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
    MY_BOOKINGS: '/api/v1/bookings/my-bookings',
  },
  PAYMENTS: {
    INITIATE: '/api/v1/payments/initiate',
    VERIFY: '/api/v1/payments/verify',
    WEBHOOK_MOCK: '/api/v1/payments/webhook/mock',
  },
  REVIEWS: {
    LIST: '/api/v1/turfs/:turfId/reviews',
    CREATE: '/api/v1/turfs/:turfId/reviews',
  },
  TEAMS: {
    LIST: '/api/v1/teams',
    CREATE: '/api/v1/teams',
    GET: '/api/v1/teams/:teamId',
  },
  INVITATIONS: {
    LIST: '/api/v1/invitations',
    RESPOND: '/api/v1/invitations/:invitationId/respond',
  },
  OWNER: {
    STATS: '/api/v1/owner/stats',
    TURFS: '/api/v1/owner/turfs',
  }
};

export const WEBSOCKET_URL = `${API_BASE_URL}/ws`;
