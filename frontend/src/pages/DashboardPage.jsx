import React, { useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { Link } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import { Card, CardContent } from '../components/ui/Card';
import useBookings from '../hooks/useBookings';
import useTeams from '../hooks/useTeams';
import BookingTable from '../features/booking/BookingTable';

const DashboardPage = () => {
  const { user } = useAuth();
  const { bookings, loading: bookingsLoading, error: bookingsError, refetch: refetchBookings } = useBookings();
  const { teams, invitations, loading: teamsLoading, fetchTeams, fetchInvitations } = useTeams();

  useEffect(() => {
    refetchBookings();
    fetchTeams();
    fetchInvitations();
  }, [refetchBookings, fetchTeams, fetchInvitations]);

  const upcomingBookings = bookings?.filter(b => b.status === 'CONFIRMED') || [];

  const stats = [
    { label: 'Upcoming Matches', value: bookingsLoading ? '-' : upcomingBookings.length, icon: 'sports_soccer' },
    { label: 'Active Teams', value: teamsLoading ? '-' : (teams?.length || 0), icon: 'groups' },
    { label: 'Pending Invites', value: teamsLoading ? '-' : (invitations?.length || 0), icon: 'mail' },
  ];

  return (
    <div className="animate-fade-in space-y-8 pb-12">
      <section className="space-y-4">
        <h1 className="text-4xl font-extrabold leading-tight tracking-tight text-primary-dark sm:text-5xl">
          Welcome back, {user?.name || 'Athlete'}
        </h1>
        <p className="max-w-xl text-base font-medium text-gray-600">
          Here's a quick overview of your sports activities.
        </p>
      </section>

      <section className="grid gap-6 sm:grid-cols-3">
        {stats.map(({ label, value, icon }) => (
          <Card key={label} className="border border-outline-variant/30 hover:shadow-md transition-all">
            <CardContent className="flex items-center p-6 gap-4">
              <div className="bg-primary/10 text-primary rounded-full p-4 flex items-center justify-center">
                <span className="material-symbols-outlined text-2xl">{icon}</span>
              </div>
              <div>
                <p className="text-sm font-semibold text-gray-500">{label}</p>
                <p className="mt-1 text-3xl font-extrabold text-gray-950">{value}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </section>

      <section className="mt-8 rounded-2xl bg-gradient-to-r from-primary to-primary-dark p-8 text-white shadow-lg">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
          <div>
            <h2 className="text-2xl font-bold mb-2">Ready to play?</h2>
            <p className="text-primary-100 max-w-md text-sm leading-relaxed">
              Discover top-rated sports venues near you, invite your friends, and book your slot in seconds.
            </p>
          </div>
          <Link 
            to={ROUTES.EXPLORE}
            className="inline-block bg-white text-primary font-bold px-8 py-3 rounded-full hover:bg-gray-50 transition-colors shadow-sm text-center"
          >
            Explore Turfs
          </Link>
        </div>
      </section>

      <section className="mt-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Your Upcoming Bookings</h2>
        <BookingTable bookings={upcomingBookings} loading={bookingsLoading} error={bookingsError} />
      </section>
    </div>
  );
};

export default DashboardPage;
