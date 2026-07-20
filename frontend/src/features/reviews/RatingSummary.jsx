import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';

const RatingSummary = ({ reviews = [] }) => {
  if (!reviews.length) return null;

  const total = reviews.length;
  const average = (reviews.reduce((acc, curr) => acc + curr.rating, 0) / total).toFixed(1);
  const distribution = [5, 4, 3, 2, 1].map(stars => {
    const count = reviews.filter(r => r.rating === stars).length;
    return { stars, count, percentage: Math.round((count / total) * 100) };
  });

  return (
    <Card className="bg-white border border-gray-100 shadow-sm">
      <CardContent className="p-6 flex flex-col sm:flex-row gap-8 items-center">
        <div className="text-center flex-shrink-0">
          <div className="text-5xl font-extrabold text-gray-900">{average}</div>
          <div className="flex text-accent mt-2 justify-center">
            {[1,2,3,4,5].map(i => (
              <span key={i} className="material-symbols-outlined text-xl">
                {i <= Math.round(average) ? 'star' : 'star_border'}
              </span>
            ))}
          </div>
          <p className="text-sm text-gray-500 mt-1">{total} review{total !== 1 && 's'}</p>
        </div>
        
        <div className="flex-1 w-full space-y-2">
          {distribution.map(({ stars, percentage }) => (
            <div key={stars} className="flex items-center gap-3 text-sm font-semibold text-gray-600">
              <span className="w-4">{stars}</span>
              <span className="material-symbols-outlined text-sm text-gray-400">star</span>
              <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-accent rounded-full" style={{ width: `${percentage}%` }}></div>
              </div>
              <span className="w-8 text-right text-xs text-gray-400">{percentage}%</span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default RatingSummary;
