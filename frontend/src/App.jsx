import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ToastProvider } from './contexts/ToastContext';
import AuthLayout from './layouts/AuthLayout';
import AppLayout from './layouts/AppLayout';
import ProtectedRoute from './router/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminSecretLogin from './pages/AdminSecretLogin';
import LandingPage from './pages/LandingPage';
import DashboardPage from './pages/DashboardPage';
import ExplorePage from './pages/ExplorePage';
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
import AdminUsersPage from './pages/AdminUsersPage';
import JoinTeamPage from './pages/JoinTeamPage';
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
            <Route path={ROUTES.LANDING} element={<LandingPage />} />

            {/* Public/Auth Routes */}
            <Route element={<AuthLayout />}>
              <Route path={ROUTES.LOGIN} element={<LoginPage />} />
              <Route path={ROUTES.REGISTER} element={<RegisterPage />} />
              <Route path={ROUTES.ADMIN_LOGIN} element={<AdminSecretLogin />} />
            </Route>

            {/* Protected Routes */}
            {/* Public Join Team Route */}
        <Route path="/join-team/:teamId" element={<JoinTeamPage />} />

        <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
                <Route path={ROUTES.EXPLORE} element={<ExplorePage />} />
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
                <Route path={ROUTES.ADMIN_USERS} element={<AdminUsersPage />} />
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
