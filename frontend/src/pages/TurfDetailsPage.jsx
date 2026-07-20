import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { turfService } from '../services/turfService';
import { ROUTES } from '../constants/routes';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Skeleton from '../components/ui/Skeleton';
import { formatCurrency, formatTime } from '../utils/formatters';

const fallbackImages = [
  'https://images.unsplash.com/photo-1556056504-5c7696c4c28d?auto=format&fit=crop&w=1400&q=80',
  'https://images.unsplash.com/photo-1577223625816-7546f13df25d?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1526232761682-d26e03ac148e?auto=format&fit=crop&w=1000&q=80',
];

const amenityMap = {
  floodlightsAvailable: ['Floodlights', 'Lights'],
  parkingAvailable: ['Free Parking', 'P'],
  changingRoomsAvailable: ['Changing Rooms', 'Room'],
  washroomsAvailable: ['Washrooms', 'WC'],
  drinkingWaterAvailable: ['Water Station', 'Water'],
  equipmentRentalAvailable: ['Equipment Rental', 'Kit'],
  foodAvailable: ['Food Counter', 'Food'],
  coachingAvailable: ['Coaching', 'Coach'],
};

const getGalleryImages = (turf) => {
  const apiImages = [
    turf?.coverImage,
    ...(turf?.images || []).map((image) => image.imageUrl),
  ].filter(Boolean);

  return [...new Set([...apiImages, ...fallbackImages])].slice(0, 4);
};

const getAmenities = (turf) => {
  const enabledAmenities = Object.entries(amenityMap)
    .filter(([key]) => turf?.[key])
    .map(([, value]) => value);
  const listedAmenities = (turf?.amenities || []).map((amenity) => [amenity, 'Ok']);
  const merged = [...enabledAmenities, ...listedAmenities];

  return merged.length ? merged.slice(0, 8) : [
    ['Floodlights', 'Lights'],
    ['Free Parking', 'P'],
    ['Water Station', 'Water'],
    ['Changing Rooms', 'Room'],
  ];
};

