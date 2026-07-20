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

  const handleViewDetails = (turfId) => {
    const path = ROUTES.TURF_DETAILS.replace(':turfId', turfId);
    navigate(path);
  };

  const startingPrice = turfs?.length
    ? Math.min(...turfs.map((turf) => Number(turf.hourlyRate || 0)).filter(Boolean))
    : 0;
  const sportsCount = new Set((turfs || []).flatMap((turf) => turf.sportTypes || [])).size;
  const topRating = turfs?.length
    ? Math.max(...turfs.map((turf) => Number(turf.averageRating || 0)))
    : 0;
  const activeFilters = Object.entries(searchParams)
    .filter(([key, value]) => value && !['sortBy', 'sortDirection'].includes(key))
    .map(([key, value]) => `${key.replace(/([A-Z])/g, ' $1')}: ${value}`);

  const stats = [
    ['Available Venues', turfs?.length || 0],
    ['Sports Covered', sportsCount || '-'],
    ['Starting From', startingPrice ? `₹${startingPrice}` : '-'],
    ['Top Rating', topRating ? topRating.toFixed(1) : '-'],
  ];

  return (
    <div className="animate-fade-in space-y-8 pb-12">
      <section className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_26rem]">
        <div className="space-y-5">
          <div>
            <h1 className="text-4xl font-extrabold leading-tight tracking-tight text-primary-dark sm:text-5xl">
              Discover Venues
            </h1>
            <p className="mt-2 max-w-xl text-base font-medium text-gray-600">
              Find and book the best sports facilities near you.
            </p>
          </div>
          <TurfSearch onSearch={handleSearch} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          {stats.map(([label, value]) => (
            <div key={label} className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
              <p className="text-sm font-semibold text-gray-500">{label}</p>
              <p className="mt-4 text-3xl font-extrabold text-gray-950">{value}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-lg border border-primary/10 bg-white/55 p-4 shadow-sm">
        <div className="flex flex-col gap-4 border-b border-primary/10 pb-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-xs font-extrabold uppercase tracking-wider text-gray-500">Explore turfs</p>
            <h2 className="mt-1 text-2xl font-extrabold tracking-tight text-gray-950">
              {loading ? 'Finding venues...' : `${turfs?.length || 0} venue${turfs?.length === 1 ? '' : 's'} available`}
            </h2>
            {activeFilters.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {activeFilters.map((filter) => (
                  <span key={filter} className="rounded-full border border-primary/10 bg-primary-light px-3 py-1 text-xs font-extrabold uppercase tracking-wide text-primary-dark">
                    {filter}
                  </span>
                ))}
              </div>
            )}
          </div>

          <div className="inline-flex w-fit rounded-full border border-primary/10 bg-[#e3f0f8] p-1">
            <button type="button" className="rounded-full bg-white px-5 py-2 text-sm font-extrabold text-primary shadow-sm">
              List
            </button>
            <button type="button" className="px-5 py-2 text-sm font-semibold text-gray-600">
              Show Map
            </button>
          </div>
        </div>

        <div className="pt-8">
          <TurfList
            turfs={turfs}
            loading={loading}
            error={error}
            onViewSlots={handleViewDetails}
          />
        </div>
      </section>
    </div>
  );
};

export default DashboardPage;
