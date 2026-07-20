import React from 'react';
import Badge from '../../components/Badge';
import LoadingSkeleton from '../../components/LoadingSkeleton';
import { formatCurrency, formatTime, formatDate } from '../../utils/formatters';

const BookingTable = ({ bookings, loading, error }) => {
  if (loading) {
    return (
      <div className="mt-6 rounded-lg border border-primary/10 bg-white p-6 shadow-sm">
        <div className="flex flex-col gap-4">
          <LoadingSkeleton variant="text" />
          <LoadingSkeleton variant="text" />
          <LoadingSkeleton variant="text" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mt-6 rounded-lg border border-red-100 bg-red-50 py-8 text-center">
        <p className="mb-2 font-extrabold text-red-600">Failed to load bookings</p>
        <p className="text-sm font-medium text-red-500">{error}</p>
      </div>
    );
  }

  if (!bookings || bookings.length === 0) {
    return (
      <div className="mt-6 rounded-lg border border-dashed border-primary/20 bg-white py-12 text-center shadow-sm">
        <p className="text-lg font-extrabold text-gray-600">No bookings yet</p>
        <p className="mt-1 text-sm font-medium text-gray-400">Once you complete a booking, it will show up here.</p>
      </div>
    );
  }

  return (
    <div className="mt-6 overflow-x-auto rounded-lg border border-primary/10 bg-white shadow-sm">
      <table className="min-w-full divide-y divide-primary/10">
        <thead className="bg-[#f4faff]">
          <tr>
            {['Booking ID', 'Date', 'Time Slot', 'Price', 'Status'].map((heading) => (
              <th key={heading} scope="col" className="px-6 py-4 text-left text-xs font-extrabold uppercase tracking-wider text-gray-500">
                {heading}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-primary/10 bg-white">
          {bookings.map((booking) => (
            <tr key={booking.id} className="transition-colors hover:bg-[#f4faff]">
              <td className="whitespace-nowrap px-6 py-4 font-mono text-sm font-bold text-gray-900">
                {booking.id}
              </td>
              <td className="whitespace-nowrap px-6 py-4 text-sm font-semibold text-gray-600">
                {formatDate(booking.date)}
              </td>
              <td className="whitespace-nowrap px-6 py-4 text-sm font-semibold text-gray-600">
                {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
              </td>
              <td className="whitespace-nowrap px-6 py-4 text-sm font-extrabold text-gray-900">
                {formatCurrency(booking.totalPrice)}
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <Badge status={booking.status} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default BookingTable;
