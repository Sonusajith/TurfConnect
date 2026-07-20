import React, { useState, useEffect } from 'react';
import useReviews from '../hooks/useReviews';
import { useTurfs } from '../hooks/useTurfs';
import { useAuth } from '../hooks/useAuth';
import useOwner from '../hooks/useOwner';
import useBookings from '../hooks/useBookings';
import Select from '../components/ui/Select';
import ReviewList from '../features/reviews/ReviewList';
import RatingSummary from '../features/reviews/RatingSummary';
import ReviewFormModal from '../features/reviews/ReviewFormModal';

const ReviewsPage = ({ turfId }) => {
  const { user } = useAuth();
  const isOwner = user?.role === 'TURF_OWNER' || user?.role === 'SUPER_ADMIN' || user?.role === 'ORG_ADMIN';
  
  const { turfs: publicTurfs, loading: publicTurfsLoading } = useTurfs();
  const { turfs: ownerTurfs, loading: ownerTurfsLoading, fetchDashboardData } = useOwner();
  const { bookings } = useBookings();
  
  const turfs = isOwner ? ownerTurfs : publicTurfs;
  const turfsLoading = isOwner ? ownerTurfsLoading : publicTurfsLoading;

  const [selectedTurfId, setSelectedTurfId] = useState(turfId || '');
  const activeTurfId = turfId || selectedTurfId;
  const { reviews, loading, error, fetchReviews, submitReview } = useReviews(activeTurfId);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Find a valid booking for the selected turf so the player can review it
  const eligibleBooking = bookings?.find(b => b.turfId === activeTurfId && b.status === 'CONFIRMED');

  useEffect(() => {
    if (isOwner) {
      fetchDashboardData();
    }
  }, [isOwner, fetchDashboardData]);

  useEffect(() => {
    if (!selectedTurfId && turfs?.length) {
      setSelectedTurfId(turfs[0].id);
    }
  }, [selectedTurfId, turfs]);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews, activeTurfId]);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">Reviews & Ratings</h2>
        {!isOwner && (
          <div className="relative group">
            <button
              disabled={!activeTurfId || !eligibleBooking}
              onClick={() => setIsModalOpen(true)}
              className="bg-accent hover:bg-accent-dark text-white px-4 py-2 rounded-lg font-semibold transition disabled:opacity-50"
            >
              Write a Review
            </button>
            {!eligibleBooking && activeTurfId && (
              <div className="absolute top-full right-0 mt-2 w-64 bg-gray-800 text-white text-xs rounded p-2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10">
                You must have a completed booking at this venue to write a review.
              </div>
            )}
          </div>
        )}
      </div>

      {!turfId && (
        <div className="rounded-xl border border-primary/10 bg-white p-4 shadow-sm">
          <label htmlFor="reviewTurf" className="block text-xs font-extrabold uppercase tracking-wide text-gray-500">
            Review venue
          </label>
          <Select
            value={selectedTurfId}
            onChange={setSelectedTurfId}
            placeholder={turfsLoading ? "Loading venues..." : (turfs?.length ? "Select a venue" : "No venues available")}
            options={turfs?.map(turf => ({ value: turf.id, label: turf.name })) || []}
            buttonClassName="mt-2 h-11 border-primary/15 bg-[#f4faff]"
          />
        </div>
      )}
      
      <RatingSummary reviews={reviews} />
      
      <ReviewList reviews={reviews} loading={loading} error={error} />
      
      <ReviewFormModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onSubmit={submitReview} 
        bookingId={eligibleBooking?.id}
      />
    </div>
  );
};

export default ReviewsPage;
