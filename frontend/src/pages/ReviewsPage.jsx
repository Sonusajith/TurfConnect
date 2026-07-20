import React, { useState, useEffect } from 'react';
import useReviews from '../hooks/useReviews';
import { useTurfs } from '../hooks/useTurfs';
import ReviewList from '../features/reviews/ReviewList';
import RatingSummary from '../features/reviews/RatingSummary';
import ReviewFormModal from '../features/reviews/ReviewFormModal';

const ReviewsPage = ({ turfId }) => {
  const { turfs, loading: turfsLoading } = useTurfs();
  const [selectedTurfId, setSelectedTurfId] = useState(turfId || '');
  const activeTurfId = turfId || selectedTurfId;
  const { reviews, loading, error, fetchReviews, submitReview } = useReviews(activeTurfId);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    if (!selectedTurfId && turfs?.length) {
      setSelectedTurfId(turfs[0].id);
    }
  }, [selectedTurfId, turfs]);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">Reviews & Ratings</h2>
        <button
          disabled={!activeTurfId}
          onClick={() => setIsModalOpen(true)}
          className="bg-accent hover:bg-accent-dark text-white px-4 py-2 rounded-lg font-semibold transition disabled:opacity-50"
        >
          Write a Review
        </button>
      </div>

      {!turfId && (
        <div className="rounded-xl border border-primary/10 bg-white p-4 shadow-sm">
          <label htmlFor="reviewTurf" className="block text-xs font-extrabold uppercase tracking-wide text-gray-500">
            Review venue
          </label>
          <select
            id="reviewTurf"
            value={selectedTurfId}
            onChange={(event) => setSelectedTurfId(event.target.value)}
            disabled={turfsLoading || !turfs?.length}
            className="mt-2 h-11 w-full rounded-lg border border-primary/15 bg-[#f4faff] px-3 text-sm font-bold text-gray-900 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
          >
            {turfsLoading && <option>Loading venues...</option>}
            {!turfsLoading && !turfs?.length && <option>No venues available</option>}
            {turfs?.map((turf) => (
              <option key={turf.id} value={turf.id}>{turf.name}</option>
            ))}
          </select>
        </div>
      )}
      
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
