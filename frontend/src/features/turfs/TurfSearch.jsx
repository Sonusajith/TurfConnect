import React, { useState } from 'react';
import Button from '../../components/ui/Button';

const TurfSearch = ({ onSearch }) => {
  const [city, setCity] = useState('');
  const [sport, setSport] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [minRating, setMinRating] = useState('');
  const [indoorOrOutdoor, setIndoorOrOutdoor] = useState('');
  const [floodlights, setFloodlights] = useState(false);
  const [sort, setSort] = useState('createdAt:desc');
  const [isLocating, setIsLocating] = useState(false);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    const params = {};
    if (city.trim()) params.city = city.trim();
    if (sport.trim()) params.sport = sport.trim();
    if (minPrice) params.minPrice = minPrice;
    if (maxPrice) params.maxPrice = maxPrice;
    if (minRating) params.minRating = minRating;
    if (indoorOrOutdoor) params.indoorOrOutdoor = indoorOrOutdoor;
    if (floodlights) params.floodlights = true;
    const [sortBy, sortDirection] = sort.split(':');
    params.sortBy = sortBy;
    params.sortDirection = sortDirection;
    onSearch(params);
  };

  const handleClear = () => {
    setCity('');
    setSport('');
    setMinPrice('');
    setMaxPrice('');
    setMinRating('');
    setIndoorOrOutdoor('');
    setFloodlights(false);
    setSort('createdAt:desc');
    onSearch({});
  };

  const handleGetLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser');
      return;
    }

    setIsLocating(true);
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const { latitude, longitude } = position.coords;
          const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=10`);
          const data = await res.json();

          const locationName =
            data.address.city ||
            data.address.town ||
            data.address.state_district ||
            data.address.county ||
            '';

          if (locationName) {
            setCity(locationName);
          } else {
            alert('Could not determine your city from location.');
          }
        } catch (error) {
          alert('Error fetching location data.');
        } finally {
          setIsLocating(false);
        }
      },
      () => {
        alert('Unable to retrieve your location. Please check browser permissions.');
        setIsLocating(false);
      }
    );
  };

  return (
    <div className="rounded-lg border border-primary/10 bg-[#dcebf3] p-2 shadow-sm">
      <form onSubmit={handleSearchSubmit} className="space-y-2">
        <div className="grid gap-2 md:grid-cols-[minmax(0,1fr)_minmax(0,0.8fr)_auto]">
        <div className="relative w-full">
          <label htmlFor="citySearch" className="sr-only">
            Location
          </label>
          <div className="absolute inset-y-0 left-0 flex items-center pl-4 text-primary">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657 13.414 20.9a2 2 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z" />
            </svg>
          </div>
          <input
            id="citySearch"
            type="text"
            placeholder="Location"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            className="block h-14 w-full rounded-lg border border-transparent bg-transparent pl-12 pr-12 text-sm font-semibold text-gray-800 placeholder-gray-600 focus:border-primary focus:bg-white focus:outline-none focus:ring-2 focus:ring-primary/20"
            aria-label="City"
          />
          <button
            type="button"
            onClick={handleGetLocation}
            disabled={isLocating}
            className="absolute inset-y-0 right-2 my-auto flex items-center p-2 text-primary transition hover:text-primary-dark disabled:opacity-50"
            title="Detect My Location"
          >
            {isLocating ? (
              <svg className="h-5 w-5 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"></path>
              </svg>
            ) : (
              <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5A2.5 2.5 0 1 1 12 6a2.5 2.5 0 0 1 0 5.5z" />
              </svg>
            )}
          </button>
        </div>

        <div className="relative w-full">
          <label htmlFor="sportSelect" className="sr-only">
            Sport Type
          </label>
          <div className="absolute inset-y-0 left-0 flex items-center pl-4 text-primary">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14.752 11.168 11.555 9.036A1 1 0 0 0 10 9.87v4.263a1 1 0 0 0 1.555.832l3.197-2.132a1 1 0 0 0 0-1.664z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" />
            </svg>
          </div>
          <select
            id="sportSelect"
            value={sport}
            onChange={(e) => setSport(e.target.value)}
            className="block h-14 w-full appearance-none rounded-lg border border-transparent bg-transparent pl-12 pr-10 text-sm font-semibold text-gray-800 focus:border-primary focus:bg-white focus:outline-none focus:ring-2 focus:ring-primary/20"
            aria-label="Sport Type"
          >
            <option value="">Sport</option>
            <option value="FOOTBALL">Football</option>
            <option value="CRICKET">Cricket</option>
            <option value="BADMINTON">Badminton</option>
            <option value="TENNIS">Tennis</option>
          </select>
          <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
            <svg className="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="m19 9-7 7-7-7" />
            </svg>
          </div>
        </div>

        <Button type="submit" variant="primary" className="h-14 w-full rounded-lg px-8 text-sm font-extrabold shadow-md md:w-auto">
          Search
        </Button>
        </div>

        <div className="grid gap-2 md:grid-cols-5">
          <input
            type="number"
            min="0"
            placeholder="Min price"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            className="h-11 rounded-lg border border-transparent bg-white/70 px-3 text-sm font-semibold text-gray-800 outline-none transition placeholder:text-gray-500 focus:border-primary focus:bg-white focus:ring-2 focus:ring-primary/20"
            aria-label="Minimum price"
          />
          <input
            type="number"
            min="0"
            placeholder="Max price"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            className="h-11 rounded-lg border border-transparent bg-white/70 px-3 text-sm font-semibold text-gray-800 outline-none transition placeholder:text-gray-500 focus:border-primary focus:bg-white focus:ring-2 focus:ring-primary/20"
            aria-label="Maximum price"
          />
          <select
            value={minRating}
            onChange={(e) => setMinRating(e.target.value)}
            className="h-11 rounded-lg border border-transparent bg-white/70 px-3 text-sm font-semibold text-gray-800 outline-none transition focus:border-primary focus:bg-white focus:ring-2 focus:ring-primary/20"
            aria-label="Minimum rating"
          >
            <option value="">Any rating</option>
            <option value="3">3.0+</option>
            <option value="4">4.0+</option>
            <option value="4.5">4.5+</option>
          </select>
          <select
            value={indoorOrOutdoor}
            onChange={(e) => setIndoorOrOutdoor(e.target.value)}
            className="h-11 rounded-lg border border-transparent bg-white/70 px-3 text-sm font-semibold text-gray-800 outline-none transition focus:border-primary focus:bg-white focus:ring-2 focus:ring-primary/20"
            aria-label="Venue type"
          >
            <option value="">Any venue</option>
            <option value="INDOOR">Indoor</option>
            <option value="OUTDOOR">Outdoor</option>
          </select>
          <select
            value={sort}
            onChange={(e) => setSort(e.target.value)}
            className="h-11 rounded-lg border border-transparent bg-white/70 px-3 text-sm font-semibold text-gray-800 outline-none transition focus:border-primary focus:bg-white focus:ring-2 focus:ring-primary/20"
            aria-label="Sort venues"
          >
            <option value="createdAt:desc">Newest</option>
            <option value="hourlyRate:asc">Price low to high</option>
            <option value="hourlyRate:desc">Price high to low</option>
            <option value="averageRating:desc">Top rated</option>
          </select>
        </div>

        <div className="flex flex-wrap items-center justify-between gap-3 px-1 pb-1">
          <label className="inline-flex cursor-pointer items-center gap-2 text-sm font-bold text-primary-dark">
            <input
              type="checkbox"
              checked={floodlights}
              onChange={(e) => setFloodlights(e.target.checked)}
              className="h-4 w-4 rounded border-primary/30 text-primary focus:ring-primary"
            />
            Floodlights available
          </label>
          <button
            type="button"
            onClick={handleClear}
            className="text-sm font-extrabold text-primary transition hover:text-primary-dark"
          >
            Clear filters
          </button>
        </div>
      </form>
    </div>
  );
};

export default TurfSearch;
