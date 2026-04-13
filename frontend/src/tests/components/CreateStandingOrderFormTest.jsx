import { render, screen, fireEvent, act } from '@testing-library/react';
import { vi } from 'vitest';
import CreateStandingOrderForm from '../../components/standingorders/CreateStandingOrderForm';

vi.mock('../../hooks/useStandingOrders', () => ({
  useCreateStandingOrder: vi.fn(),
}));

import { useCreateStandingOrder } from '../../hooks/useStandingOrders';

describe('CreateStandingOrderForm', () => {
  it('submit button is disabled when required fields are empty', () => {
    useCreateStandingOrder.mockReturnValue({ mutate: vi.fn(), isPending: false, isError: false });

    render(<CreateStandingOrderForm accountId={1} />);
    expect(screen.getByRole('button', { name: /create standing order/i })).toBeDisabled();
  });

  it('displays field-level error when server returns 400 with field', async () => {
    const mutateFn = vi.fn();
    let capturedCallbacks = {};
    useCreateStandingOrder.mockReturnValue({
      mutate: (payload, callbacks) => {
        capturedCallbacks = callbacks;
        mutateFn(payload);
      },
      isPending: false,
      isError: false,
    });

    render(<CreateStandingOrderForm accountId={1} />);

    // Fill in required fields
    fireEvent.change(screen.getByLabelText(/payee account/i), { target: { value: 'GB82WEST12345698765432', name: 'payeeAccount' } });
    fireEvent.change(screen.getByLabelText(/payee name/i), { target: { value: 'Jane', name: 'payeeName' } });
    fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: '100', name: 'amount' } });
    fireEvent.change(screen.getByLabelText(/frequency/i), { target: { value: 'MONTHLY', name: 'frequency' } });
    fireEvent.change(screen.getByLabelText(/start date/i), { target: { value: '2026-12-01T10:00', name: 'startDate' } });

    fireEvent.click(screen.getByRole('button', { name: /create standing order/i }));

    // Simulate server error with field
    await act(async () => {
      capturedCallbacks.onError({
        response: { data: { code: 'ERR_CHECKSUM_FAILURE', message: 'Invalid payee account checksum', field: 'payeeAccount' } },
      });
    });

    expect(screen.getByText('Invalid payee account checksum')).toBeInTheDocument();
  });
});
