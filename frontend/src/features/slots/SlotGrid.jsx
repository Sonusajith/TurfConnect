import React from 'react';
import SlotCard from './SlotCard';
import LoadingSkeleton from '../../components/LoadingSkeleton';

const SlotGrid = ({ slots, loading, error, onSelectSlot }) => {
  if (loading) {
    return <LoadingSkeleton variant="grid" className="mt-6" />;
  }

  if (error) {
    return (
      <div className="text-center py-8 bg-red-50 rounded-2xl border border-red-100 mt-6">
        <p className="text-red-600 font-semibold mb-2">Failed to load slots</p>
        <p className="text-sm text-red-500">{error}</p>
      </div>
    );
  }

  if (!slots || slots.length === 0) {
    return (
      <div className="text-center py-12 bg-gray-50 rounded-2xl border border-dashed border-gray-200 mt-6">
        <p className="text-gray-500 font-medium text-lg">No slots generated for this date</p>
        <p className="text-sm text-gray-400 mt-1">Please select another date or contact support.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 mt-6">
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
