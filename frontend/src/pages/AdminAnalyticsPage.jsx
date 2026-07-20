import React from 'react';
import Card, { CardContent } from '../components/ui/Card';

const StatCard = ({ title, value, icon, trend, colorClass }) => (
  <Card className="hover:shadow-md transition-shadow border border-outline-variant/30">
    <CardContent className="p-6 flex items-center gap-4">
      <div className={`w-14 h-14 rounded-xl flex items-center justify-center ${colorClass}`}>
        <span className="material-symbols-outlined text-3xl">{icon}</span>
      </div>
      <div>
        <p className="text-on-surface-variant font-label-md mb-1">{title}</p>
        <h4 className="font-headline-lg text-2xl text-on-surface">{value}</h4>
        {trend && (
          <p className={`text-xs mt-1 font-bold ${trend > 0 ? 'text-green-600' : 'text-error'}`}>
            {trend > 0 ? '↑' : '↓'} {Math.abs(trend)}% from last month
          </p>
        )}
      </div>
    </CardContent>
  </Card>
);

const AdminAnalyticsPage = () => {
  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-7xl mx-auto pb-24 md:pb-8">
      <div className="mb-8 border-b border-outline-variant/30 pb-6">
        <h1 className="font-headline-lg text-headline-lg text-on-surface">Admin Dashboard</h1>
        <p className="text-on-surface-variant mt-2">Franchise overview and revenue analytics.</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-gutter mb-8">
        <StatCard title="Total Revenue" value="₹1.4M" icon="account_balance_wallet" trend={12} colorClass="bg-green-100 text-green-700" />
        <StatCard title="Total Bookings" value="1,248" icon="event_available" trend={8} colorClass="bg-blue-100 text-blue-700" />
        <StatCard title="Avg Occupancy" value="68%" icon="pie_chart" trend={-2} colorClass="bg-purple-100 text-purple-700" />
        <StatCard title="Active Turfs" value="14" icon="stadium" colorClass="bg-orange-100 text-orange-700" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
        <div className="lg:col-span-2">
          <Card className="h-96 border border-outline-variant/30 flex items-center justify-center bg-surface-container-lowest">
            <div className="text-center">
              <span className="material-symbols-outlined text-5xl text-on-surface-variant opacity-30 mb-2">bar_chart</span>
              <p className="text-on-surface-variant font-label-md">Revenue Growth Chart Placeholder</p>
            </div>
          </Card>
        </div>
        <div>
          <Card className="h-96 border border-outline-variant/30 flex items-center justify-center bg-surface-container-lowest">
            <div className="text-center">
              <span className="material-symbols-outlined text-5xl text-on-surface-variant opacity-30 mb-2">donut_large</span>
              <p className="text-on-surface-variant font-label-md">Sport Popularity Placeholder</p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default AdminAnalyticsPage;
