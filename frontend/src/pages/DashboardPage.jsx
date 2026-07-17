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
    const path = ROUTES.SLOT_PICKER.replace(':turfId', turfId);
    navigate(path);
  };

  return (
    <div className="space-y-12 animate-fade-in pb-12">
      {/* Premium Hero Section */}
      <div className="bg-gradient-to-r from-primary-dark via-primary to-secondary rounded-[2.5rem] p-10 md:p-16 text-white shadow-2xl relative overflow-hidden">
        {/* Abstract Background Patterns */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3"></div>
        <div className="absolute bottom-0 left-1/4 w-48 h-48 bg-accent/20 rounded-full blur-2xl translate-y-1/2"></div>
        
        <div className="max-w-2xl space-y-6 relative z-10">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md border border-white/20 rounded-full text-sm font-semibold tracking-wide uppercase text-white/90">
            <span className="w-2 h-2 rounded-full bg-accent animate-pulse-subtle"></span>
            Book Instantly
          </div>
          <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight leading-tight drop-shadow-sm">
            Find & Book Your <br/>
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-green-200 to-white">Next Arena</span>
          </h1>
          <p className="text-lg md:text-xl text-green-50 font-medium max-w-xl leading-relaxed">
            Discover premium local sports pitches, view real-time availability, and reserve yours in seconds.
          </p>
        </div>
        
        {/* Subtle Pitch Decoration */}
        <div className="absolute right-0 bottom-0 top-0 w-1/3 opacity-20 pointer-events-none hidden lg:block border-l-4 border-dashed border-white rounded-l-[100%] transform translate-x-12"></div>
      </div>

      {/* Floating Search Bar */}
      <div className="px-2 md:px-0">
        <TurfSearch onSearch={handleSearch} />
      </div>

      {/* Venues Listing */}
      <section className="px-2 md:px-0 pt-4">
        <div className="flex justify-between items-end mb-8">
          <div>
            <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Explore Venues</h2>
            <p className="text-gray-500 mt-1 font-medium">Find the perfect pitch for your next game.</p>
          </div>
        </div>
        
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
