import React from 'react';
import RatingSummary from '../features/reviews/RatingSummary';
import ReviewList from '../features/reviews/ReviewList';
import ReviewFormModal from '../features/reviews/ReviewFormModal';

const ReviewsPage = () => {
  return (
    <div className="space-y-6 pb-10">
      <section>
        <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Player trust</p>
        <h1 className="mt-1 text-4xl font-extrabold tracking-tight text-primary-dark">Reviews & Ratings</h1>
        <p className="mt-2 max-w-2xl text-sm font-medium text-gray-600">
          Skeleton route for review summaries, lists, and post-booking review submission.
        </p>
      </section>

      <RatingSummary />
      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_24rem]">
        <ReviewList />
        <ReviewFormModal />
      </div>
    </div>
  );
};

export default ReviewsPage;
