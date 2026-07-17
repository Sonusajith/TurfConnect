import React from 'react';
import SlotCard from './SlotCard';
import Skeleton from '../../components/ui/Skeleton';

const SlotGrid = ({ slots, loading, error, onSelectSlot }) => {
  if (loading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 mt-6">
        {Array.from({ length: 10 }).map((_, idx) => (
          <Skeleton key={idx} className="h-20 w-full rounded-xl" />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-10 bg-red-50/50 rounded-2xl border border-red-100 mt-6 shadow-sm">
        <p className="text-red-700 font-bold mb-1">Failed to load slots</p>
        <p className="text-sm text-red-600/80">{error}</p>
      </div>
    );
  }

  if (!slots || slots.length === 0) {
    return (
      <div className="text-center py-16 bg-gray-50/80 rounded-3xl border border-dashed border-gray-200 mt-6">
        <p className="text-gray-600 font-bold text-lg mb-1">No slots available</p>
        <p className="text-sm text-gray-500">Please select another date or check back later.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 mt-6 animate-fade-in">
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
