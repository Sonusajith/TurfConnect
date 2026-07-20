import React, { useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Navigate } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import useOwner from '../hooks/useOwner';
import OwnerStats from '../features/owner/OwnerStats';
import OwnerTurfList from '../features/owner/OwnerTurfList';

const ownerRoles = ['TURF_OWNER', 'ORG_ADMIN', 'FRANCHISE_ADMIN', 'SUPER_ADMIN'];

const OwnerDashboardPage = () => {
  const { user } = useAuth();
  const { stats, turfs, loading, error, fetchDashboardData } = useOwner();

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  if (!user || !ownerRoles.includes(user.role)) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return (
    <div className="space-y-8 animate-fade-in pb-12">
      <div>
        <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Admin Dashboard</p>
        <h1 className="text-3xl font-extrabold tracking-tight text-primary-dark sm:text-4xl">Franchise Overview</h1>
        <p className="mt-1 text-sm font-medium text-gray-500">Manage your venues, view bookings, and analyze revenue.</p>
      </div>

      <OwnerStats stats={stats} loading={loading} error={error} />
      
      <div className="mt-12">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Your Venues</h2>
          <button className="bg-primary hover:bg-primary-dark text-white px-5 py-2 rounded-lg font-bold shadow-sm transition">
            Add New Venue
          </button>
        </div>
        <OwnerTurfList turfs={turfs} loading={loading} error={error} />
      </div>
    </div>
  );
};

export default OwnerDashboardPage;
