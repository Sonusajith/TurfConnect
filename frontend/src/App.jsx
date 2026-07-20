import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ToastProvider } from './contexts/ToastContext';
import AuthLayout from './layouts/AuthLayout';
import AppLayout from './layouts/AppLayout';
import ProtectedRoute from './router/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import TurfDetailsPage from './pages/TurfDetailsPage';
import SlotPickerPage from './pages/SlotPickerPage';
import BookingHistoryPage from './pages/BookingHistoryPage';
import ReviewsPage from './pages/ReviewsPage';
import TeamsPage from './pages/TeamsPage';
import InvitationsPage from './pages/InvitationsPage';
import MatchesPage from './pages/MatchesPage';
import TournamentsPage from './pages/TournamentsPage';
import LeaderboardPage from './pages/LeaderboardPage';
import AdminAnalyticsPage from './pages/AdminAnalyticsPage';
import OwnerDashboardPage from './pages/OwnerDashboardPage';
import SettingsPage from './pages/SettingsPage';
import NotFoundPage from './pages/NotFoundPage';
import { ROUTES } from './constants/routes';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            {/* Public/Auth Routes */}
            <Route element={<AuthLayout />}>
              <Route path={ROUTES.LOGIN} element={<LoginPage />} />
            </Route>

            {/* Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
                <Route path={ROUTES.TURF_DETAILS} element={<TurfDetailsPage />} />
                <Route path={ROUTES.SLOT_PICKER} element={<SlotPickerPage />} />
                <Route path={ROUTES.BOOKINGS} element={<BookingHistoryPage />} />
                <Route path={ROUTES.REVIEWS} element={<ReviewsPage />} />
                <Route path={ROUTES.TEAMS} element={<TeamsPage />} />
                <Route path={ROUTES.INVITATIONS} element={<InvitationsPage />} />
                <Route path={ROUTES.MATCHES} element={<MatchesPage />} />
                <Route path={ROUTES.TOURNAMENTS} element={<TournamentsPage />} />
                <Route path={ROUTES.LEADERBOARD} element={<LeaderboardPage />} />
                <Route path={ROUTES.ADMIN_ANALYTICS} element={<AdminAnalyticsPage />} />
                <Route path={ROUTES.OWNER_DASHBOARD} element={<OwnerDashboardPage />} />
                <Route path={ROUTES.SETTINGS} element={<SettingsPage />} />
              </Route>
            </Route>

            {/* 404 Route */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
