import React from 'react';
import { Card, CardContent, CardFooter } from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import { formatCurrency } from '../../utils/formatters';

const TurfCard = ({ turf, onViewSlots }) => {
  const { name, sportTypes, address, city, hourlyRate, currency, openTime, closeTime } = turf;

  return (
    <Card interactive={true} className="flex flex-col h-full group bg-white border border-gray-100/80 shadow-sm hover:shadow-xl hover:border-primary/20">
      <CardContent className="flex-1 p-0">
        {/* Hero Image Section */}
        <div className="relative h-48 bg-gradient-to-br from-primary-dark via-primary to-secondary overflow-hidden">
          <div className="absolute inset-0 opacity-20 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjIiIGZpbGw9IiNmZmZmZmYiLz48L3N2Zz4=')] bg-[length:20px_20px]"></div>
          <div className="absolute top-4 left-4 flex gap-2">
            <Badge variant="primary" className="shadow-sm backdrop-blur-md bg-white/90 text-primary-dark font-bold">
              ★ 4.8
            </Badge>
          </div>
          <div className="absolute bottom-4 left-4 right-4 flex justify-between items-end">
            <h3 className="text-xl font-extrabold text-white drop-shadow-md line-clamp-1">{name}</h3>
          </div>
        </div>

        {/* Content Section */}
        <div className="p-5">
          <div className="flex justify-between items-start mb-4">
            <p className="text-gray-500 text-sm font-medium line-clamp-1 flex items-center gap-1">
              <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
              {address}, {city}
            </p>
            <div className="text-right">
              <span className="block text-lg font-extrabold text-primary-dark leading-none">
                {formatCurrency(hourlyRate, currency)}
              </span>
              <span className="text-[10px] text-gray-400 font-semibold uppercase tracking-wider">Per Hour</span>
            </div>
          </div>

          <div className="flex items-center gap-2 mb-4 text-sm text-gray-600 bg-gray-50 p-2.5 rounded-lg border border-gray-100">
            <svg className="w-4 h-4 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <span className="font-medium">Open: {openTime} - {closeTime}</span>
          </div>

          <div className="flex flex-wrap gap-2">
            {sportTypes?.map((sport) => (
              <Badge key={sport} variant="default" className="bg-gray-100 text-gray-600 border border-gray-200">
                {sport}
              </Badge>
            ))}
          </div>
        </div>
      </CardContent>

      <CardFooter className="p-5 pt-0 bg-transparent border-t-0 mt-auto">
        <Button
          variant="accent"
          fullWidth
          className="group-hover:bg-accent-dark shadow-sm transition-all py-3 font-bold text-sm tracking-wide uppercase"
          onClick={onViewSlots}
        >
          Check Availability
          <svg className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
        </Button>
      </CardFooter>
    </Card>
  );
};

export default TurfCard;
