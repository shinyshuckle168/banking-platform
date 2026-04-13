import { useCancelStandingOrder } from '../../hooks/useStandingOrders';

const TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000;

const fmt = (val) => val
  ? new Intl.DateTimeFormat('en-GB', { dateStyle: 'medium' }).format(new Date(val))
  : '—';

const fmtAmount = (val) =>
  new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(val);

/**
 * Renders a single standing order with cancel capability. (T061)
 * Cancel button is disabled when nextRunDate is within 24 hours.
 */
const StandingOrderItem = ({ order, accountId }) => {
  const { standingOrderId, payeeAccount, payeeName, amount, frequency, status, nextRunDate, startDate, endDate, reference } = order;
  const cancelMutation = useCancelStandingOrder(accountId);

  const isWithin24h = nextRunDate
    ? new Date(nextRunDate) - Date.now() <= TWENTY_FOUR_HOURS_MS
    : false;

  const statusClass = status === 'ACTIVE' ? 'badge-green' : status === 'CANCELLED' ? 'badge-red' : 'badge-grey';

  return (
    <div className="so-card">
      <div className="so-main">
        <div className="so-payee">{payeeName}</div>
        <div className="so-meta">
          <span>{payeeAccount}</span>
          <span>{frequency}</span>
          <span>Next: {fmt(nextRunDate)}</span>
          <span>From {fmt(startDate)}{endDate ? ` – ${fmt(endDate)}` : ''}</span>
          {reference && <span>Ref: {reference}</span>}
        </div>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexShrink: 0 }}>
        <div className="so-amount">{fmtAmount(amount)}</div>
        <span className={`badge ${statusClass}`}>{status}</span>
        <button
          className="btn btn-danger"
          onClick={() => cancelMutation.mutate(standingOrderId)}
          disabled={isWithin24h || cancelMutation.isPending || status !== 'ACTIVE'}
          title={isWithin24h ? 'Cannot cancel within 24 hours of next run date' : undefined}
        >
          {cancelMutation.isPending ? 'Cancelling…' : 'Cancel'}
        </button>
      </div>
    </div>
  );
};

export default StandingOrderItem;
