import { useState, useEffect, useCallback } from 'react';
import { bookingService } from '../services/bookingService';

export const useBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchBookings = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await bookingService.getMyBookings();
      if (response && response.success) {
        setBookings(response.data);
      } else {
        throw new Error(response?.message || 'Failed to load bookings');
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  const cancelBooking = useCallback(async (bookingId) => {
    const response = await bookingService.cancel(bookingId);
    if (response && response.success && response.data) {
      setBookings((current) => current.map((booking) => (
        booking.id === bookingId ? response.data : booking
      )));
    }
    try {
      await fetchBookings();
    } catch {
      // Keep the optimistic cancellation result if a background refresh fails.
    }
  }, [fetchBookings]);

  const updateSplitContribution = useCallback(async (bookingId, splitContribution) => {
    await bookingService.updateSplitContribution(bookingId, splitContribution);
    await fetchBookings();
  }, [fetchBookings]);

  return { bookings, loading, error, refetch: fetchBookings, cancelBooking, updateSplitContribution };
};
export default useBookings;
