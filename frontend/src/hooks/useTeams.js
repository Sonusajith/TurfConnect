import { useState, useCallback } from 'react';
import { useAuth } from './useAuth';
import { API_ENDPOINTS, API_BASE_URL } from '../constants/api';

const COMMUNITY_DIRECT_URL = import.meta.env.VITE_COMMUNITY_SERVICE_URL || 'http://localhost:8087';

const getJson = async (res) => {
  const text = await res.text();
  return text ? JSON.parse(text) : {};
};

const communityFetch = async (endpoint, options = {}) => {
  const gatewayUrl = `${API_BASE_URL}${endpoint}`;
  const directUrl = `${COMMUNITY_DIRECT_URL}${endpoint}`;
  const res = await fetch(gatewayUrl, options);

  if (res.status !== 503) {
    return res;
  }

  return fetch(directUrl, options);
};

export const useTeams = () => {
  const [teams, setTeams] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { token, user } = useAuth();
  const buildAuthHeaders = useCallback((extraHeaders = {}) => ({
    ...extraHeaders,
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(user?.userId ? { 'X-User-Id': user.userId } : {}),
    ...(user?.role ? { 'X-User-Role': user.role } : {}),
  }), [token, user?.role, user?.userId]);

  const withTeamMeta = useCallback((team) => {
    const myMembership = team.members?.find((member) => member.userId === user?.userId);
    return {
      ...team,
      role: myMembership?.role,
      memberCount: team.members?.length || 0,
    };
  }, [user?.userId]);

  const fetchTeams = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await communityFetch(API_ENDPOINTS.TEAMS.LIST, {
        headers: buildAuthHeaders()
      });
      if (!res.ok) {
        let errMsg = `HTTP Error ${res.status}`;
        try {
          const errData = await getJson(res);
          errMsg = errData.message || errMsg;
        } catch {
          // ignore json parse error
        }
        throw new Error(errMsg);
      }
      const data = await getJson(res);
      let rawTeams = data.data || data || [];
      if (!Array.isArray(rawTeams)) {
        rawTeams = [];
      }
      const processedTeams = rawTeams.map(withTeamMeta);
      setTeams(processedTeams);
    } catch (e) {
      console.error("Teams API failed:", e.message);
      setTeams([]);
      setError('Team service is unavailable. Start community-service and refresh this page.');
    } finally {
      setLoading(false);
    }
  }, [buildAuthHeaders, withTeamMeta]);

  const createTeam = async (teamData) => {
    try {
      const res = await communityFetch(API_ENDPOINTS.TEAMS.CREATE, {
        method: 'POST',
        headers: {
          ...buildAuthHeaders({ 'Content-Type': 'application/json' }),
        },
        body: JSON.stringify(teamData)
      });
      if (!res.ok) {
        const errorData = await getJson(res).catch(() => ({}));
        throw new Error(errorData.message || 'Failed to create team');
      }
      const data = await getJson(res);
      const createdTeam = data.data || data;
      if (createdTeam?.id) {
        setTeams((current) => [withTeamMeta(createdTeam), ...current.filter((team) => team.id !== createdTeam.id)]);
      }
      void fetchTeams();
      return true;
    } catch (e) {
      console.error("Create Team API failed:", e.message);
      throw e;
    }
  };

  const updateTeam = async (teamId, teamData) => {
    try {
      const res = await communityFetch(`${API_ENDPOINTS.TEAMS.LIST}/${teamId}`, {
        method: 'PUT',
        headers: {
          ...buildAuthHeaders({ 'Content-Type': 'application/json' }),
        },
        body: JSON.stringify(teamData)
      });
      if (!res.ok) {
        const errorData = await getJson(res).catch(() => ({}));
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
      const res = await communityFetch(API_ENDPOINTS.INVITATIONS.LIST, {
        headers: buildAuthHeaders()
      });
      if (res.ok) {
        const data = await getJson(res);
        setInvitations(data.data || data || []);
      }
    } catch (e) {
      console.error("Invitations API failed:", e.message);
      setError(e.message || 'Failed to load invitations');
    }
  }, [buildAuthHeaders]);

  const respondToInvitation = async (invitationId, status) => {
    try {
      const endpoint = status === 'ACCEPTED'
        ? API_ENDPOINTS.INVITATIONS.ACCEPT
        : API_ENDPOINTS.INVITATIONS.DECLINE;
      const res = await communityFetch(endpoint.replace(':invitationId', invitationId), {
        method: 'PUT',
        headers: {
          ...buildAuthHeaders({ 'Content-Type': 'application/json' }),
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
      const endpoint = API_ENDPOINTS.INVITATIONS.SEND.replace(':teamId', teamId);
      const res = await communityFetch(endpoint, {
        method: 'POST',
        headers: {
          ...buildAuthHeaders({ 'Content-Type': 'application/json' }),
        },
        body: JSON.stringify({ inviteeEmail: email, message })
      });
      if (!res.ok) {
        const errorData = await getJson(res).catch(() => ({}));
        throw new Error(errorData.message || 'Failed to send invitation');
      }
      return await getJson(res);
    } catch (e) {
      console.error("Send Invitation API failed:", e.message);
      throw e;
    }
  };

  return { teams, invitations, loading, error, fetchTeams, createTeam, updateTeam, fetchInvitations, respondToInvitation, sendInvitation };
};

export default useTeams;
