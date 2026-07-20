import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';

const OwnerStats = ({ stats, loading, error }) => {
  if (loading) return <div className="text-gray-500 font-medium animate-pulse">Loading analytics...</div>;
  if (error) return <div className="text-red-500 font-medium">Error: {error}</div>;

  const displayStats = [
    { label: 'Total Revenue', value: stats?.totalRevenue ? `₹${stats.totalRevenue}` : '₹0', trend: '+12%', icon: 'account_balance_wallet', color: 'text-primary', bg: 'bg-primary-light' },
    { label: 'Total Bookings', value: stats?.totalBookings || '0', trend: '+8%', icon: 'receipt_long', color: 'text-blue-600', bg: 'bg-blue-50' },
    { label: 'Avg Occupancy', value: stats?.avgOccupancy ? `${stats.avgOccupancy}%` : '0%', trend: '-2%', icon: 'pie_chart', color: 'text-purple-600', bg: 'bg-purple-50' },
    { label: 'Active Turfs', value: stats?.activeTurfs || '0', trend: null, icon: 'stadium', color: 'text-accent', bg: 'bg-accent-light' },
  ];

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {displayStats.map((stat, i) => (
        <Card key={i} className="border border-outline-variant/30 hover:shadow-md transition-shadow">
          <CardContent className="p-5 flex items-center gap-4">
            <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${stat.bg} ${stat.color}`}>
              <span className="material-symbols-outlined text-2xl">{stat.icon}</span>
            </div>
            <div>
              <p className="text-sm font-semibold text-gray-500">{stat.label}</p>
              <div className="flex items-baseline gap-2">
                <p className="text-2xl font-extrabold text-gray-900">{stat.value}</p>
                {stat.trend && (
                  <span className={`text-[10px] font-bold ${stat.trend.startsWith('+') ? 'text-secondary-dark' : 'text-red-600'}`}>
                    {stat.trend}
                  </span>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default OwnerStats;
