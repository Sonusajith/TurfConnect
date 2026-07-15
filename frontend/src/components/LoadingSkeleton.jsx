import React from 'react';

const LoadingSkeleton = ({ variant = 'card', className = '' }) => {
  if (variant === 'text') {
    return (
      <div className={`animate-pulse flex flex-col gap-2 ${className}`}>
        <div className="h-4 bg-gray-200 rounded w-3/4"></div>
        <div className="h-4 bg-gray-200 rounded w-1/2"></div>
      </div>
    );
  }

  if (variant === 'grid') {
    return (
      <div className={`grid grid-cols-2 md:grid-cols-4 gap-4 animate-pulse ${className}`}>
        {Array.from({ length: 8 }).map((_, idx) => (
          <div key={idx} className="h-16 bg-gray-200 rounded-lg"></div>
        ))}
      </div>
    );
  }

  return (
    <div className={`animate-pulse bg-white border border-gray-100 rounded-2xl p-5 flex flex-col gap-4 ${className}`}>
      <div className="h-40 bg-gray-200 rounded-xl"></div>
      <div className="h-6 bg-gray-200 rounded w-3/4"></div>
      <div className="h-4 bg-gray-200 rounded w-1/2"></div>
      <div className="flex gap-2">
        <div className="h-8 bg-gray-200 rounded w-16"></div>
        <div className="h-8 bg-gray-200 rounded w-16"></div>
      </div>
    </div>
  );
};

export default LoadingSkeleton;
