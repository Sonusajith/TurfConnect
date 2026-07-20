import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation, Link } from 'react-router-dom';
import { API_BASE_URL, API_ENDPOINTS } from '../constants/api';
import { Card, CardContent } from '../components/ui/Card';
import { useAuth } from '../hooks/useAuth';

const JoinTeamPage = () => {
  const { teamId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user, token } = useAuth();
  const [team, setTeam] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [playerName, setPlayerName] = useState('');
  const [isJoining, setIsJoining] = useState(false);
  const [joinSuccess, setJoinSuccess] = useState(false);

  useEffect(() => {
    const fetchTeam = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TEAMS.GET.replace(':teamId', teamId)}`);
        if (!res.ok) throw new Error('Failed to fetch team details');
        const data = await res.json();
        setTeam(data.data);
      } catch (err) {
        setError(err.message || 'Team not found');
      } finally {
        setLoading(false);
      }
    };
    fetchTeam();
  }, [teamId]);

  const handleJoin = async (e) => {
    e.preventDefault();
    if (!playerName.trim() || playerName.length < 2) {
      setError('Please enter a valid name (at least 2 characters)');
      return;
    }
    
    setIsJoining(true);
    setError('');
    
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TEAMS.JOIN.replace(':teamId', teamId)}`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify({ name: playerName })
      });
      
      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.message || 'Failed to join team');
      }
      
      setJoinSuccess(true);
    } catch (err) {
      setError(err.message || 'Failed to join team');
    } finally {
      setIsJoining(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="text-gray-500 font-medium animate-pulse">Loading team details...</div>
      </div>
    );
  }

  if (error && !team) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <Card className="max-w-md w-full border-red-100 bg-red-50">
          <CardContent className="p-8 text-center">
            <span className="material-symbols-outlined text-4xl text-red-500 mb-2">error</span>
            <h2 className="text-lg font-bold text-red-700 mb-2">Invalid Invite Link</h2>
            <p className="text-sm text-red-600 font-medium">This team could not be found. The link may have expired or is incorrect.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (joinSuccess) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <Card className="max-w-md w-full border-green-100">
          <CardContent className="p-8 text-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="material-symbols-outlined text-3xl text-green-600">check_circle</span>
            </div>
            <h2 className="text-2xl font-black text-gray-900 mb-2">You're in!</h2>
            <p className="text-gray-600 font-medium mb-6">
              You have successfully joined <strong className="text-gray-900">{team.name}</strong> as {playerName}.
            </p>
            <button 
              onClick={() => navigate('/login')}
              className="px-6 py-3 bg-primary hover:bg-primary-dark text-white font-bold rounded-xl transition-colors w-full"
            >
              Sign In to TurfConnect
            </button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="mb-8 text-center">
        <div className="flex items-center justify-center gap-2 mb-2">
          <span className="material-symbols-outlined text-primary text-3xl">sports_soccer</span>
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">TurfConnect</h1>
        </div>
        <p className="text-gray-500 font-semibold">Join the team and start playing</p>
      </div>

      <Card className="max-w-md w-full border-outline-variant/30 shadow-lg shadow-primary/5">
        <CardContent className="p-8">
          <div className="text-center mb-8">
            <div className="w-20 h-20 bg-primary-light text-primary rounded-2xl flex items-center justify-center text-4xl font-black mx-auto mb-4 shadow-sm border border-primary/20">
              {team.name ? team.name[0].toUpperCase() : 'T'}
            </div>
            <h2 className="text-2xl font-black text-gray-900 mb-1">{team.name}</h2>
            <p className="text-sm font-bold text-gray-500 uppercase tracking-wider">{team.sportType}</p>
          </div>

            {user ? (
              <form onSubmit={handleJoin} className="space-y-6">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-2">Your Player Name</label>
                  <input
                    type="text"
                    required
                    className="w-full border-2 border-gray-200 rounded-xl p-4 text-gray-900 font-medium focus:border-primary focus:ring-0 transition-colors"
                    placeholder="e.g. Cristiano Ronaldo"
                    value={playerName}
                    onChange={(e) => setPlayerName(e.target.value)}
                  />
                  <p className="text-xs text-gray-500 mt-2">
                    You are logged in as <strong>{user.email}</strong>. Entering a name will set your display name for this team.
                  </p>
                </div>

                {error && (
                  <div className="p-3 bg-red-50 text-red-600 text-sm font-semibold rounded-lg flex items-start gap-2">
                    <span className="material-symbols-outlined text-[18px]">error</span>
                    {error}
                  </div>
                )}

                <button
                  type="submit"
                  disabled={isJoining || !playerName.trim()}
                  className="w-full py-4 bg-primary hover:bg-primary-dark text-white font-black rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/30 flex items-center justify-center gap-2"
                >
                  {isJoining ? 'Joining...' : 'Join Team'}
                  {!isJoining && <span className="material-symbols-outlined text-[20px]">arrow_forward</span>}
                </button>
              </form>
            ) : (
              <div className="text-center space-y-4">
                <div className="bg-blue-50 p-4 rounded-xl border border-blue-100 mb-4">
                  <span className="material-symbols-outlined text-blue-500 text-3xl mb-2">lock</span>
                  <h3 className="text-sm font-bold text-blue-900 mb-1">Login Required</h3>
                  <p className="text-xs text-blue-800">You need to have an account and be logged in to join a team.</p>
                </div>
                
                <Link 
                  to={`/login?redirect=${encodeURIComponent(location.pathname)}`}
                  className="w-full py-4 bg-gray-900 hover:bg-gray-800 text-white font-black rounded-xl transition-all flex items-center justify-center gap-2 block"
                >
                  Log In or Register
                  <span className="material-symbols-outlined text-[20px]">login</span>
                </Link>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    );
  };

export default JoinTeamPage;
