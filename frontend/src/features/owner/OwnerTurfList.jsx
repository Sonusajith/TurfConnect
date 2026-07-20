import React from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import { formatCurrency } from '../../utils/formatters';

const getTurfImage = (turf) => turf.coverImage || turf.images?.[0]?.imageUrl || turf.images?.[0];

const OwnerTurfList = ({ turfs, loading, error }) => {
  if (loading) return <div className="font-medium text-gray-500">Loading venues...</div>;
  if (error) return <div className="font-medium text-red-500">Error: {error}</div>;

  if (!turfs || turfs.length === 0) {
    return (
      <div className="rounded-2xl border border-primary/10 bg-white py-16 text-center shadow-sm">
        <span className="material-symbols-outlined mb-3 text-5xl text-gray-300">stadium</span>
        <p className="font-medium text-gray-500">You haven't listed any venues yet.</p>
        <button title="Managing venues requires the Owner Admin App" className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-bold text-white opacity-50 cursor-not-allowed">Add Your First Venue</button>
      </div>
    );
  }

  return (
    <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      {turfs.map((turf, index) => {
        const image = getTurfImage(turf);
        const isActive = turf.status === 'ACTIVE' || turf.active;
        const location = [turf.address, turf.city].filter(Boolean).join(', ') || turf.location || 'Location unavailable';

        return (
          <Card key={turf.id || index} className="flex h-full flex-col overflow-hidden border border-outline-variant/30 transition-all hover:border-primary/40">
            <div className="relative h-40 bg-gray-200">
              {image ? (
                <img src={image} alt={turf.name} className="h-full w-full object-cover" />
              ) : (
                <div className="flex h-full w-full items-center justify-center bg-gray-100 text-gray-400">
                  <span className="material-symbols-outlined text-4xl">image</span>
                </div>
              )}
              <div className="absolute right-3 top-3">
                <Badge variant={isActive ? 'success' : 'default'}>{isActive ? 'Active' : 'Draft'}</Badge>
              </div>
            </div>

            <CardContent className="flex flex-1 flex-col p-5">
              <h3 className="text-lg font-bold leading-tight text-gray-900">{turf.name}</h3>
              <p className="mt-1 line-clamp-1 text-sm text-gray-500">{location}</p>

              <div className="mt-4 flex flex-wrap gap-2">
                {turf.sportTypes?.map((sport) => (
                  <span key={sport} className="rounded bg-gray-100 px-2 py-1 text-[10px] font-bold uppercase tracking-wider text-gray-600">
                    {sport}
                  </span>
                ))}
              </div>

              <div className="mt-auto flex items-center justify-between border-t border-gray-50 pt-4">
                <div>
                  <p className="text-[10px] font-bold uppercase tracking-wide text-gray-400">Hourly Rate</p>
                  <p className="font-extrabold text-primary-dark">{formatCurrency(turf.hourlyRate || 0, turf.currency)}</p>
                </div>
                <Link to={`/turfs/${turf.id}/slots`} className="text-sm font-bold text-accent transition-colors hover:text-accent-dark">
                  Manage Slots
                </Link>
              </div>
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
};

export default OwnerTurfList;
