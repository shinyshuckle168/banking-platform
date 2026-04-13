/**
 * Renders a single transaction row. (T036)
 */
const TransactionItem = ({ transaction }) => {
  const { transactionId, amount, type, status, timestamp, description, idempotencyKey } = transaction;

  const statusBadge = {
    PENDING: { label: 'Pending', className: 'badge-amber' },
    SUCCESS: { label: 'Success', className: 'badge-green' },
    FAILED:  { label: 'Failed',  className: 'badge-red' },
  }[status] || { label: status, className: 'badge-grey' };

  const formattedAmount = typeof amount === 'number'
    ? new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(Math.abs(amount))
    : amount;

  const formattedDate = timestamp
    ? new Intl.DateTimeFormat('en-GB', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(timestamp))
    : '—';

  const isCredit = type === 'CREDIT';

  return (
    <tr className="transaction-item">
      <td className="txn-id">{transactionId}</td>
      <td className="txn-description">{description || '—'}</td>
      <td><span className="type-badge">{type}</span></td>
      <td className={`txn-amount ${isCredit ? 'credit' : 'debit'}`}>
        {isCredit ? '+' : '-'}{formattedAmount}
      </td>
      <td>
        <span className={`badge ${statusBadge.className}`}>{statusBadge.label}</span>
      </td>
      <td className="txn-date">{formattedDate}</td>
      {idempotencyKey != null && <td className="txn-id">{idempotencyKey}</td>}
    </tr>
  );
};

export default TransactionItem;
