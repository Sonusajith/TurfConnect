import React from 'react';
import TurfCard from './TurfCard';
import LoadingSkeleton from '../../components/LoadingSkeleton';

const TurfList = ({ turfs, loading, error, onViewSlots }) => {
  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: 6 }).map((_, idx) => (
          <LoadingSkeleton key={idx} />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12 bg-red-50 rounded-2xl border border-red-100">
        <p className="text-red-600 font-semibold mb-2">Failed to load turfs</p>
        <p className="text-sm text-red-500">{error}</p>
      </div>
    );
  }

  if (!turfs || turfs.length === 0) {
    return (
      <div className="text-center py-16 bg-gray-50 rounded-2xl border border-dashed border-gray-200">
        <p className="text-gray-500 font-medium text-lg">No turfs found</p>
        <p className="text-sm text-gray-400 mt-1">Try resetting your search filters or try a different city.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {turfs.map((turf) => (
        <TurfCard
          key={turf.id}
          turf={turf}
          onViewSlots={() => onViewSlots(turf.id)}
        />
      ))}
    </div>
  );
};

export default TurfList;
