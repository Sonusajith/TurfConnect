import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import TurfList from '../features/turfs/TurfList';

describe('TurfList Component', () => {
  test('renders loading skeleton items when loading is true', () => {
    const { container } = render(
      <TurfList turfs={[]} loading={true} error={null} onViewSlots={vi.fn()} />
    );
    // Grid skeleton has animation class animate-pulse
    const skeletons = container.getElementsByClassName('animate-pulse');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  test('renders error state panels when error is encountered', () => {
    render(
      <TurfList
        turfs={[]}
        loading={false}
        error="Network error encountered"
        onViewSlots={vi.fn()}
      />
    );
    expect(screen.getByText(/failed to load turfs/i)).toBeInTheDocument();
    expect(screen.getByText(/network error encountered/i)).toBeInTheDocument();
  });

  test('renders empty placeholder if list is empty', () => {
    render(
      <TurfList turfs={[]} loading={false} error={null} onViewSlots={vi.fn()} />
    );
    expect(screen.getByText(/no turfs found/i)).toBeInTheDocument();
  });

  test('renders lists of turf cards with titles and buttons', () => {
    const mockTurfs = [
      {
        id: '1',
        name: 'Camp Nou Sports Arena',
        sportTypes: ['FOOTBALL'],
        address: '123 Arena Rd',
        city: 'Bengaluru',
        hourlyRate: 120.0,
        currency: 'INR',
        openTime: '06:00',
        closeTime: '22:00',
      },
    ];

    render(
      <TurfList
        turfs={mockTurfs}
        loading={false}
        error={null}
        onViewSlots={vi.fn()}
      />
    );

    expect(screen.getByText('Camp Nou Sports Arena')).toBeInTheDocument();
    expect(screen.getByText(/120.00/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /book now/i })).toBeInTheDocument();
  });
});
