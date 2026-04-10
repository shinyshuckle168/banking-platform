import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import StandingOrderItem from '../../components/standingorders/StandingOrderItem';

vi.mock('../../hooks/useStandingOrders', () => ({
  useCancelStandingOrder: vi.fn(),
}));

import { useCancelStandingOrder } from '../../hooks/useStandingOrders';

const makeOrder = (nextRunDate) => ({
  standingOrderId: 'so-1',
  payeeAccount: 'GB82WEST12345698765432',
  payeeName: 'John Doe',
  amount: 100,
  frequency: 'MONTHLY',
  status: 'ACTIVE',
  nextRunDate,
  startDate: '2026-01-01T00:00:00',
  reference: 'Rent',
});

describe('StandingOrderItem', () => {
  it('cancel button is disabled when nextRunDate is within 24h', () => {
    const mutate = vi.fn();
    useCancelStandingOrder.mockReturnValue({ mutate, isPending: false });

    // 1 hour from now
    const soon = new Date(Date.now() + 60 * 60 * 1000).toISOString();
    render(<StandingOrderItem order={makeOrder(soon)} accountId={1} />);

    expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
  });

  it('cancel button is enabled and calls mutation when > 24h', () => {
    const mutate = vi.fn();
    useCancelStandingOrder.mockReturnValue({ mutate, isPending: false });

    // 48 hours from now
    const later = new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString();
    render(<StandingOrderItem order={makeOrder(later)} accountId={1} />);

    const btn = screen.getByRole('button', { name: /cancel/i });
    expect(btn).not.toBeDisabled();
    fireEvent.click(btn);
    expect(mutate).toHaveBeenCalledWith('so-1');
  });

  it('renders all expected fields', () => {
    useCancelStandingOrder.mockReturnValue({ mutate: vi.fn(), isPending: false });
    const later = new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString();
    render(<StandingOrderItem order={makeOrder(later)} accountId={1} />);

    expect(screen.getByText(/john doe/i)).toBeInTheDocument();
    expect(screen.getByText(/GB82WEST12345698765432/)).toBeInTheDocument();
    expect(screen.getByText(/MONTHLY/)).toBeInTheDocument();
    expect(screen.getByText(/ACTIVE/)).toBeInTheDocument();
  });
});
