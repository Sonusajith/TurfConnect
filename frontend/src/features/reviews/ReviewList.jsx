import React from 'react';

const ReviewList = ({ reviews }) => {
  if (!reviews || reviews.length === 0) {
    return (
      <div className="rounded-lg border border-primary/10 bg-white p-5 text-center shadow-sm">
        <p className="text-sm font-semibold text-gray-500">No reviews yet. Be the first to leave one.</p>
      </div>
    );
  }

  const renderStars = (rating) => {
    return Array.from({ length: 5 }).map((_, i) => (
      <span key={i} className={`text-lg ${i < rating ? 'text-[#F59E0B]' : 'text-gray-300'}`}>
        star
      </span>
    ));
  };

  return (
    <div className="space-y-4">
      {reviews.map((review) => (
        <div key={review.id} className="rounded-lg border border-primary/10 bg-white p-4 shadow-sm">
          <div className="mb-2 flex items-start justify-between gap-3">
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm font-bold text-white">
                {review.userName?.charAt(0).toUpperCase() || 'A'}
              </div>
              <div>
                <p className="text-sm font-extrabold text-gray-950">{review.userName || 'Anonymous'}</p>
                <p className="text-xs font-medium text-gray-500">{review.date}</p>
              </div>
            </div>
            <div className="flex gap-0.5">{renderStars(review.rating)}</div>
          </div>
          <p className="mt-2 text-sm font-medium text-gray-500">{review.comment}</p>
        </div>
      ))}
    </div>
  );
};

export default ReviewList;
