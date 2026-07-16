import React from 'react';
import Card from '../../components/Card';
import Button from '../../components/Button';
import { formatCurrency } from '../../utils/formatters';

const TurfCard = ({ turf, onViewSlots }) => {
  const { name, sportTypes, address, city, hourlyRate, currency, openTime, closeTime } = turf;

  return (
    <Card className="flex flex-col h-full hover:scale-[1.01] transition-transform">
      <div className="flex-1">
        {/* Placeholder image representation with sport types */}
        <div className="bg-primary-light rounded-xl h-40 flex items-center justify-center mb-4 text-primary font-bold text-lg capitalize border border-green-100">
          🥅 {sportTypes?.join(' / ') || 'Sports Turf'}
        </div>

        <div className="flex items-start justify-between">
          <h3 className="text-lg font-bold text-gray-900 line-clamp-1">{name}</h3>
          <span className="text-primary font-bold text-base">
            {formatCurrency(hourlyRate, currency)}/hr
          </span>
        </div>

        <p className="text-gray-500 text-sm mt-1 line-clamp-1">
          📍 {address}, {city}
        </p>

        <p className="text-gray-600 text-xs mt-2.5 font-medium">
          ⏰ Open: {openTime} - {closeTime}
        </p>

        <div className="flex flex-wrap gap-1.5 mt-3">
          {sportTypes?.map((sport) => (
            <span
              key={sport}
              className="px-2 py-0.5 bg-gray-100 text-gray-700 text-xs font-semibold rounded-full border"
            >
              {sport}
            </span>
          ))}
        </div>
      </div>

      <div className="mt-5">
        <Button
          variant="accent"
          className="w-full font-bold uppercase tracking-wider text-xs py-2.5"
          onClick={onViewSlots}
        >
          Book Now
        </Button>
      </div>
    </Card>
  );
};

export default TurfCard;
