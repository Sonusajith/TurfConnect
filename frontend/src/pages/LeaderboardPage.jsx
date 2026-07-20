import React from 'react';
import { Card, CardContent } from '../components/ui/Card';

const MOCK_LEADERBOARD = [
  { rank: 1, team: 'FC Thunder', played: 5, won: 4, drawn: 1, lost: 0, points: 13 },
  { rank: 2, team: 'Pitch Pirates', played: 5, won: 3, drawn: 1, lost: 1, points: 10 },
  { rank: 3, team: 'Weekend Warriors', played: 5, won: 2, drawn: 0, lost: 3, points: 6 },
  { rank: 4, team: 'Goal Diggers', played: 5, won: 0, drawn: 0, lost: 5, points: 0 }
];

const LeaderboardPage = () => {
  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-4xl mx-auto pb-24 md:pb-8">
      <div className="mb-8">
        <h1 className="font-headline-lg text-headline-lg text-on-surface">Live Leaderboard</h1>
        <p className="text-on-surface-variant mt-1">Summer Futsal Cup 2026</p>
      </div>

      <Card className="overflow-hidden border border-outline-variant/30">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container-high border-b border-outline-variant/40">
                <th className="p-4 font-label-lg text-on-surface-variant uppercase tracking-wider text-sm font-semibold w-16">Rank</th>
                <th className="p-4 font-label-lg text-on-surface-variant uppercase tracking-wider text-sm font-semibold">Team</th>
                <th className="p-4 font-label-lg text-on-surface-variant uppercase tracking-wider text-sm font-semibold text-center w-16">P</th>
                <th className="p-4 font-label-lg text-on-surface-variant uppercase tracking-wider text-sm font-semibold text-center w-16">W</th>
                <th className="p-4 font-label-lg text-on-surface-variant uppercase tracking-wider text-sm font-semibold text-center w-16">D</th>
                <th className="p-4 font-label-lg text-on-surface-variant uppercase tracking-wider text-sm font-semibold text-center w-16">L</th>
                <th className="p-4 font-label-lg text-primary uppercase tracking-wider text-sm font-bold text-center w-20">Pts</th>
              </tr>
            </thead>
            <tbody>
              {MOCK_LEADERBOARD.map((row, index) => (
                <tr key={row.rank} className={`border-b border-outline-variant/20 hover:bg-surface-container-lowest transition-colors ${index === 0 ? 'bg-primary-container/20' : ''}`}>
                  <td className="p-4 font-bold text-on-surface">
                    {index === 0 ? <span className="text-action-orange material-symbols-outlined text-xl">trophy</span> : row.rank}
                  </td>
                  <td className="p-4 font-headline-md text-on-surface">{row.team}</td>
                  <td className="p-4 text-center text-on-surface-variant">{row.played}</td>
                  <td className="p-4 text-center text-on-surface-variant">{row.won}</td>
                  <td className="p-4 text-center text-on-surface-variant">{row.drawn}</td>
                  <td className="p-4 text-center text-on-surface-variant">{row.lost}</td>
                  <td className="p-4 text-center font-bold text-primary text-lg">{row.points}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};

export default LeaderboardPage;
