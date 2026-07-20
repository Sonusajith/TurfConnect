import { useState, useCallback } from 'react';
import { useAuth } from './useAuth';
import { API_BASE_URL, API_ENDPOINTS } from '../constants/api';

export const useAnalytics = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { token, user } = useAuth();

  const fetchPlatformAnalytics = useCallback(async (startDate, endDate) => {
    if (user?.role !== 'SUPER_ADMIN' && user?.role !== 'ORG_ADMIN') {
      return;
    }
    
    setLoading(true);
    setError(null);
    try {
      const url = new URL(`${API_BASE_URL}${API_ENDPOINTS.OWNER.STATS}`);
      url.searchParams.append('startDate', startDate);
      url.searchParams.append('endDate', endDate);
      
      const res = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!res.ok) throw new Error('Failed to load analytics data');
      const json = await res.json();
      setData(json);
    } catch (e) {
      console.error("Analytics API failed:", e.message);
      setError(e.message || 'Failed to load analytics data');
    } finally {
      setLoading(false);
    }
  }, [token, user?.role]);

  return { data, loading, error, fetchPlatformAnalytics };
};

export default useAnalytics;
