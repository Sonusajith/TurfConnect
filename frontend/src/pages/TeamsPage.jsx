import React, { useState } from 'react';
import TeamCard from '../features/community/TeamCard';

// Mock Data
const MOCK_TEAMS = [
  { id: 1, name: 'FC Thunder', sport: 'Football', membersCount: 11, isCaptain: true },
  { id: 2, name: 'Net Ninjas', sport: 'Badminton', membersCount: 4, isCaptain: false },
  { id: 3, name: 'Pitch Pirates', sport: 'Football', membersCount: 8, isCaptain: false }
];

const TeamsPage = () => {
  const [teams] = useState(MOCK_TEAMS);

  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-7xl mx-auto pb-24 md:pb-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4">
        <div>
          <h1 className="font-headline-lg text-headline-lg text-on-surface">My Teams</h1>
          <p className="text-on-surface-variant mt-1">Manage your teams or join new ones.</p>
        </div>
        <button className="bg-primary hover:bg-primary-dark text-white px-6 py-2.5 rounded-lg font-label-md flex items-center gap-2 transition-colors shadow-sm w-full sm:w-auto justify-center">
          <span className="material-symbols-outlined">add</span>
          Create Team
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-gutter">
        {teams.map(team => (
          <TeamCard 
            key={team.id} 
            team={team} 
            onView={() => console.log('View team', team.id)} 
          />
        ))}
      </div>

      {teams.length === 0 && (
        <div className="text-center py-16 bg-surface-container-lowest rounded-xl border border-dashed border-outline-variant">
          <span className="material-symbols-outlined text-5xl text-on-surface-variant opacity-50 mb-4">groups</span>
          <h3 className="font-headline-md text-on-surface mb-2">No teams yet</h3>
          <p className="text-on-surface-variant max-w-sm mx-auto">Create a team to start inviting players and scheduling matches.</p>
        </div>
      )}
    </div>
  );
};

export default TeamsPage;
