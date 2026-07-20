import { useState, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
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
      // Fetch stats
      const statsRes = await fetch(`${API_BASE_URL}${API_ENDPOINTS.OWNER.STATS}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!statsRes.ok) throw new Error('Failed to fetch owner stats');
      const statsData = await statsRes.json();
      setStats(statsData.data || statsData);

      // Fetch turfs
      const turfsRes = await fetch(`${API_BASE_URL}${API_ENDPOINTS.OWNER.TURFS}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!turfsRes.ok) throw new Error('Failed to fetch owner turfs');
      const turfsData = await turfsRes.json();
      setTurfs(turfsData.data || turfsData || []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [token, user?.role]);

  return { stats, turfs, loading, error, fetchDashboardData };
};

export default useOwner;
