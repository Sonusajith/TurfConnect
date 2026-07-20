import React, { useEffect, useState } from 'react';
import { Card, CardContent } from '../components/ui/Card';
import { useAnalytics } from '../hooks/useAnalytics';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { formatCurrency } from '../utils/formatters';

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
  const { data, loading, error, fetchPlatformAnalytics } = useAnalytics();

  useEffect(() => {
    // Fetch last 30 days of data
    const endDate = new Date().toISOString().split('T')[0];
    const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    fetchPlatformAnalytics(startDate, endDate);
  }, [fetchPlatformAnalytics]);

  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-7xl mx-auto pb-24 md:pb-8">
      <div className="mb-8 border-b border-outline-variant/30 pb-6">
        <h1 className="font-headline-lg text-headline-lg text-on-surface">Admin Dashboard</h1>
        <p className="text-on-surface-variant mt-2">Franchise overview and revenue analytics (Last 30 Days).</p>
      </div>

      {error && (
        <div className="mb-6 rounded-lg bg-error/10 p-4 text-error">
          <p className="font-bold">Failed to load analytics</p>
          <p className="text-sm">{error}</p>
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-gutter mb-8">
          {[1, 2, 3, 4].map(i => <div key={i} className="h-28"><LoadingSkeleton variant="rectangular" className="h-full rounded-xl" /></div>)}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-gutter mb-8">
          <StatCard title="Total Revenue" value={data ? formatCurrency(data.totalRevenue) : '₹0'} icon="account_balance_wallet" colorClass="bg-green-100 text-green-700" />
          <StatCard title="Total Bookings" value={data?.totalBookings || '0'} icon="event_available" colorClass="bg-blue-100 text-blue-700" />
          <StatCard title="Conf. Rate" value={data ? `${(data.confirmationRate * 100).toFixed(1)}%` : '0%'} icon="pie_chart" colorClass="bg-purple-100 text-purple-700" />
          <StatCard title="Avg Rev / Booking" value={data ? formatCurrency(data.averageRevenuePerBooking) : '₹0'} icon="payments" colorClass="bg-orange-100 text-orange-700" />
        </div>
      )}

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
