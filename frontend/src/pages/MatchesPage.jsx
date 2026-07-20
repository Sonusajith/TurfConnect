import React, { useState } from 'react';
import MatchCard from '../features/community/MatchCard';

const MOCK_MATCHES = [
  {
    id: 1,
    teamA: { name: 'FC Thunder' },
    teamB: { name: 'Pitch Pirates' },
    date: 'Oct 24, 2026',
    time: '18:00',
    location: 'Elite Park Stadium',
    status: 'SCHEDULED',
    score: { teamA: 0, teamB: 0 }
  },
  {
    id: 2,
    teamA: { name: 'Net Ninjas' },
    teamB: { name: 'Shuttle Smashers' },
    date: 'Oct 20, 2026',
    time: '19:30',
    location: 'City Sports Hub',
    status: 'COMPLETED',
    score: { teamA: 2, teamB: 1 }
  }
];

const MatchesPage = () => {
  const [activeTab, setActiveTab] = useState('upcoming');
  const matches = MOCK_MATCHES;

  const upcomingMatches = matches.filter(m => m.status === 'SCHEDULED' || m.status === 'ONGOING');
  const pastMatches = matches.filter(m => m.status === 'COMPLETED' || m.status === 'CANCELLED');

  const displayedMatches = activeTab === 'upcoming' ? upcomingMatches : pastMatches;

  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-7xl mx-auto pb-24 md:pb-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4">
        <div>
          <h1 className="font-headline-lg text-headline-lg text-on-surface">Matches</h1>
          <p className="text-on-surface-variant mt-1">Schedule friendly matches against other teams.</p>
        </div>
        <button className="bg-action-orange hover:bg-accent-hover text-white px-6 py-2.5 rounded-lg font-label-md flex items-center gap-2 transition-colors shadow-sm w-full sm:w-auto justify-center">
          <span className="material-symbols-outlined">sports_score</span>
          Challenge Team
        </button>
      </div>

      <div className="flex gap-4 border-b border-outline-variant/30 mb-8">
        <button 
          onClick={() => setActiveTab('upcoming')}
          className={`pb-3 font-label-md transition-colors border-b-2 px-2 ${activeTab === 'upcoming' ? 'border-primary text-primary' : 'border-transparent text-on-surface-variant hover:text-on-surface'}`}
        >
          Upcoming ({upcomingMatches.length})
        </button>
        <button 
          onClick={() => setActiveTab('past')}
          className={`pb-3 font-label-md transition-colors border-b-2 px-2 ${activeTab === 'past' ? 'border-primary text-primary' : 'border-transparent text-on-surface-variant hover:text-on-surface'}`}
        >
          Past Results
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-gutter">
        {displayedMatches.length > 0 ? (
          displayedMatches.map(match => (
            <MatchCard 
              key={match.id} 
              match={match} 
              onViewDetails={() => console.log('View match', match.id)} 
            />
          ))
        ) : (
          <div className="col-span-full text-center py-16 bg-surface-container-lowest rounded-xl border border-dashed border-outline-variant">
            <span className="material-symbols-outlined text-5xl text-on-surface-variant opacity-50 mb-4">event_busy</span>
            <h3 className="font-headline-md text-on-surface mb-2">No {activeTab} matches</h3>
            <p className="text-on-surface-variant max-w-sm mx-auto">You don't have any matches in this category right now.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default MatchesPage;
