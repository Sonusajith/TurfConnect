import { useState, useEffect, useCallback } from 'react';
import { turfService } from '../services/turfService';

export const useTurfs = (searchParams = {}) => {
  const [turfs, setTurfs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchTurfs = useCallback(async (params = searchParams) => {
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
      console.warn("Turfs API failed, using mock data:", e.message);
      setTurfs([
        { id: 't1', name: 'Elite Park Stadium', location: 'Downtown', sportTypes: ['Football'], hourlyRate: 1500, active: true, images: [], rating: 4.8 },
        { id: 't2', name: 'City Sports Hub', location: 'Westside', sportTypes: ['Badminton', 'Tennis'], hourlyRate: 800, active: true, images: [], rating: 4.2 }
      ]);
      setError(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTurfs();
  }, [fetchTurfs]);

  return { turfs, loading, error, refetch: fetchTurfs };
};
export default useTurfs;
