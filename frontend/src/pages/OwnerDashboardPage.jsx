import React from 'react';
import OwnerStats from '../features/owner/OwnerStats';
import OwnerTurfList from '../features/owner/OwnerTurfList';

const OwnerDashboardPage = () => {
  return (
    <div className="space-y-6 pb-10">
      <section>
        <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Owner console</p>
        <h1 className="mt-1 text-4xl font-extrabold tracking-tight text-primary-dark">Owner Dashboard</h1>
        <p className="mt-2 max-w-2xl text-sm font-medium text-gray-600">
          Skeleton route for turf-owner inventory, analytics, and venue management.
        </p>
      </section>

      <OwnerStats />
      <OwnerTurfList />
    </div>
  );
};

export default OwnerDashboardPage;
