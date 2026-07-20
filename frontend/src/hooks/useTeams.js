import { useState, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { API_ENDPOINTS, API_BASE_URL } from '../constants/api';

export const useTeams = () => {
  const [teams, setTeams] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { token } = useAuth();

  const fetchTeams = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TEAMS.LIST}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setTeams(data.data || data || []);
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [token]);

  const createTeam = async (teamData) => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TEAMS.CREATE}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(teamData)
      });
      if (!res.ok) throw new Error('Failed to create team');
      await fetchTeams();
      return true;
    } catch (e) {
      setError(e.message);
      return false;
    }
  };

  const fetchInvitations = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.INVITATIONS.LIST}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setInvitations(data.data || data || []);
      }
    } catch (e) {
      console.error(e);
    }
  }, [token]);

  const respondToInvitation = async (invitationId, status) => {
    try {
      const url = `${API_BASE_URL}${API_ENDPOINTS.INVITATIONS.RESPOND.replace(':invitationId', invitationId)}`;
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ status })
      });
      if (res.ok) {
        await fetchInvitations();
        await fetchTeams();
      }
    } catch (e) {
      console.error(e);
    }
  };

  return { teams, invitations, loading, error, fetchTeams, createTeam, fetchInvitations, respondToInvitation };
};

export default useTeams;
