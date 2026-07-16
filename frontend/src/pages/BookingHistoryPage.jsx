import React from 'react';
import useBookings from '../hooks/useBookings';
import BookingTable from '../features/booking/BookingTable';

const BookingHistoryPage = () => {
  const { bookings, loading, error } = useBookings();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Your Booking History</h1>
        <p className="text-sm text-gray-500 mt-1">
          Monitor your slot reservations, payment validations, and active booking statuses.
        </p>
      </div>

      <BookingTable
        bookings={bookings}
        loading={loading}
        error={error}
      />
    </div>
  );
};

export default BookingHistoryPage;
