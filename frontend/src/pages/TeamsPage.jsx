import React, { useState } from 'react';
import TeamCard from '../features/community/TeamCard';

const MOCK_TEAMS = [
  { id: 1, name: 'FC Thunder', sport: 'Football', membersCount: 11, isCaptain: true },
  { id: 2, name: 'Net Ninjas', sport: 'Badminton', membersCount: 4, isCaptain: false },
  { id: 3, name: 'Pitch Pirates', sport: 'Football', membersCount: 8, isCaptain: false },
];

const TeamsPage = () => {
  const [teams] = useState(MOCK_TEAMS);

  return (
    <div className="mx-auto max-w-7xl animate-fade-in pb-24 md:pb-8">
      <div className="mb-8 flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Community</p>
          <h1 className="mt-1 text-4xl font-extrabold tracking-tight text-primary-dark">My Teams</h1>
          <p className="mt-2 text-sm font-medium text-gray-600">Manage your teams or join new ones.</p>
        </div>
        <button className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-6 py-3 text-sm font-extrabold text-white shadow-sm transition hover:bg-primary-dark sm:w-auto">
          Create Team
        </button>
      </div>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-3">
        {teams.map((team) => (
          <TeamCard
            key={team.id}
            team={team}
            onView={() => console.log('View team', team.id)}
          />
        ))}
      </div>

      {teams.length === 0 && (
        <div className="rounded-lg border border-dashed border-primary/20 bg-white py-16 text-center shadow-sm">
          <h3 className="mb-2 text-lg font-extrabold text-gray-950">No teams yet</h3>
          <p className="mx-auto max-w-sm text-sm font-medium text-gray-500">
            Create a team to start inviting players and scheduling matches.
          </p>
        </div>
      )}
    </div>
  );
};

export default TeamsPage;
