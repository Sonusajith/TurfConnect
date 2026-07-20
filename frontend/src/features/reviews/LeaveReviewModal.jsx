import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';

const LeaveReviewModal = ({ isOpen, onClose, onSubmit, turfName }) => {
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) return;
    
    setIsSubmitting(true);
    try {
      await onSubmit({ rating, comment });
      onClose();
      setRating(0);
      setComment('');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Leave a Review">
      <form onSubmit={handleSubmit} className="space-y-6">
        <p className="text-sm text-on-surface-variant">How was your experience playing at <span className="font-bold text-on-surface">{turfName}</span>?</p>
        
        <div className="flex flex-col items-center gap-2">
          <div className="flex gap-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                className={`material-symbols-outlined text-4xl transition-colors ${
                  star <= (hoverRating || rating) ? 'text-[#F59E0B] fill-current drop-shadow-sm' : 'text-outline-variant'
                }`}
                onMouseEnter={() => setHoverRating(star)}
                onMouseLeave={() => setHoverRating(0)}
                onClick={() => setRating(star)}
              >
                star
              </button>
            ))}
          </div>
          <span className="text-xs font-label-md text-on-surface-variant uppercase tracking-wider">
            {rating === 0 ? 'Select a rating' : ['Poor', 'Fair', 'Good', 'Very Good', 'Excellent'][rating - 1]}
          </span>
        </div>

        <div>
          <label className="block text-sm font-medium text-on-surface mb-2">Your Feedback (Optional)</label>
          <textarea
            rows={4}
            className="w-full px-4 py-3 rounded-lg border border-outline bg-surface-container-lowest text-on-surface focus:ring-2 focus:ring-primary focus:border-transparent transition-all outline-none resize-none"
            placeholder="Tell us what you liked or how they can improve..."
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          />
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-outline-variant/30">
          <Button variant="outline" type="button" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button variant="primary" type="submit" isLoading={isSubmitting} disabled={rating === 0}>
            Submit Review
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default LeaveReviewModal;
