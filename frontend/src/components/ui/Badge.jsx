import React from 'react';

const Badge = ({ children, variant = 'default', className = '' }) => {
  const baseStyles = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium';
  
  const variants = {
    default: 'bg-gray-100 text-gray-800',
    primary: 'bg-primary-light text-primary-dark',
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    danger: 'bg-red-100 text-red-800',
    accent: 'bg-accent-light text-accent-dark'
  };

  const variantClass = variants[variant] || variants.default;

  return (
    <span className={`${baseStyles} ${variantClass} ${className}`}>
      {children}
    </span>
  );
};

export default Badge;
