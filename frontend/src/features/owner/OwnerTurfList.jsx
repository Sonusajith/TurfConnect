import React from 'react';

const OwnerTurfList = () => {
  return (
    <section className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-wider text-gray-500">Owner inventory</p>
          <h2 className="mt-1 text-xl font-extrabold tracking-tight text-gray-950">My Turfs</h2>
          <p className="mt-2 text-sm font-medium text-gray-500">
            Owner turf list, status controls, edit actions, and slot health can be built here.
          </p>
        </div>
        <button
          type="button"
          className="rounded-lg bg-accent px-5 py-3 text-sm font-extrabold text-white shadow-sm transition hover:bg-accent-dark"
        >
          Add Turf
        </button>
      </div>
    </section>
  );
};

export default OwnerTurfList;
