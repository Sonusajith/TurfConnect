import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import { formatCurrency, formatDate, formatTime } from '../../utils/formatters';

const shortPlayerId = (userId) => userId ? `Player ${userId.slice(-4).toUpperCase()}` : 'Player';

const OwnerRecentBookings = ({ bookings, loading, error }) => {
  if (loading) return <div className="font-medium text-gray-500">Loading recent bookings...</div>;
  if (error) return <div className="font-medium text-red-500">Error: {error}</div>;

  const recentBookings = bookings?.slice(0, 8) || [];

  if (recentBookings.length === 0) {
    return (
      <Card className="border border-outline-variant/30">
        <CardContent className="py-10 text-center">
          <span className="material-symbols-outlined mb-2 text-4xl text-gray-300">event_busy</span>
          <p className="font-bold text-gray-700">No customer bookings yet</p>
          <p className="mt-1 text-sm font-medium text-gray-500">Confirmed and pending bookings for your venues will appear here.</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="overflow-hidden border border-outline-variant/30">
      <CardContent className="p-0">
        <div className="divide-y divide-gray-100">
          {recentBookings.map((booking) => {
            const customerName = booking.userName || shortPlayerId(booking.userId);
            const contact = booking.userEmail || booking.userMobileNumber || booking.userId;

            return (
              <div key={booking.id} className="grid gap-4 p-5 md:grid-cols-[1.4fr_1.2fr_0.8fr_auto] md:items-center">
                <div>
                  <p className="text-sm font-extrabold text-gray-900">{customerName}</p>
                  <p className="mt-1 text-xs font-semibold text-gray-500">{contact}</p>
                </div>

                <div>
                  <p className="text-sm font-bold text-gray-800">{booking.turfName || `Turf ${booking.turfId}`}</p>
                  <p className="mt-1 text-xs font-semibold text-gray-500">
                    {formatDate(booking.date)} · {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
                  </p>
                </div>

                <div>
                  <p className="text-sm font-extrabold text-primary-dark">{formatCurrency(booking.totalPrice)}</p>
                  <p className="mt-1 text-xs font-semibold text-gray-500">Booking value</p>
                </div>

                <Badge variant={booking.status === 'CONFIRMED' ? 'success' : 'default'}>
                  {booking.status || 'PENDING'}
                </Badge>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
};

export default OwnerRecentBookings;
