import React, { useState, useEffect } from 'react';
import useTeams from '../hooks/useTeams';
import TeamList from '../features/teams/TeamList';
import TeamCreateModal from '../features/teams/TeamCreateModal';

const TeamsPage = () => {
  const { teams, loading, error, fetchTeams, createTeam, updateTeam, sendInvitation } = useTeams();
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    fetchTeams();
  }, [fetchTeams]);

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Community</p>
          <h1 className="text-3xl font-extrabold tracking-tight text-primary-dark sm:text-4xl">My Teams</h1>
          <p className="mt-1 text-sm font-medium text-gray-500">Manage your teams or create new ones.</p>
        </div>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="bg-primary hover:bg-primary-dark text-white px-5 py-2.5 rounded-lg font-bold shadow-sm transition"
        >
          Create Team
        </button>
      </div>

      <TeamList teams={teams} loading={loading} error={error} onSendInvite={sendInvitation} onUpdateTeam={updateTeam} />

      <TeamCreateModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onSubmit={createTeam} 
      />
    </div>
  );
};

export default TeamsPage;
