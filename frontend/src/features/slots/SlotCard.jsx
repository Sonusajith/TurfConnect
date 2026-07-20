import React from 'react';
import { formatCurrency, formatTime } from '../../utils/formatters';

const SlotCard = ({ slot, onSelect }) => {
  const { startTime, endTime, status, price } = slot;
  const isAvailable = status === 'AVAILABLE';

  const getStatusStyles = () => {
    switch (status) {
      case 'AVAILABLE':
        return 'border-primary/20 bg-white text-primary-dark hover:-translate-y-0.5 hover:border-primary hover:shadow-lg hover:ring-2 hover:ring-primary/15';
      case 'LOCKED':
        return 'border-yellow-200 bg-yellow-50 text-yellow-800 opacity-80';
      case 'BOOKED':
        return 'border-gray-200 bg-gray-100 text-gray-500 opacity-70';
      default:
        return 'border-gray-200 bg-gray-50 text-gray-400 opacity-70';
    }
  };

  return (
    <button
      disabled={!isAvailable}
      onClick={() => onSelect(slot)}
      className={`relative flex min-h-24 flex-col items-start justify-between rounded-lg border p-4 text-left transition-all duration-200 disabled:cursor-not-allowed ${getStatusStyles()}`}
    >
      <span className="text-lg font-extrabold tracking-tight">{formatTime(startTime)}</span>
      <span className="text-xs font-semibold text-gray-500">{formatTime(endTime)}</span>

      <div className="mt-3 flex w-full items-center justify-between gap-2">
        <span className="text-sm font-extrabold">{formatCurrency(price)}</span>
        <span className={`rounded-full px-2 py-1 text-[10px] font-extrabold uppercase tracking-wider ${
          status === 'AVAILABLE'
            ? 'bg-primary-light text-primary-dark'
            : status === 'LOCKED'
              ? 'bg-yellow-200 text-yellow-900'
              : 'bg-gray-200 text-gray-600'
        }`}>
          {status}
        </span>
      </div>

      {isAvailable && (
        <span className="absolute right-3 top-3 flex h-2.5 w-2.5">
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-green-400 opacity-75"></span>
          <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-green-500"></span>
        </span>
      )}
    </button>
  );
};

export default SlotCard;
