import React, { useState } from 'react';
import TournamentCard from '../features/community/TournamentCard';
import TournamentRegistrationModal from '../features/community/TournamentRegistrationModal';
import TournamentDetailsModal from '../features/community/TournamentDetailsModal';
import { useToast } from '../hooks/useToast';

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
  const [tournaments, setTournaments] = useState(MOCK_TOURNAMENTS);
  const [registrationModalData, setRegistrationModalData] = useState({ isOpen: false, tournament: null });
  const [detailsModalData, setDetailsModalData] = useState({ isOpen: false, tournament: null });
  const { addToast } = useToast();

  const handleRegister = async (tournamentId, _teamId) => {
    // Simulate an API call
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // Update local mock state to show registered
    setTournaments(prev => prev.map(t => {
      if (t.id === tournamentId) {
        return { ...t, status: 'REGISTERED', teamsRegistered: t.teamsRegistered + 1 };
      }
      return t;
    }));
    
    addToast('Successfully registered for tournament!', 'success');
  };
  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-7xl mx-auto pb-24 md:pb-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4">
        <div>
          <h1 className="font-headline-lg text-headline-lg text-on-surface">Tournaments</h1>
          <p className="text-on-surface-variant mt-1">Compete for glory and exciting prize pools.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-gutter">
        {tournaments.map(tournament => (
          <TournamentCard 
            key={tournament.id} 
            tournament={tournament} 
            onRegister={() => setRegistrationModalData({ isOpen: true, tournament })}
            onView={() => setDetailsModalData({ isOpen: true, tournament })}
          />
        ))}
      </div>

      <TournamentRegistrationModal 
        isOpen={registrationModalData.isOpen}
        onClose={() => setRegistrationModalData({ isOpen: false, tournament: null })}
        tournament={registrationModalData.tournament}
        onRegister={handleRegister}
      />

      <TournamentDetailsModal
        isOpen={detailsModalData.isOpen}
        onClose={() => setDetailsModalData({ isOpen: false, tournament: null })}
        tournament={detailsModalData.tournament}
      />
    </div>
  );
};

export default TournamentsPage;
