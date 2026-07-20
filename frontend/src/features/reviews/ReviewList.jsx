import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';

const ReviewList = ({ reviews, loading, error }) => {
  if (loading) return <div className="text-gray-500 animate-pulse">Loading reviews...</div>;
  if (error) return <div className="text-red-500">Error loading reviews: {error}</div>;
  if (!reviews || reviews.length === 0) {
    return (
      <div className="text-center py-8 bg-white rounded-xl border border-gray-100">
        <p className="text-gray-500 font-medium">No reviews yet. Be the first to leave one!</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {reviews.map((review, i) => (
        <Card key={review.id || i} className="border border-outline-variant/30">
          <CardContent className="p-4">
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-full bg-primary-light text-primary flex items-center justify-center font-bold">
                  {(review.userName || 'U')[0].toUpperCase()}
                </div>
                <div>
                  <p className="font-semibold text-gray-900">{review.userName || 'User'}</p>
                  <p className="text-xs text-gray-500">{new Date(review.createdAt).toLocaleDateString()}</p>
                </div>
              </div>
              <div className="flex items-center gap-1 text-accent">
                <span className="material-symbols-outlined text-sm">star</span>
                <span className="font-bold">{review.rating}</span>
              </div>
            </div>
            <p className="text-gray-700 text-sm mt-2">{review.comment}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default ReviewList;
