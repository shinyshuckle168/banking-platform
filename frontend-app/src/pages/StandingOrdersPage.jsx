import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import {
  useCancelStandingOrder,
  useCreateStandingOrder,
  useStandingOrders
} from '../hooks/useGroup3';
import { STANDING_ORDER_FREQUENCIES, emptyStandingOrderForm } from '../types';

function validateStandingOrderForm(form) {
  if (!form.payeeAccount?.trim()) {
    return 'Payee account is required.';
  }

  if (!form.payeeName?.trim()) {
    return 'Payee name is required.';
  }

  if (!form.amount || Number.parseFloat(form.amount) <= 0) {
    return 'Amount must be greater than zero.';
  }

  if (!form.reference?.trim()) {
    return 'Reference is required.';
  }

  const reference = form.reference.trim();
  if (!/^[a-zA-Z0-9]{1,18}$/.test(reference)) {
    return 'Reference must be 1 to 18 alphanumeric characters.';
  }

  const startDateTime = new Date(form.startDate);
  const now = new Date();
  const twentyFourHoursLater = new Date(now.getTime() + 24 * 60 * 60 * 1000);

  if (startDateTime <= twentyFourHoursLater) {
    return 'Start date must be at least 24 hours in the future.';
  }

  if (form.endDate) {
    const endDateTime = new Date(form.endDate);
    if (endDateTime <= startDateTime) {
      return 'End date must be after the start date.';
    }
  }

  return null;
}

export function StandingOrdersPage() {
  const { accountId } = useParams();
  const [form, setForm] = useState(emptyStandingOrderForm);
  const [localError, setLocalError] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);
  const query = useStandingOrders(accountId);
  const createMutation = useCreateStandingOrder();
  const cancelMutation = useCancelStandingOrder();

  async function handleSubmit(event) {
    event.preventDefault();
    setLocalError(null);
    setActionMessage(null);

    const validationError = validateStandingOrderForm(form);
    if (validationError) {
      setLocalError(validationError);
      return;
    }

    try {
      const result = await createMutation.mutateAsync({ accountId, ...form });
      setActionMessage(result.message || 'Standing order created.');
      setForm(emptyStandingOrderForm);
      await query.refetch();
    } catch (error) {
      setLocalError(mapAxiosError(error));
    }
  }

  async function handleCancel(standingOrderId) {
    setLocalError(null);
    setActionMessage(null);

    if (!window.confirm('Cancel this standing order? The backend may reject cancellation inside the 24-hour processing lock window.')) {
      return;
    }

    try {
      const result = await cancelMutation.mutateAsync(standingOrderId);
      setActionMessage(result.message || 'Standing order cancelled.');
      await query.refetch();
    } catch (error) {
      setLocalError(mapAxiosError(error));
    }
  }

  const queryError = query.error ? mapAxiosError(query.error) : null;
  const standingOrders = query.data?.standingOrders || query.data || [];

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">POST + GET /accounts/{'{accountId}'}/standing-orders</p>
          <h2>Standing Orders</h2>
          <p className="muted">Create a recurring payment instruction above the current list of orders for this account.</p>
        </div>
        <div className="banner info">This page is scaffolded against the future Group 3 backend contract. Until that merge lands, live requests from this screen may fail.</div>
        {actionMessage ? <div className="banner success">{actionMessage}</div> : null}
        {localError ? <div className="banner error">{localError.message || localError}</div> : null}
        {queryError ? <div className="banner error">{queryError.message}</div> : null}
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="standing-order-payee-account">Payee Account</label>
            <input
              id="standing-order-payee-account"
              value={form.payeeAccount}
              onChange={(event) => setForm((current) => ({ ...current, payeeAccount: event.target.value }))}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="standing-order-payee-name">Payee Name</label>
            <input
              id="standing-order-payee-name"
              value={form.payeeName}
              onChange={(event) => setForm((current) => ({ ...current, payeeName: event.target.value }))}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="standing-order-amount">Amount</label>
            <input
              id="standing-order-amount"
              value={form.amount}
              onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="standing-order-frequency">Frequency</label>
            <select
              id="standing-order-frequency"
              value={form.frequency}
              onChange={(event) => setForm((current) => ({ ...current, frequency: event.target.value }))}
            >
              {STANDING_ORDER_FREQUENCIES.map((frequency) => <option key={frequency} value={frequency}>{frequency}</option>)}
            </select>
          </div>
          <div className="field">
            <label htmlFor="standing-order-start-date">Start Date</label>
            <input
              id="standing-order-start-date"
              type="datetime-local"
              value={form.startDate}
              onChange={(event) => setForm((current) => ({ ...current, startDate: event.target.value }))}
              required
            />
            <p className="field-hint">Must be at least 24 hours in the future.</p>
          </div>
          <div className="field">
            <label htmlFor="standing-order-end-date">End Date</label>
            <input
              id="standing-order-end-date"
              type="datetime-local"
              value={form.endDate}
              onChange={(event) => setForm((current) => ({ ...current, endDate: event.target.value }))}
            />
            <p className="field-hint">Optional. When provided, must be after the start date.</p>
          </div>
          <div className="field full">
            <label htmlFor="standing-order-reference">Reference</label>
            <input
              id="standing-order-reference"
              value={form.reference}
              onChange={(event) => setForm((current) => ({ ...current, reference: event.target.value }))}
              required
            />
            <p className="field-hint">1 to 18 alphanumeric characters (letters and numbers only).</p>
          </div>
          <div className="actions">
            <button type="submit" disabled={createMutation.isPending}>Create Standing Order</button>
            <Link className="button-link subtle" to={`/accounts/${accountId}`}>Back to Account</Link>
          </div>
        </form>
      </section>

      <section className="panel stack">
        <div className="section-header">
          <div>
            <h3>Existing Orders</h3>
            <p className="muted">Orders remain visible even after they move into locked, cancelled, or terminated states.</p>
          </div>
          <div className="detail-item">
            <p className="muted">Order Count</p>
            <strong>{query.data?.standingOrderCount ?? standingOrders.length}</strong>
          </div>
        </div>
        {query.isLoading ? <div className="banner success">Loading standing orders...</div> : null}
        {standingOrders.length > 0 ? (
          <div className="table-shell">
            <table>
              <thead>
                <tr>
                  <th>Payee</th>
                  <th>Amount</th>
                  <th>Frequency</th>
                  <th>Status</th>
                  <th>Next Run</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {standingOrders.map((order) => {
                  const disableCancel = ['CANCELLED', 'LOCKED', 'TERMINATED'].includes(order.status);

                  return (
                    <tr key={order.standingOrderId}>
                      <td>
                        <strong>{order.payeeName}</strong>
                        <p className="muted compact-text">{order.payeeAccount || order.reference || 'No account reference returned'}</p>
                      </td>
                      <td>{order.amount}</td>
                      <td>{order.frequency}</td>
                      <td><span className={`status-pill ${String(order.status || '').toLowerCase() || 'neutral'}`}>{order.status}</span></td>
                      <td>{order.nextRunDate ? new Date(order.nextRunDate).toLocaleString() : 'Not scheduled'}</td>
                      <td>
                        <button type="button" className="secondary" onClick={() => handleCancel(order.standingOrderId)} disabled={disableCancel || cancelMutation.isPending}>
                          Cancel
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="panel empty-state stack tight-gap">
            <h3>No standing orders returned</h3>
            <p className="muted">This can be a valid empty state, or a sign that the future backend slice is not merged yet.</p>
          </div>
        )}
      </section>
    </div>
  );
}