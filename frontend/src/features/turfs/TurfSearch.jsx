import React, { useState } from 'react';
import Input from '../../components/Input';
import Button from '../../components/Button';

const TurfSearch = ({ onSearch }) => {
  const [city, setCity] = useState('');
  const [sport, setSport] = useState('');

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    const params = {};
    if (city.trim()) params.city = city.trim();
    if (sport.trim()) params.sport = sport.trim();
    onSearch(params);
  };

  return (
    <form onSubmit={handleSearchSubmit} className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex flex-col md:flex-row gap-4 items-end">
      <Input
        label="City"
        id="citySearch"
        placeholder="e.g. Bengaluru"
        value={city}
        onChange={(e) => setCity(e.target.value)}
        className="flex-1"
      />

      <div className="flex flex-col gap-1.5 w-full flex-1">
        <label htmlFor="sportSelect" className="text-sm font-semibold text-gray-700">
          Sport Type
        </label>
        <select
          id="sportSelect"
          value={sport}
          onChange={(e) => setSport(e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-light focus:border-primary transition-all bg-white"
        >
          <option value="">Any Sport</option>
          <option value="FOOTBALL">Football</option>
          <option value="CRICKET">Cricket</option>
          <option value="BADMINTON">Badminton</option>
          <option value="TENNIS">Tennis</option>
        </select>
      </div>

      <Button type="submit" variant="primary" className="w-full md:w-auto px-6 py-2.5">
        🔍 Search
      </Button>
    </form>
  );
};

export default TurfSearch;
