import React from 'react';
import TurfCard from './TurfCard';
import Skeleton from '../../components/ui/Skeleton';

const TurfList = ({ turfs, loading, error, onViewSlots }) => {
  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
        {Array.from({ length: 6 }).map((_, idx) => (
          <div key={idx} className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
            <Skeleton className="h-48 w-full mb-4 rounded-xl" />
            <Skeleton className="h-6 w-3/4 mb-2" />
            <Skeleton className="h-4 w-1/2 mb-4" />
            <Skeleton className="h-10 w-full" />
          </div>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-16 bg-red-50/50 rounded-3xl border border-red-100 shadow-sm">
        <div className="w-16 h-16 bg-red-100 text-red-500 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        </div>
        <h3 className="text-lg text-red-700 font-bold mb-1">Failed to load venues</h3>
        <p className="text-sm text-red-600/80">{error}</p>
      </div>
    );
  }

  if (!turfs || turfs.length === 0) {
    return (
      <div className="text-center py-20 bg-white rounded-3xl border border-dashed border-gray-200 shadow-sm">
        <div className="w-20 h-20 bg-gray-50 text-gray-400 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
        </div>
        <h3 className="text-xl text-gray-800 font-bold mb-2">No venues found</h3>
        <p className="text-base text-gray-500 max-w-sm mx-auto">Try adjusting your filters or searching in a different city to find available sports turfs.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8 animate-fade-in">
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
