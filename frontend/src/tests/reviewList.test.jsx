import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, expect, test } from 'vitest';
import ReviewList from '../features/reviews/ReviewList';

describe('ReviewList', () => {
  test('shows real reviewer names from review responses', () => {
    render(
      <ReviewList
        reviews={[
          {
            id: 'review-1',
            userId: 'user-1',
            userName: 'Saif Player',
            userEmail: 'saif.player@turfconnect.test',
            rating: 5,
            comment: 'Great turf.',
            createdAt: '2026-07-21T10:00:00',
          },
        ]}
        loading={false}
        error={null}
      />
    );

    expect(screen.getByText('Saif Player')).toBeInTheDocument();
    expect(screen.queryByText(/Player USER/i)).not.toBeInTheDocument();
  });

  test('uses email as a readable fallback when only email is available', () => {
    render(
      <ReviewList
        reviews={[
          {
            id: 'review-2',
            userId: 'user-2',
            userEmail: 'sonu.owner@turfconnect.test',
            rating: 4,
            comment: 'Clean venue.',
            createdAt: '2026-07-21T10:00:00',
          },
        ]}
        loading={false}
        error={null}
      />
    );

    expect(screen.getByText('Sonu Owner')).toBeInTheDocument();
  });
});
