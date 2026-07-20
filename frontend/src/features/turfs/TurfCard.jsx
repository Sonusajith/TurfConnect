import React from 'react';
import { Card, CardContent, CardFooter } from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import { formatCurrency } from '../../utils/formatters';

const TurfCard = ({ turf, onViewSlots }) => {
  const {
    name,
    sportTypes,
    address,
    city,
    hourlyRate,
    currency,
    openTime,
    closeTime,
    coverImage,
    averageRating,
    totalReviews,
  } = turf;
  const rating = Number(averageRating || 4.8).toFixed(1);

  return (
    <Card interactive={true} className="group flex h-full flex-col rounded-lg border border-primary/10 bg-white shadow-sm hover:border-primary/20 hover:shadow-xl">
      <CardContent className="flex-1 p-0">
        <div className="relative h-48 overflow-hidden bg-gradient-to-br from-primary-dark via-primary to-secondary">
          {coverImage ? (
            <img
              src={coverImage}
              alt={`${name} turf`}
              className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
            />
          ) : (
            <div className="absolute inset-0 opacity-20 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZyI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjIiIGZpbGw9IiNmZmZmZmYiLz48L3N2Zz4=')] bg-[length:20px_20px]" />
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-black/55 via-black/10 to-transparent" />
          <div className="absolute left-4 top-4 flex gap-2">
            <Badge variant="primary" className="bg-white/90 font-bold text-primary-dark shadow-sm backdrop-blur-md">
              {`Rating ${rating}${totalReviews ? ` (${totalReviews})` : ''}`}
            </Badge>
          </div>
          <div className="absolute bottom-4 left-4 right-4">
            <h3 className="line-clamp-1 text-xl font-extrabold text-white drop-shadow-md">{name}</h3>
          </div>
        </div>

        <div className="p-5">
          <div className="mb-4 flex items-start justify-between gap-4">
            <p className="line-clamp-2 flex items-start gap-1 text-sm font-medium text-gray-500">
              <svg className="mt-0.5 h-4 w-4 shrink-0 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657 13.414 20.9a2 2 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z" />
              </svg>
              {address}, {city}
            </p>
            <div className="shrink-0 text-right">
              <span className="block text-lg font-extrabold leading-none text-primary-dark">
                {formatCurrency(hourlyRate, currency)}
              </span>
              <span className="text-[10px] font-semibold uppercase tracking-wider text-gray-400">per hr</span>
            </div>
          </div>

          <div className="mb-4 flex items-center gap-2 rounded-lg border border-gray-100 bg-gray-50 p-2.5 text-sm text-gray-600">
            <svg className="h-4 w-4 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" />
            </svg>
            <span className="font-medium">Open: {openTime} - {closeTime}</span>
          </div>

          <div className="flex flex-wrap gap-2">
            {sportTypes?.map((sport) => (
              <Badge key={sport} variant="default" className="border border-gray-200 bg-gray-100 text-gray-600">
                {sport}
              </Badge>
            ))}
          </div>
        </div>
      </CardContent>

      <CardFooter className="mt-auto border-t-0 bg-transparent p-5 pt-0">
        <Button
          variant="accent"
          fullWidth
          className="py-3 text-sm font-bold uppercase tracking-wide shadow-sm transition-all group-hover:bg-accent-dark"
          onClick={onViewSlots}
        >
          View Details
          <svg className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="m14 5 7 7m0 0-7 7m7-7H3" />
          </svg>
        </Button>
      </CardFooter>
    </Card>
  );
};

export default TurfCard;
