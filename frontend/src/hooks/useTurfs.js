import { useState, useEffect, useCallback } from 'react';
import { turfService } from '../services/turfService';

const DEFAULT_SEARCH_PARAMS = {};

export const useTurfs = (searchParams = DEFAULT_SEARCH_PARAMS) => {
  const [turfs, setTurfs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchTurfs = useCallback(async (params = DEFAULT_SEARCH_PARAMS) => {
    setLoading(true);
    setError(null);
    try {
      const response = await turfService.list(params);
      if (response && response.success) {
        setTurfs(response.data.content || response.data);
      } else {
        throw new Error(response?.message || 'Failed to load turfs');
      }
    } catch (e) {
      console.error("Turfs API failed:", e.message);
      setError(e.message || 'Failed to load turfs');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTurfs(searchParams);
  }, [fetchTurfs, searchParams]);

  return { turfs, loading, error, refetch: fetchTurfs };
};
export default useTurfs;
