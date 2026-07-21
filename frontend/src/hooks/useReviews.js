import { useState, useCallback } from 'react';
import { useAuth } from './useAuth';
import { API_ENDPOINTS, API_BASE_URL } from '../constants/api';

export const useReviews = (turfId) => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { token } = useAuth();

  const fetchReviews = useCallback(async () => {
    if (!turfId) return;
    setLoading(true);
    setError(null);
    try {
      const url = `${API_BASE_URL}${API_ENDPOINTS.REVIEWS.LIST.replace(':turfId', turfId)}`;
      const res = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!res.ok) {
        if (res.status === 404) {
          setReviews([]);
          return;
        }
        throw new Error('Failed to fetch reviews');
      }
      const data = await res.json();
      setReviews(data.data || data || []);
    } catch (e) {
      console.error("Reviews API failed:", e.message);
      setReviews([]);
      setError('Review service is unavailable. Start review-service and refresh this page.');
    } finally {
      setLoading(false);
    }
  }, [turfId, token]);

  const submitReview = async (reviewData) => {
    try {
      const url = `${API_BASE_URL}${API_ENDPOINTS.REVIEWS.CREATE}`;
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(reviewData)
      });
      if (!res.ok) throw new Error('Failed to submit review');
      await fetchReviews();
      return true;
    } catch (e) {
      console.error("Submit Review API failed:", e.message);
      throw e;
    }
  };

  return { reviews, loading, error, fetchReviews, submitReview };
};

export default useReviews;
