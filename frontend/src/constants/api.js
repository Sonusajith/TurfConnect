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
    MY_TURFS: '/api/v1/turfs/my-turfs',
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
    LIST: '/api/v1/reviews/turf/:turfId',
    SUMMARY: '/api/v1/reviews/turf/:turfId/summary',
    CREATE: '/api/v1/reviews',
  },
  TEAMS: {
    LIST: '/api/v1/teams',
    CREATE: '/api/v1/teams',
    GET: '/api/v1/teams/:teamId',
  },
  INVITATIONS: {
    LIST: '/api/v1/invitations/me',
    ACCEPT: '/api/v1/invitations/:invitationId/accept',
    DECLINE: '/api/v1/invitations/:invitationId/decline',
  },
  OWNER: {
    STATS: '/api/v1/analytics/platform',
    TURFS: '/api/v1/turfs/my-turfs',
  }
};

export const WEBSOCKET_URL = `${API_BASE_URL}/ws`;
