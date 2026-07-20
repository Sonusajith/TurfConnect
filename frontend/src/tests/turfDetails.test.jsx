import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, test, vi } from 'vitest';
import TurfDetailsPage from '../pages/TurfDetailsPage';
import { turfService } from '../services/turfService';

vi.mock('../services/turfService', () => ({
  turfService: {
    get: vi.fn(),
  },
}));

describe('TurfDetailsPage', () => {
  test('renders venue details and booking CTA from API data', async () => {
    turfService.get.mockResolvedValue({
      success: true,
      data: {
        id: 'turf-1',
        name: 'Elite Arena South',
        description: 'Premium 7-a-side football turf with night lighting.',
        sportTypes: ['FOOTBALL'],
        address: '122 Olympic Way',
        city: 'Bengaluru',
        state: 'Karnataka',
        hourlyRate: 900,
        currency: 'INR',
        openTime: '06:00',
        closeTime: '22:00',
        slotDurationMinutes: 60,
        surfaceType: 'Synthetic turf',
        indoorOrOutdoor: 'Outdoor',
        parkingAvailable: true,
        drinkingWaterAvailable: true,
        averageRating: 4.9,
        totalReviews: 128,
        status: 'ACTIVE',
      },
    });

    render(
      <MemoryRouter initialEntries={['/turfs/turf-1']}>
        <Routes>
          <Route path="/turfs/:turfId" element={<TurfDetailsPage />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /elite arena south/i })).toBeInTheDocument();
    });

    expect(screen.getByText(/premium 7-a-side football turf/i)).toBeInTheDocument();
    expect(screen.getByText(/synthetic turf/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /check availability/i })).toBeInTheDocument();
  });
});
