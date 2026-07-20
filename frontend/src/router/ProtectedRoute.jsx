import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { ROUTES } from '../constants/routes';

const ProtectedRoute = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  // Redirect to login if user session is not found
  if (!user) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  // Handle Root Path Redirects
  const currentPath = window.location.pathname;
  if (currentPath === ROUTES.DASHBOARD) {
    if (user.role === 'SUPER_ADMIN' || user.role === 'ORG_ADMIN') {
      return <Navigate to={ROUTES.ADMIN_ANALYTICS} replace />;
    }
    if (user.role === 'TURF_OWNER') {
      return <Navigate to={ROUTES.OWNER_DASHBOARD} replace />;
    }
  }

  // Handle Unauthorized Access
  const isOwnerRoute = currentPath.startsWith('/owner');
  const isAdminRoute = currentPath.startsWith('/admin');
  
  if (isOwnerRoute && user.role !== 'TURF_OWNER') {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }
  if (isAdminRoute && user.role !== 'SUPER_ADMIN' && user.role !== 'ORG_ADMIN') {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
