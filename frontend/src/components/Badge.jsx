import React from 'react';

const Badge = ({ status, children, className = '' }) => {
  const getBadgeStyles = (statusVal) => {
    const s = String(statusVal).toUpperCase();
    switch (s) {
      case 'AVAILABLE':
      case 'SUCCESS':
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'PENDING':
      case 'LOCKED':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'FAILED':
      case 'CANCELLED':
      case 'DELETED':
        return 'bg-red-100 text-red-800 border-red-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  return (
    <span
      className={`
        inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border
        ${getBadgeStyles(status)}
        ${className}
      `}
    >
      {children || status}
    </span>
  );
};

export default Badge;
