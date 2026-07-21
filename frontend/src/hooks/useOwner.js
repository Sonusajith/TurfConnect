import { useState, useCallback } from 'react';
import { useAuth } from './useAuth';
import { API_ENDPOINTS, API_BASE_URL } from '../constants/api';

export const useOwner = () => {
  const [stats, setStats] = useState(null);
  const [turfs, setTurfs] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { token, user } = useAuth();

  const fetchDashboardData = useCallback(async () => {
    // Only fetch if user is an owner/admin
    if (!['TURF_OWNER', 'ORG_ADMIN', 'FRANCHISE_ADMIN', 'SUPER_ADMIN'].includes(user?.role)) {
      return;
    }
    
    setLoading(true);
    setError(null);
    try {
      const turfsRes = await fetch(`${API_BASE_URL}${API_ENDPOINTS.OWNER.TURFS}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!turfsRes.ok) throw new Error('Failed to fetch owner turfs');
      const turfsData = await turfsRes.json();
      const ownerTurfs = turfsData.data?.content || turfsData.data || turfsData || [];
      setTurfs(ownerTurfs);

      const bookingResults = await Promise.allSettled(
        ownerTurfs
          .filter((turf) => turf.id)
          .map(async (turf) => {
            const endpoint = API_ENDPOINTS.BOOKINGS.TURF_BOOKINGS.replace(':turfId', turf.id);
            const bookingsRes = await fetch(`${API_BASE_URL}${endpoint}`, {
              headers: { Authorization: `Bearer ${token}` }
            });
            if (!bookingsRes.ok) throw new Error(`Failed to fetch bookings for ${turf.name || turf.id}`);
            const bookingsData = await bookingsRes.json();
            const turfBookings = bookingsData.data || bookingsData || [];
            return turfBookings.map((booking) => ({
              ...booking,
              turfName: turf.name,
            }));
          })
      );

      const ownerBookings = bookingResults
        .filter((result) => result.status === 'fulfilled')
        .flatMap((result) => result.value)
        .sort((a, b) => new Date(b.createdAt || b.date || 0) - new Date(a.createdAt || a.date || 0));
      setBookings(ownerBookings);

      const statsRes = await fetch(`${API_BASE_URL}${API_ENDPOINTS.OWNER.STATS}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      let fetchedStats = {};
      if (statsRes.ok) {
        const statsData = await statsRes.json();
        fetchedStats = statsData.data || statsData || {};
      }

      setStats({
        ...fetchedStats,
        activeTurfs: ownerTurfs.filter((turf) => turf.status === 'ACTIVE' || turf.active).length,
        totalBookings: fetchedStats.totalBookings ?? ownerBookings.length,
        totalRevenue: fetchedStats.totalRevenue ?? ownerBookings
          .filter((booking) => booking.status === 'CONFIRMED')
          .reduce((sum, booking) => sum + Number(booking.totalPrice || 0), 0),
      });
    } catch (e) {
      console.error("Owner API failed:", e.message);
      setError(e.message || 'Failed to load owner data');
    } finally {
      setLoading(false);
    }
  }, [token, user?.role]);

  const addTurf = async (turfData) => {
    try {
      const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TURFS.LIST}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(turfData)
      });
      
      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'Failed to create venue');
      }
      
      const newTurfData = await response.json();
      const newTurf = newTurfData.data || newTurfData;
      setTurfs((prev) => [...prev, newTurf]);
      return newTurf;
    } catch (err) {
      console.error("Failed to add turf:", err);
      throw err;
    }
  };

  return { stats, turfs, bookings, loading, error, fetchDashboardData, addTurf };
};

export default useOwner;
