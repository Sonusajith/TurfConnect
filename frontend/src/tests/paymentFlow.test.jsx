import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import PaymentModal from '../features/payment/PaymentModal';

describe('PaymentModal Component', () => {
  const mockBooking = {
    id: 'booking-1',
    totalPrice: 120.0,
  };

  test('validates inputs fields before simulation payment requests', async () => {
    render(
      <PaymentModal
        isOpen={true}
        onClose={vi.fn()}
        booking={mockBooking}
        onPaymentComplete={vi.fn()}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /simulate pay/i }));
    expect(await screen.findByText(/please fill in all mock card fields/i)).toBeInTheDocument();
  });

  test('triggers callback upon filling input values and clicking button', async () => {
    const handleComplete = vi.fn().mockResolvedValue({ success: true });
    
    render(
      <PaymentModal
        isOpen={true}
        onClose={vi.fn()}
        booking={mockBooking}
        onPaymentComplete={handleComplete}
      />
    );

    fireEvent.change(screen.getByLabelText(/card number/i), { target: { value: '4111222233334444' } });
    fireEvent.change(screen.getByLabelText(/expiry date/i), { target: { value: '12/28' } });
    fireEvent.change(screen.getByLabelText(/cvv/i), { target: { value: '123' } });

    fireEvent.click(screen.getByRole('button', { name: /simulate pay/i }));

    await waitFor(() => {
      expect(handleComplete).toHaveBeenCalledWith(mockBooking);
      expect(screen.getByText(/payment successful/i)).toBeInTheDocument();
    });
  });
});
