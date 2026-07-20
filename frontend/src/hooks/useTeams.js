import { useState, useCallback } from 'react';
import { useAuth } from './useAuth';
import { API_ENDPOINTS, API_BASE_URL } from '../constants/api';

export const useTeams = () => {
  const [teams, setTeams] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { token, user } = useAuth();

  const fetchTeams = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TEAMS.LIST}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!res.ok) {
        let errMsg = `HTTP Error ${res.status}`;
        try {
          const errData = await res.json();
          errMsg = errData.message || errMsg;
        } catch (e) {
          // ignore json parse error
        }
        throw new Error(errMsg);
      }
      const data = await res.json();
      let rawTeams = data.data || data || [];
      if (!Array.isArray(rawTeams)) {
        rawTeams = [];
      }
      const processedTeams = rawTeams.map(t => {
        const myMembership = t.members?.find(m => m.userId === user?.userId);
        return {
          ...t,
          role: myMembership?.role,
          memberCount: t.members?.length || 0
        };
      });
      setTeams(processedTeams);
    } catch (e) {
      console.error("Teams API failed:", e.message);
      setError(e.message || 'Failed to load teams');
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
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to create team');
      }
      await fetchTeams();
      return true;
    } catch (e) {
      console.error("Create Team API failed:", e.message);
      throw e;
    }
  };

  const updateTeam = async (teamId, teamData) => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TEAMS.LIST}/${teamId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(teamData)
      });
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to update team');
      }
      await fetchTeams();
      return true;
    } catch (e) {
      console.error("Update Team API failed:", e.message);
      throw e;
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
      console.error("Invitations API failed:", e.message);
      setError(e.message || 'Failed to load invitations');
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
      throw e;
    }
  };

  const sendInvitation = async (teamId, email, message = '') => {
    try {
      const url = `${API_BASE_URL}${API_ENDPOINTS.INVITATIONS.SEND.replace(':teamId', teamId)}`;
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ inviteeEmail: email, message })
      });
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to send invitation');
      }
      return await res.json();
    } catch (e) {
      console.error("Send Invitation API failed:", e.message);
      throw e;
    }
  };

  return { teams, invitations, loading, error, fetchTeams, createTeam, updateTeam, fetchInvitations, respondToInvitation, sendInvitation };
};

export default useTeams;
