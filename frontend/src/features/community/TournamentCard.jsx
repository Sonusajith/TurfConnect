import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';

const TournamentCard = ({ tournament, onRegister, onView }) => {
  const { name, sport, startDate, endDate, prizePool, teamsRegistered, maxTeams, status } = tournament;

  const isRegistrationOpen = status === 'OPEN_FOR_REGISTRATION';

  return (
    <Card className="overflow-hidden group hover:shadow-lg transition-all border border-outline-variant/30 flex flex-col h-full">
      <div className="h-32 bg-gradient-to-br from-primary via-primary-dark to-secondary relative flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0 opacity-10 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjIiIGZpbGw9IiNmZmZmZmYiLz48L3N2Zz4=')] bg-[length:20px_20px]"></div>
        <span className="material-symbols-outlined text-white/20 text-6xl absolute -right-4 -bottom-4 transform rotate-12">emoji_events</span>
        <h3 className="text-white font-headline-md text-xl relative z-10 px-4 text-center text-balance drop-shadow-md">{name}</h3>
      </div>
      
      <CardContent className="p-md flex-1 flex flex-col">
        <div className="flex justify-between items-start mb-4">
          <Badge variant={isRegistrationOpen ? 'secondary' : 'default'} className="uppercase tracking-wider text-[10px]">
            {status.replace(/_/g, ' ')}
          </Badge>
          <span className="text-primary font-bold bg-primary-container px-2 py-0.5 rounded text-sm">
            {prizePool}
          </span>
        </div>

        <div className="space-y-2 mb-6">
          <div className="flex items-center gap-2 text-on-surface-variant text-sm">
            <span className="material-symbols-outlined text-[16px]">sports_soccer</span>
            <span>{sport}</span>
          </div>
          <div className="flex items-center gap-2 text-on-surface-variant text-sm">
            <span className="material-symbols-outlined text-[16px]">calendar_month</span>
            <span>{startDate} - {endDate}</span>
          </div>
          <div className="flex items-center gap-2 text-on-surface-variant text-sm">
            <span className="material-symbols-outlined text-[16px]">groups</span>
            <span>{teamsRegistered} / {maxTeams} Teams</span>
          </div>
        </div>

        <div className="mt-auto pt-4 border-t border-outline-variant/30 flex gap-2">
          {onView && (
            <button onClick={onView} className="flex-1 py-2 border border-primary text-primary hover:bg-primary-container rounded-lg font-label-md transition-colors">
              Details
            </button>
          )}
          {isRegistrationOpen && onRegister && (
            <button
              onClick={onRegister}
              className="flex-1 rounded-lg bg-primary px-4 py-2 text-sm font-bold text-white shadow-sm transition-colors hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
            >
              Participate
            </button>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default TournamentCard;
