import { useState, useEffect, useCallback } from 'react';
import { slotService } from '../services/slotService';

export const useSlots = (turfId, date) => {
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchSlots = useCallback(async () => {
    if (!turfId || !date) return;
    setLoading(true);
    setError(null);
    try {
      const response = await slotService.getSlots(turfId, date);
      if (response && response.success) {
        setSlots(response.data);
      } else {
        throw new Error(response?.message || 'Failed to load slots');
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [turfId, date]);

  useEffect(() => {
    fetchSlots();
  }, [fetchSlots]);

  // Handler to merge socket updates in-place without refetching the whole list
  const updateSlotInList = useCallback((updatedSlot) => {
    setSlots((prevSlots) =>
      prevSlots.map((slot) => (slot.id === updatedSlot.id ? updatedSlot : slot))
    );
  }, []);

  return { slots, loading, error, refetch: fetchSlots, updateSlotInList };
};
export default useSlots;
