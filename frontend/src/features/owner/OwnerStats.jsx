import React from 'react';

const stats = [
  ['Revenue', '--'],
  ['Occupancy', '--'],
  ['Active Turfs', '--'],
  ['Bookings', '--'],
];

const OwnerStats = () => {
  return (
    <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {stats.map(([label, value]) => (
        <div key={label} className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
          <p className="text-xs font-extrabold uppercase tracking-wide text-gray-500">{label}</p>
          <p className="mt-4 text-3xl font-extrabold text-primary-dark">{value}</p>
        </div>
      ))}
    </section>
  );
};

export default OwnerStats;
