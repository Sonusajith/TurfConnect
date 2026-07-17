import React from 'react';
import { formatTime } from '../../utils/formatters';

const SlotCard = ({ slot, onSelect }) => {
  const { startTime, status, price } = slot;
  const isAvailable = status === 'AVAILABLE';

  const getStatusStyles = () => {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-white border-green-200 text-green-800 hover:border-primary hover:shadow-lg hover:-translate-y-1 hover:ring-2 hover:ring-primary/20 shadow-sm cursor-pointer';
      case 'LOCKED':
        return 'bg-yellow-50/50 border-yellow-200 text-yellow-700 cursor-not-allowed opacity-80';
      case 'BOOKED':
        return 'bg-gray-100 border-gray-200 text-gray-500 cursor-not-allowed opacity-60';
      default:
        return 'bg-gray-50 border-gray-200 text-gray-400 cursor-not-allowed';
    }
  };

  return (
    <button
      disabled={!isAvailable}
      onClick={() => onSelect(slot)}
      className={`
        relative p-4 border-2 rounded-2xl flex flex-col items-center justify-center transition-all duration-300 font-semibold select-none
        ${getStatusStyles()}
      `}
    >
      {/* Time */}
      <span className="text-xl font-extrabold tracking-tight mb-1">{formatTime(startTime)}</span>
      
      {/* Price */}
      <span className="text-sm font-bold opacity-90 mb-3">₹{price}</span>
      
      {/* Status Badge */}
      <span className={`text-[10px] uppercase tracking-wider px-2 py-1 rounded-md font-bold ${
        status === 'AVAILABLE' ? 'bg-primary-light text-primary-dark' :
        status === 'LOCKED' ? 'bg-yellow-200 text-yellow-800' :
        'bg-gray-200 text-gray-600'
      }`}>
        {status}
      </span>
      
      {/* Active Indicator (subtle glowing dot) */}
      {isAvailable && (
        <span className="absolute top-3 right-3 flex h-2.5 w-2.5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
          <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-green-500"></span>
        </span>
      )}
    </button>
  );
};

export default SlotCard;
