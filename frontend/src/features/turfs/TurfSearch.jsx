import React, { useState } from 'react';
import Button from '../../components/ui/Button';

const TurfSearch = ({ onSearch }) => {
  const [city, setCity] = useState('');
  const [sport, setSport] = useState('');
  const [isLocating, setIsLocating] = useState(false);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    const params = {};
    if (city.trim()) params.city = city.trim();
    if (sport.trim()) params.sport = sport.trim();
    onSearch(params);
  };

  const handleGetLocation = () => {
    if (!navigator.geolocation) {
      alert("Geolocation is not supported by your browser");
      return;
    }
    setIsLocating(true);
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const { latitude, longitude } = position.coords;
          const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=10`);
          const data = await res.json();
          
          const locationName = data.address.city || data.address.town || data.address.state_district || data.address.county || '';
          if (locationName) {
            setCity(locationName);
          } else {
            alert("Could not determine your city from location.");
          }
        } catch (error) {
          alert("Error fetching location data.");
        } finally {
          setIsLocating(false);
        }
      },
      (error) => {
        alert("Unable to retrieve your location. Please check browser permissions.");
        setIsLocating(false);
      }
    );
  };

  return (
    <div className="bg-white p-6 md:p-8 rounded-3xl border border-gray-100 shadow-xl shadow-primary/5 -mt-8 relative z-20 mx-4 md:mx-8 lg:mx-12">
      <form onSubmit={handleSearchSubmit} className="flex flex-col md:flex-row gap-5 items-end">
        <div className="flex-1 w-full relative">
          <label htmlFor="citySearch" className="block text-sm font-bold text-gray-700 mb-2">
            Location
          </label>
          <div className="relative flex items-center">
            <input
              id="citySearch"
              type="text"
              placeholder="Enter city (e.g., Bengaluru)"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              className="block w-full pl-4 pr-12 py-3 border border-gray-200 rounded-xl leading-5 bg-gray-50 placeholder-gray-400 focus:outline-none focus:bg-white focus:ring-2 focus:ring-primary focus:border-primary transition-all sm:text-sm"
              aria-label="City"
            />
            <button 
              type="button"
              onClick={handleGetLocation}
              disabled={isLocating}
              className="absolute inset-y-0 right-2 flex items-center p-1 my-auto text-primary hover:text-primary-dark transition-colors disabled:opacity-50"
              title="Detect My Location"
            >
              {isLocating ? (
                <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                   <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                </svg>
              )}
            </button>
          </div>
        </div>

        <div className="flex-1 w-full">
          <label htmlFor="sportSelect" className="block text-sm font-bold text-gray-700 mb-2">
            Sport Type
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"></path><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            </div>
            <select
              id="sportSelect"
              value={sport}
              onChange={(e) => setSport(e.target.value)}
              className="block w-full pl-10 pr-10 py-3 border border-gray-200 rounded-xl leading-5 bg-gray-50 focus:outline-none focus:bg-white focus:ring-2 focus:ring-primary focus:border-primary transition-all sm:text-sm appearance-none"
              aria-label="Sport Type"
            >
              <option value="">Any Sport</option>
              <option value="FOOTBALL">Football</option>
              <option value="CRICKET">Cricket</option>
              <option value="BADMINTON">Badminton</option>
              <option value="TENNIS">Tennis</option>
            </select>
            <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
              <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path></svg>
            </div>
          </div>
        </div>

        <Button type="submit" variant="primary" className="w-full md:w-auto py-3 px-8 shadow-md">
          Search
        </Button>
      </form>
    </div>
  );
};

export default TurfSearch;
