import React from 'react';
import TournamentCard from '../features/community/TournamentCard';

const MOCK_TOURNAMENTS = [
  {
    id: 1,
    name: 'Summer Futsal Cup 2026',
    sport: 'Football',
    startDate: 'Nov 01, 2026',
    endDate: 'Nov 15, 2026',
    prizePool: '₹50,000',
    teamsRegistered: 12,
    maxTeams: 16,
    status: 'OPEN_FOR_REGISTRATION'
  },
  {
    id: 2,
    name: 'City Smashers Badminton',
    sport: 'Badminton',
    startDate: 'Oct 10, 2026',
    endDate: 'Oct 12, 2026',
    prizePool: '₹20,000',
    teamsRegistered: 32,
    maxTeams: 32,
    status: 'IN_PROGRESS'
  }
];

const TournamentsPage = () => {
  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-7xl mx-auto pb-24 md:pb-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4">
        <div>
          <h1 className="font-headline-lg text-headline-lg text-on-surface">Tournaments</h1>
          <p className="text-on-surface-variant mt-1">Compete for glory and exciting prize pools.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-gutter">
        {MOCK_TOURNAMENTS.map(tournament => (
          <TournamentCard 
            key={tournament.id} 
            tournament={tournament} 
            onRegister={() => console.log('Register for tournament', tournament.id)}
            onView={() => console.log('View tournament', tournament.id)}
          />
        ))}
      </div>
    </div>
  );
};

export default TournamentsPage;
