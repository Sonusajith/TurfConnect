import React from 'react';
import SlotCard from './SlotCard';
import Skeleton from '../../components/ui/Skeleton';

const SlotGrid = ({ slots, loading, error, onSelectSlot }) => {
  if (loading) {
    return (
      <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        {Array.from({ length: 10 }).map((_, idx) => (
          <Skeleton key={idx} className="h-24 w-full rounded-lg" />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="mt-6 rounded-lg border border-red-100 bg-red-50/70 py-10 text-center shadow-sm">
        <p className="mb-1 font-extrabold text-red-700">Failed to load slots</p>
        <p className="text-sm font-medium text-red-600/80">{error}</p>
      </div>
    );
  }

  if (!slots || slots.length === 0) {
    return (
      <div className="mt-6 rounded-lg border border-dashed border-primary/20 bg-[#f4faff] py-16 text-center">
        <p className="mb-1 text-lg font-extrabold text-gray-700">No slots available</p>
        <p className="text-sm font-medium text-gray-500">Please select another date or check back later.</p>
      </div>
    );
  }

  return (
    <div className="mt-6 grid animate-fade-in grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
      {slots.map((slot) => (
        <SlotCard
          key={slot.id}
          slot={slot}
          onSelect={onSelectSlot}
        />
      ))}
    </div>
  );
};

export default SlotGrid;
