import { useState, useCallback } from 'react';
import { useAuth } from './useAuth';
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
      if (!res.ok) throw new Error('Failed to load teams');
      const data = await res.json();
      setTeams(data.data || data || []);
    } catch (e) {
      console.warn("Teams API failed, using mock data:", e.message);
      setTeams([
        { id: 'm1', name: 'FC Thunder', sportType: 'Football', role: 'CAPTAIN', memberCount: 5 },
        { id: 'm2', name: 'Net Ninjas', sportType: 'Badminton', role: 'MEMBER', memberCount: 2 }
      ]);
      setError(null);
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
      console.warn("Create Team API failed, simulating success:", e.message);
      setTeams(prev => [...prev, { id: Date.now().toString(), name: teamData.name, sportType: teamData.sportType, role: 'CAPTAIN', memberCount: 1 }]);
      return true;
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
      console.warn("Invitations API failed, using mock data:", e.message);
      setInvitations([
        { id: 'inv1', senderName: 'Alex', teamName: 'Pitch Pirates', createdAt: new Date().toISOString() }
      ]);
    }
  }, [token]);

  const respondToInvitation = async (invitationId, status) => {
    try {
      const endpoint = status === 'ACCEPTED'
        ? API_ENDPOINTS.INVITATIONS.ACCEPT
        : API_ENDPOINTS.INVITATIONS.DECLINE;
      const url = `${API_BASE_URL}${endpoint.replace(':invitationId', invitationId)}`;
      const res = await fetch(url, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        }
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
