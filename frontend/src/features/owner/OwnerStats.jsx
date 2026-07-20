import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';
import { formatCurrency } from '../../utils/formatters';

const OwnerStats = ({ stats, loading, error }) => {
  if (loading) return <div className="font-medium text-gray-500 animate-pulse">Loading analytics...</div>;
  if (error) return <div className="font-medium text-red-500">Error: {error}</div>;

  const displayStats = [
    { label: 'Total Revenue', value: formatCurrency(stats?.totalRevenue || 0), icon: 'account_balance_wallet', color: 'text-primary', bg: 'bg-primary-light' },
    { label: 'Total Bookings', value: stats?.totalBookings || '0', icon: 'receipt_long', color: 'text-blue-600', bg: 'bg-blue-50' },
    { label: 'Confirmation Rate', value: stats?.confirmationRate ? `${(stats.confirmationRate * 100).toFixed(1)}%` : '0%', icon: 'pie_chart', color: 'text-purple-600', bg: 'bg-purple-50' },
    { label: 'Active Turfs', value: stats?.activeTurfs || '0', icon: 'stadium', color: 'text-accent', bg: 'bg-accent-light' },
  ];

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {displayStats.map((stat) => (
        <Card key={stat.label} className="border border-outline-variant/30 transition-shadow hover:shadow-md">
          <CardContent className="flex items-center gap-4 p-5">
            <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${stat.bg} ${stat.color}`}>
              <span className="material-symbols-outlined text-2xl">{stat.icon}</span>
            </div>
            <div>
              <p className="text-sm font-semibold text-gray-500">{stat.label}</p>
              <p className="text-2xl font-extrabold text-gray-900">{stat.value}</p>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default OwnerStats;
