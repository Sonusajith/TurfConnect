import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';

const TeamCard = ({ team, onJoin, onView }) => {
  const { name, sport, membersCount, isCaptain } = team;

  return (
    <Card className="group hover:border-primary/30 transition-all shadow-sm">
      <CardContent className="p-md flex flex-col h-full">
        <div className="flex justify-between items-start mb-sm">
          <div className="flex items-center gap-sm">
            <div className="w-12 h-12 bg-primary-container text-primary rounded-lg flex items-center justify-center font-bold text-xl">
              {name.charAt(0).toUpperCase()}
            </div>
            <div>
              <h3 className="font-headline-md text-lg text-on-surface line-clamp-1">{name}</h3>
              <p className="text-on-surface-variant font-label-sm">{sport}</p>
            </div>
          </div>
          {isCaptain && <Badge variant="primary">Captain</Badge>}
        </div>

        <div className="mt-auto pt-md border-t border-outline-variant/30 flex justify-between items-center">
          <div className="flex items-center gap-1 text-on-surface-variant text-sm">
            <span className="material-symbols-outlined text-[16px]">group</span>
            <span>{membersCount} Members</span>
          </div>
          
          <div className="flex gap-2">
            {onView && (
              <button onClick={onView} className="px-4 py-1.5 text-primary bg-primary-container/50 hover:bg-primary-container rounded font-label-md transition-colors">
                View
              </button>
            )}
            {onJoin && (
              <button onClick={onJoin} className="px-4 py-1.5 text-white bg-primary hover:bg-primary-dark rounded font-label-md transition-colors shadow-sm">
                Join
              </button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default TeamCard;
