import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';

const TeamList = ({ teams, loading, error }) => {
  if (loading) return <div className="text-gray-500 font-medium">Loading teams...</div>;
  if (error) return <div className="text-red-500 font-medium">Error: {error}</div>;
  
  if (!teams || teams.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-2xl border border-primary/10 shadow-sm">
        <span className="material-symbols-outlined text-4xl text-gray-300 mb-2">groups</span>
        <p className="text-gray-500 font-medium">You are not part of any teams yet.</p>
      </div>
    );
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {teams.map((team, i) => (
        <Card key={team.id || i} className="border border-outline-variant/30 hover:border-primary/30 transition-colors">
          <CardContent className="p-5">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-primary-light text-primary flex items-center justify-center font-extrabold text-lg">
                  {team.name ? team.name[0].toUpperCase() : 'T'}
                </div>
                <div>
                  <h3 className="font-bold text-gray-900 leading-tight">{team.name}</h3>
                  <p className="text-xs font-semibold text-gray-500">{team.sportType || 'General'}</p>
                </div>
              </div>
              {team.role === 'CAPTAIN' && (
                <span className="px-2 py-1 bg-secondary-light text-secondary-dark text-[10px] font-bold uppercase tracking-wider rounded-md">Captain</span>
              )}
            </div>
            
            <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between text-sm">
              <div className="flex items-center gap-1.5 text-gray-600 font-medium">
                <span className="material-symbols-outlined text-lg">group</span>
                {team.memberCount || 1} Members
              </div>
              <button className="text-primary font-bold hover:text-primary-dark transition-colors">
                View
              </button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default TeamList;
