import React from 'react';
import { formatTime } from '../../utils/formatters';

const SlotCard = ({ slot, onSelect }) => {
  const { startTime, status, price } = slot;
  const isAvailable = status === 'AVAILABLE';

  const getStatusStyles = () => {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-green-50 border-green-200 text-green-700 hover:bg-green-100 hover:border-green-300';
      case 'LOCKED':
        return 'bg-yellow-50 border-yellow-200 text-yellow-700 cursor-not-allowed opacity-80';
      case 'BOOKED':
        return 'bg-red-50 border-red-200 text-red-700 cursor-not-allowed opacity-60';
      default:
        return 'bg-gray-50 border-gray-200 text-gray-500 cursor-not-allowed';
    }
  };

  return (
    <button
      disabled={!isAvailable}
      onClick={() => onSelect(slot)}
      className={`
        p-4 border rounded-xl flex flex-col items-center justify-center transition-all font-semibold select-none
        ${getStatusStyles()}
      `}
    >
      <span className="text-base font-bold">{formatTime(startTime)}</span>
      <span className="text-xs font-semibold mt-1">₹{price}</span>
      <span className="text-[10px] mt-1 uppercase tracking-wide px-1.5 py-0.5 rounded bg-white bg-opacity-70 border border-current font-bold">
        {status}
      </span>
    </button>
  );
};

export default SlotCard;