const TurfDetailsPage = () => {
  const { turfId } = useParams();
  const navigate = useNavigate();
  const [turf, setTurf] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadTurf = async () => {
      setLoading(true);
      setError('');

      try {
        const response = await turfService.get(turfId);
        if (!response?.success) {
          throw new Error(response?.message || 'Failed to load venue details');
        }
        setTurf(response.data);
      } catch (e) {
        setError(e.message || 'Failed to load venue details');
      } finally {
        setLoading(false);
      }
    };

    loadTurf();
  }, [turfId]);

  const galleryImages = useMemo(() => getGalleryImages(turf), [turf]);
  const amenities = useMemo(() => getAmenities(turf), [turf]);
  const rating = Number(turf?.averageRating || 4.8).toFixed(1);
  const slotPickerPath = ROUTES.SLOT_PICKER.replace(':turfId', turfId);

  if (loading) {
    return (
      <div className="space-y-8 animate-pulse">
        <Skeleton className="h-[420px] w-full rounded-2xl" />
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <Skeleton className="h-72 rounded-2xl lg:col-span-2" />
          <Skeleton className="h-72 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-2xl border border-red-100 bg-red-50/80 p-8 text-center shadow-sm">
        <h1 className="text-2xl font-extrabold text-red-700">Venue unavailable</h1>
        <p className="mt-2 text-sm font-medium text-red-600">{error}</p>
        <Button className="mt-6" variant="outline" onClick={() => navigate(ROUTES.DASHBOARD)}>
          Back to venues
        </Button>
      </div>
    );
  }

  return (
    <div className="animate-fade-in space-y-8 pb-12">
      <nav className="flex flex-wrap items-center gap-2 text-xs font-bold uppercase tracking-wide text-gray-500">
        <button className="hover:text-primary" onClick={() => navigate(ROUTES.DASHBOARD)}>Home</button>
        <span>/</span>
        <span>Venues</span>
        <span>/</span>
        <span className="text-primary">{turf.name}</span>
      </nav>

      <section className="grid h-[420px] grid-cols-1 gap-3 overflow-hidden rounded-2xl shadow-lg md:h-[560px] md:grid-cols-4 md:grid-rows-2">
        <div className="relative overflow-hidden md:col-span-2 md:row-span-2">
          <img src={galleryImages[0]} alt={`${turf.name} main view`} className="h-full w-full object-cover" />
          <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent" />
          <Badge className="absolute bottom-5 left-5 bg-primary text-white shadow-lg">Premium Venue</Badge>
        </div>
        {galleryImages.slice(1, 3).map((image, index) => (
          <div key={image} className="hidden overflow-hidden md:block">
            <img src={image} alt={`${turf.name} gallery ${index + 2}`} className="h-full w-full object-cover transition-transform duration-500 hover:scale-105" />
          </div>
        ))}
        <div className="hidden overflow-hidden md:col-span-2 md:block">
          <img src={galleryImages[3]} alt={`${turf.name} night play`} className="h-full w-full object-cover transition-transform duration-500 hover:scale-105" />
        </div>
      </section>

      <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-3">
        <section className="space-y-6 lg:col-span-2">
          <div className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm md:p-8">
            <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <h1 className="text-3xl font-extrabold tracking-tight text-primary md:text-4xl">{turf.name}</h1>
                <p className="mt-2 text-sm font-medium text-gray-500">
                  {[turf.address, turf.city, turf.state].filter(Boolean).join(', ')}
                </p>
              </div>
              <div className="inline-flex w-fit items-center gap-2 rounded-full bg-green-100 px-3 py-1 text-sm font-bold text-green-800">
                Rating {rating}{turf.totalReviews ? ` (${turf.totalReviews} reviews)` : ''}
              </div>
            </div>

            <p className="mt-6 max-w-3xl text-sm leading-7 text-gray-600">
              {turf.description || 'A professional-grade sports venue built for fast booking, clean play, and reliable team sessions. Check live availability, pick your slot, and get on the pitch without waiting.'}
            </p>

            <div className="mt-6 grid grid-cols-2 gap-3 border-t border-gray-100 pt-6 md:grid-cols-4">
              {amenities.slice(0, 4).map(([label, icon]) => (
                <div key={label} className="rounded-xl bg-gray-50 p-4 text-center">
                  <span className="block text-xs font-extrabold uppercase text-primary">{icon}</span>
                  <span className="mt-2 block text-xs font-bold text-gray-700">{label}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm md:p-8">
            <h2 className="text-2xl font-extrabold text-primary">Venue Snapshot</h2>
            <div className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <InfoRow label="Sports" value={(turf.sportTypes || []).join(', ') || 'Multi-sport'} />
              <InfoRow label="Surface" value={turf.surfaceType || 'Artificial turf'} />
              <InfoRow label="Venue Type" value={turf.indoorOrOutdoor || 'Outdoor'} />
              <InfoRow label="Capacity" value={turf.capacity ? `${turf.capacity} players` : 'Team friendly'} />
              <InfoRow label="Open Hours" value={`${formatTime(turf.openTime)} - ${formatTime(turf.closeTime)}`} />
              <InfoRow label="Slot Duration" value={`${turf.slotDurationMinutes || 60} minutes`} />
            </div>
          </div>

          <section className="space-y-4">
            <div>
              <h2 className="text-3xl font-extrabold text-primary">Player Feedback</h2>
              <p className="text-sm font-medium text-gray-500">A preview of community trust signals for this venue.</p>
            </div>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <ReviewCard initials="JS" name="Jason Schmidt" text="Clean pitch, easy booking, and solid lights for night games." />
              <ReviewCard initials="AR" name="Anita Ray" text="The venue felt premium and the booking flow was quick." />
            </div>
          </section>
        </section>

        <aside className="sticky top-24 space-y-5">
          <div className="rounded-2xl border border-gray-100 border-t-4 border-t-primary bg-white p-6 shadow-md">
            <h2 className="text-2xl font-extrabold text-primary">Booking Summary</h2>
            <div className="mt-5 space-y-3 text-sm">
              <SummaryRow label="Rate" value={`${formatCurrency(turf.hourlyRate || 0, turf.currency)} / hr`} />
              <SummaryRow label="Status" value={turf.status || 'ACTIVE'} />
              <SummaryRow label="City" value={turf.city || 'Nearby'} />
              <SummaryRow label="Timezone" value={turf.timezone || 'Asia/Kolkata'} />
            </div>
            <div className="mt-6 rounded-xl bg-primary-light p-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-bold text-primary">Starting from</span>
                <span className="text-2xl font-extrabold text-primary">
                  {formatCurrency(turf.hourlyRate || 0, turf.currency)}
                </span>
              </div>
            </div>
            <Button variant="accent" fullWidth className="mt-5 py-4 font-extrabold uppercase" onClick={() => navigate(slotPickerPath)}>
              Check Availability
            </Button>
            <p className="mt-3 text-center text-xs font-medium text-gray-500">Choose a live slot on the next screen.</p>
          </div>

          <div className="rounded-2xl border border-green-100 bg-green-50/80 p-5">
            <p className="text-sm font-extrabold text-primary">Contact Manager</p>
            <p className="mt-1 text-sm font-medium text-gray-700">{turf.contactNumber || turf.email || 'Available after booking'}</p>
          </div>
        </aside>
      </div>
    </div>
  );
};

const InfoRow = ({ label, value }) => (
  <div className="rounded-xl border border-gray-100 bg-gray-50 p-4">
    <span className="block text-[10px] font-bold uppercase tracking-wide text-gray-400">{label}</span>
    <span className="mt-1 block text-sm font-extrabold text-gray-900">{value}</span>
  </div>
);

const SummaryRow = ({ label, value }) => (
  <div className="flex items-center justify-between border-b border-gray-100 pb-3">
    <span className="font-medium text-gray-500">{label}</span>
    <span className="font-bold text-gray-900">{value}</span>
  </div>
);

const ReviewCard = ({ initials, name, text }) => (
  <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
    <div className="flex items-start justify-between gap-4">
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-green-100 text-sm font-extrabold text-primary">
          {initials}
        </div>
        <div>
          <p className="text-sm font-extrabold text-gray-900">{name}</p>
          <p className="text-xs font-medium text-gray-400">Recently played</p>
        </div>
      </div>
      <span className="text-sm text-accent">5.0</span>
    </div>
    <p className="mt-4 text-sm leading-6 text-gray-600">"{text}"</p>
  </div>
);

export default TurfDetailsPage;
