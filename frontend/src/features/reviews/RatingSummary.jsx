import React from 'react';

const RatingSummary = () => {
  return (
    <section className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Ratings</p>
          <h2 className="mt-1 text-2xl font-extrabold tracking-tight text-gray-950">Venue Rating Summary</h2>
          <p className="mt-2 text-sm font-medium text-gray-500">
            Aggregate rating cards will connect to review summary APIs here.
          </p>
        </div>
        <div className="rounded-lg border border-primary/10 bg-primary-light px-5 py-4 text-right">
          <p className="text-xs font-extrabold uppercase tracking-wide text-gray-500">Average</p>
          <p className="mt-1 text-3xl font-extrabold text-primary-dark">--</p>
        </div>
      </div>
    </section>
  );
};

export default RatingSummary;
