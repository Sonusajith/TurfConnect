import React from 'react';

const Skeleton = ({ className = '', variant = 'rectangular', ...props }) => {
  const variants = {
    rectangular: 'rounded-lg',
    circular: 'rounded-full',
    text: 'rounded-md h-4'
  };

  return (
    <div
      className={`animate-pulse bg-gray-200 ${variants[variant]} ${className}`}
      {...props}
    />
  );
};

export default Skeleton;
