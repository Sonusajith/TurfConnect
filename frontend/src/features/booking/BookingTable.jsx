import React from 'react';
import Badge from '../../components/Badge';
import LoadingSkeleton from '../../components/LoadingSkeleton';
import { formatCurrency, formatTime, formatDate } from '../../utils/formatters';

const BookingTable = ({ bookings, loading, error }) => {
  if (loading) {
    return (
      <div className="flex flex-col gap-4 mt-6">
        <LoadingSkeleton variant="text" />
        <LoadingSkeleton variant="text" />
        <LoadingSkeleton variant="text" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8 bg-red-50 rounded-2xl border border-red-100 mt-6">
        <p className="text-red-600 font-semibold mb-2">Failed to load bookings</p>
        <p className="text-sm text-red-500">{error}</p>
      </div>
    );
  }

  if (!bookings || bookings.length === 0) {
    return (
      <div className="text-center py-12 bg-gray-50 rounded-2xl border border-dashed border-gray-200 mt-6">
        <p className="text-gray-500 font-medium text-lg">No bookings yet</p>
        <p className="text-sm text-gray-400 mt-1">Once you complete a booking, it will show up here.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto mt-6 bg-white rounded-2xl border border-gray-100 shadow-sm">
      <table className="min-w-full divide-y divide-gray-150">
        <thead className="bg-gray-50">
          <tr>
            <th scope="col" className="px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">
              Booking ID
            </th>
            <th scope="col" className="px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">
              Date
            </th>
            <th scope="col" className="px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">
              Time Slot
            </th>
            <th scope="col" className="px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">
              Price
            </th>
            <th scope="col" className="px-6 py-3.5 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">
              Status
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-100">
          {bookings.map((booking) => (
            <tr key={booking.id} className="hover:bg-gray-50 transition-colors">
              <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900 font-mono">
                {booking.id}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                {formatDate(booking.date)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                {formatCurrency(booking.totalPrice)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
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
