import React, { useState, useEffect } from 'react';
import useReviews from '../hooks/useReviews';
import ReviewList from '../features/reviews/ReviewList';
import RatingSummary from '../features/reviews/RatingSummary';
import ReviewFormModal from '../features/reviews/ReviewFormModal';

const ReviewsPage = ({ turfId }) => {
  const { reviews, loading, error, fetchReviews, submitReview } = useReviews(turfId);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">Reviews & Ratings</h2>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="bg-accent hover:bg-accent-dark text-white px-4 py-2 rounded-lg font-semibold transition"
        >
          Write a Review
        </button>
      </div>
      
      <RatingSummary reviews={reviews} />
      
      <ReviewList reviews={reviews} loading={loading} error={error} />
      
      <ReviewFormModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onSubmit={submitReview} 
      />
    </div>
  );
};

export default ReviewsPage;
