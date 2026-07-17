import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TurfSearch from '../features/turfs/TurfSearch';
import TurfList from '../features/turfs/TurfList';
import useTurfs from '../hooks/useTurfs';
import { ROUTES } from '../constants/routes';

const DashboardPage = () => {
  const [searchParams, setSearchParams] = useState({});
  const { turfs, loading, error, refetch } = useTurfs(searchParams);
  const navigate = useNavigate();

  const handleSearch = (params) => {
    setSearchParams(params);
    refetch(params);
  };

  const handleViewSlots = (turfId) => {
    // Navigate to slot picker page for selected turf
    const path = ROUTES.SLOT_PICKER.replace(':turfId', turfId);
    navigate(path);
  };

  return (
    <div className="space-y-8">
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-primary to-primary-dark rounded-3xl p-8 md:p-12 text-white shadow-lg relative overflow-hidden">
        <div className="max-w-xl space-y-4 relative z-10">
          <h1 className="text-3xl md:text-5xl font-extrabold tracking-tight">
            Find & Book Your Next Arena
          </h1>
          <p className="text-base md:text-lg text-green-100 font-medium">
            Discover local sports pitches, view real-time slot availability, and reserve yours in minutes.
          </p>
        </div>
        {/* Subtle background decoration representing pitch lines */}
        <div className="absolute right-0 bottom-0 top-0 w-1/2 opacity-10 pointer-events-none hidden md:block border-l-4 border-dashed border-white rounded-l-full"></div>
      </div>

      {/* Search and Filters */}
      <section className="space-y-4">
        <h2 className="text-xl font-bold text-gray-900">Explore Sports Venues</h2>
        <TurfSearch onSearch={handleSearch} />
      </section>

      {/* Turf Listings */}
      <section>
        <TurfList
          turfs={turfs}
          loading={loading}
          error={error}
          onViewSlots={handleViewSlots}
        />
      </section>
    </div>
  );
};

export default DashboardPage;
