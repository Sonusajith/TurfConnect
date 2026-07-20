import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';

const OwnerTurfList = ({ turfs, loading, error }) => {
  if (loading) return <div className="text-gray-500 font-medium">Loading venues...</div>;
  if (error) return <div className="text-red-500 font-medium">Error: {error}</div>;

  if (!turfs || turfs.length === 0) {
    return (
      <div className="text-center py-16 bg-white rounded-2xl border border-primary/10 shadow-sm">
        <span className="material-symbols-outlined text-5xl text-gray-300 mb-3">stadium</span>
        <p className="text-gray-500 font-medium">You haven't listed any venues yet.</p>
        <button className="mt-4 px-4 py-2 bg-primary text-white rounded-lg font-bold text-sm">Add Your First Venue</button>
      </div>
    );
  }

  return (
    <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      {turfs.map((turf, i) => (
        <Card key={turf.id || i} className="overflow-hidden border border-outline-variant/30 hover:border-primary/40 transition-all flex flex-col h-full">
          <div className="h-40 bg-gray-200 relative">
            {turf.images?.[0] ? (
              <img src={turf.images[0]} alt={turf.name} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-gray-100 text-gray-400">
                <span className="material-symbols-outlined text-4xl">image</span>
              </div>
            )}
            <div className="absolute top-3 right-3">
              <Badge variant={turf.active ? 'success' : 'default'}>{turf.active ? 'Active' : 'Draft'}</Badge>
            </div>
          </div>
          <CardContent className="p-5 flex-1 flex flex-col">
            <h3 className="text-lg font-bold text-gray-900 leading-tight">{turf.name}</h3>
            <p className="text-sm text-gray-500 mt-1 line-clamp-1">{turf.location}</p>
            
            <div className="mt-4 flex flex-wrap gap-2">
              {turf.sportTypes?.map(sport => (
                <span key={sport} className="px-2 py-1 bg-gray-100 text-gray-600 text-[10px] font-bold uppercase tracking-wider rounded">
                  {sport}
                </span>
              ))}
            </div>

            <div className="mt-auto pt-4 border-t border-gray-50 flex items-center justify-between">
              <div>
                <p className="text-[10px] font-bold text-gray-400 uppercase tracking-wide">Hourly Rate</p>
                <p className="font-extrabold text-primary-dark">₹{turf.hourlyRate}</p>
              </div>
              <button className="text-sm font-bold text-accent hover:text-accent-dark transition-colors">
                Manage Slots
              </button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default OwnerTurfList;
