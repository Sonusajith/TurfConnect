import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';

const MatchCard = ({ match, onViewDetails }) => {
  const { teamA, teamB, date, time, location, status, score } = match;

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SCHEDULED': return <Badge variant="secondary">Scheduled</Badge>;
      case 'ONGOING': return <Badge variant="accent" className="animate-pulse">Live</Badge>;
      case 'COMPLETED': return <Badge variant="default">Completed</Badge>;
      case 'CANCELLED': return <Badge variant="danger">Cancelled</Badge>;
      default: return null;
    }
  };

  return (
    <Card className="hover:shadow-md transition-all border border-outline-variant/50">
      <CardContent className="p-0">
        <div className="bg-surface-container-low p-sm px-md flex justify-between items-center border-b border-outline-variant/30">
          <div className="flex items-center gap-4 text-on-surface-variant text-xs font-semibold uppercase tracking-wider">
            <span className="flex items-center gap-1"><span className="material-symbols-outlined text-[14px]">calendar_today</span> {date}</span>
            <span className="flex items-center gap-1"><span className="material-symbols-outlined text-[14px]">schedule</span> {time}</span>
          </div>
          {getStatusBadge(status)}
        </div>

        <div className="p-md flex items-center justify-between">
          <div className="flex flex-col items-center flex-1">
            <div className="w-16 h-16 bg-surface-container-high rounded-full flex items-center justify-center mb-2 shadow-inner">
              <span className="font-bold text-xl text-on-surface">{teamA.name.substring(0, 2).toUpperCase()}</span>
            </div>
            <span className="font-headline-md text-sm text-center">{teamA.name}</span>
          </div>

          <div className="flex flex-col items-center px-4">
            {status === 'COMPLETED' ? (
              <div className="flex items-center gap-3 font-headline-xl text-2xl">
                <span className={score.teamA > score.teamB ? "text-primary" : "text-on-surface-variant"}>{score.teamA}</span>
                <span className="text-outline">-</span>
                <span className={score.teamB > score.teamA ? "text-primary" : "text-on-surface-variant"}>{score.teamB}</span>
              </div>
            ) : (
              <span className="font-bold text-outline uppercase text-xs tracking-widest bg-surface-container px-3 py-1 rounded-full">VS</span>
            )}
          </div>

          <div className="flex flex-col items-center flex-1">
            <div className="w-16 h-16 bg-surface-container-high rounded-full flex items-center justify-center mb-2 shadow-inner">
              <span className="font-bold text-xl text-on-surface">{teamB.name.substring(0, 2).toUpperCase()}</span>
            </div>
            <span className="font-headline-md text-sm text-center">{teamB.name}</span>
          </div>
        </div>

        <div className="px-md pb-md flex justify-between items-center">
          <p className="text-on-surface-variant text-sm flex items-center gap-1 line-clamp-1">
            <span className="material-symbols-outlined text-[16px]">location_on</span> {location}
          </p>
          {onViewDetails && (
            <button onClick={onViewDetails} className="text-primary font-label-md hover:underline whitespace-nowrap ml-4">
              Match Center →
            </button>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default MatchCard;
