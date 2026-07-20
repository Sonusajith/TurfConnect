import React, { useState } from 'react';
import Modal from '../../components/Modal';
import { useToast } from '../../hooks/useToast';

const ReviewFormModal = ({ isOpen, onClose, onSubmit, bookingId }) => {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { addToast } = useToast();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      addToast('Please select a rating', 'error');
      return;
    }
    if (!bookingId) {
      addToast('Booking ID is missing', 'error');
      return;
    }
    setIsSubmitting(true);
    try {
      await onSubmit({ bookingId, rating, comment });
      addToast('Review submitted successfully!', 'success');
      setRating(0);
      setComment('');
      onClose();
    } catch (err) {
      addToast(err.message || 'Failed to submit review', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Leave a Review">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Rating</label>
          <div className="flex gap-2">
            {[1, 2, 3, 4, 5].map(star => (
              <button 
                key={star} 
                type="button" 
                onClick={() => setRating(star)}
                className={`material-symbols-outlined text-3xl transition-colors ${rating >= star ? 'text-accent' : 'text-gray-300 hover:text-accent/50'}`}
              >
                star
              </button>
            ))}
          </div>
        </div>
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Comment</label>
          <textarea
            required
            className="w-full border border-gray-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-primary focus:outline-none"
            rows="4"
            placeholder="Share your experience..."
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          ></textarea>
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <button type="button" onClick={onClose} disabled={isSubmitting} className="px-4 py-2 font-bold text-gray-600 hover:bg-gray-50 rounded-lg disabled:opacity-50">Cancel</button>
          <button type="submit" disabled={isSubmitting || rating === 0} className="px-4 py-2 font-bold text-white bg-primary hover:bg-primary-dark rounded-lg disabled:opacity-50 flex items-center gap-2">
            {isSubmitting ? <><span className="material-symbols-outlined animate-spin text-sm">progress_activity</span> Submitting</> : 'Submit Review'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default ReviewFormModal;
