import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { ROUTES } from '../constants/routes';

const HERO_IMAGE =
  'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&w=1800&q=80';

const AuthLayout = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (user) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return (
    <div
      className="min-h-screen bg-primary-dark bg-cover bg-center font-sans"
      style={{
        backgroundImage: `linear-gradient(rgba(0, 69, 13, 0.78), rgba(3, 31, 12, 0.88)), url(${HERO_IMAGE})`,
      }}
    >
      <div className="min-h-screen flex flex-col items-center justify-center px-4 py-10">
        <div className="mb-8 text-center text-white">
          <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-lg border border-white/25 bg-white/15 text-2xl font-extrabold shadow-lg backdrop-blur">
            TC
          </div>
          <h2 className="text-4xl font-extrabold tracking-tight text-green-100">
            TurfConnect
          </h2>
          <p className="mt-2 text-lg font-bold text-white">
            Welcome Back, Athlete
          </p>
        </div>

        <div className="w-full max-w-[35rem]">
          <div className="rounded-lg border border-white/25 bg-white/95 px-5 py-7 shadow-2xl shadow-black/25 sm:px-8 sm:py-9">
            <Outlet />
          </div>
        </div>

        <p className="mt-8 text-center text-sm font-semibold text-white/70">
          (c) 2026 TurfConnect Field Management. All rights reserved.
        </p>
      </div>
    </div>
  );
};

export default AuthLayout;
