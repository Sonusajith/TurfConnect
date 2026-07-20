import React from 'react';
import useBookings from '../hooks/useBookings';
import BookingTable from '../features/booking/BookingTable';

const BookingHistoryPage = () => {
  const { bookings, loading, error, cancelBooking, updateSplitContribution } = useBookings();
  const totalBookings = bookings?.length || 0;
  const confirmedBookings = bookings?.filter((booking) => booking.status === 'CONFIRMED').length || 0;

  return (
    <div className="space-y-6 pb-10">
      <section className="rounded-lg border border-primary/10 bg-white p-6 shadow-sm">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Booking desk</p>
            <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-gray-950">Your Booking History</h1>
            <p className="mt-2 max-w-2xl text-sm font-medium text-gray-500">
              Monitor your slot reservations, payment validations, and active booking statuses.
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3 sm:w-80">
            <div className="rounded-lg border border-primary/10 bg-[#f4faff] p-4">
              <p className="text-xs font-bold uppercase tracking-wide text-gray-500">Total</p>
              <p className="mt-2 text-2xl font-extrabold text-gray-950">{totalBookings}</p>
            </div>
            <div className="rounded-lg border border-primary/10 bg-[#f4faff] p-4">
              <p className="text-xs font-bold uppercase tracking-wide text-gray-500">Confirmed</p>
              <p className="mt-2 text-2xl font-extrabold text-primary-dark">{confirmedBookings}</p>
            </div>
          </div>
        </div>
      </section>

      <BookingTable
        bookings={bookings}
        loading={loading}
        error={error}
        onCancelBooking={cancelBooking}
        onUpdateSplitContribution={updateSplitContribution}
      />
    </div>
  );
};

export default BookingHistoryPage;
