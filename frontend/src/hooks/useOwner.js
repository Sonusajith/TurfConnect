import { useState, useCallback } from 'react';
import { useAuth } from './useAuth';
import { API_ENDPOINTS, API_BASE_URL } from '../constants/api';

export const useOwner = () => {
  const [stats, setStats] = useState(null);
  const [turfs, setTurfs] = useState([]);
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

  return { stats, turfs, loading, error, fetchDashboardData, addTurf };
};

export default useOwner;
