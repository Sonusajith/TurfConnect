import React from 'react';

const ReviewList = ({ reviews }) => {
  if (!reviews || reviews.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-on-surface-variant">No reviews yet. Be the first to leave one!</p>
      </div>
    );
  }

  const renderStars = (rating) => {
    return Array.from({ length: 5 }).map((_, i) => (
      <span key={i} className={`material-symbols-outlined text-lg ${i < rating ? 'text-[#F59E0B] fill-current' : 'text-outline-variant'}`}>
        star
      </span>
    ));
  };

  return (
    <div className="space-y-4">
      {reviews.map((review) => (
        <div key={review.id} className="p-4 border border-outline-variant/30 rounded-xl bg-surface-container-lowest">
          <div className="flex justify-between items-start mb-2">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-primary-container text-primary flex items-center justify-center font-bold text-sm">
                {review.userName?.charAt(0).toUpperCase() || 'A'}
              </div>
              <div>
                <p className="font-headline-md text-sm text-on-surface">{review.userName || 'Anonymous'}</p>
                <p className="text-xs text-on-surface-variant">{review.date}</p>
              </div>
            </div>
            <div className="flex gap-0.5">
              {renderStars(review.rating)}
            </div>
          </div>
          <p className="text-sm text-on-surface-variant mt-2">{review.comment}</p>
        </div>
      ))}
    </div>
  );
};

export default ReviewList;
