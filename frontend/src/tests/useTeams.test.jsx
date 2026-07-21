import React from 'react';
import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, test, vi } from 'vitest';
import useTeams from '../hooks/useTeams';
import { AuthContext } from '../contexts/authContextCore';

const wrapper = ({ children }) => (
  <AuthContext.Provider
    value={{
      token: 'expired-token',
      user: { userId: 'user-1', role: 'PLAYER' },
    }}
  >
    {children}
  </AuthContext.Provider>
);

describe('useTeams', () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('accessToken', 'expired-token');
    localStorage.setItem('refreshToken', 'refresh-token');
    localStorage.setItem('user', JSON.stringify({ userId: 'user-1', role: 'PLAYER' }));
    global.fetch = vi.fn();
  });

  test('refreshes an expired token before creating a team', async () => {
    const createdTeam = {
      id: 'team-1',
      name: 'Refresh Squad',
      sportType: 'Football',
      members: [{ userId: 'user-1', role: 'CAPTAIN' }],
    };

    global.fetch
      .mockResolvedValueOnce(new Response('', { status: 401 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        success: true,
        data: {
          accessToken: 'fresh-token',
          refreshToken: 'refresh-token',
          userId: 'user-1',
          role: 'PLAYER',
        },
      }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        success: true,
        data: createdTeam,
      }), { status: 201, headers: { 'Content-Type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        success: true,
        data: [createdTeam],
      }), { status: 200, headers: { 'Content-Type': 'application/json' } }));

    const { result } = renderHook(() => useTeams(), { wrapper });

    await act(async () => {
      await expect(result.current.createTeam({ name: 'Refresh Squad', sportType: 'Football' })).resolves.toBe(true);
    });

    await waitFor(() => {
      expect(result.current.teams).toHaveLength(1);
    });
    expect(localStorage.getItem('accessToken')).toBe('fresh-token');
    expect(global.fetch).toHaveBeenNthCalledWith(
      3,
      'http://localhost:8080/api/v1/teams',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ Authorization: 'Bearer fresh-token' }),
      }),
    );
  });
});
