import { useRecategorise } from '../../hooks/useSpendingInsights';

const CATEGORIES = ['Housing', 'Transport', 'Food & Drink', 'Entertainment', 'Shopping', 'Utilities', 'Health', 'Income'];

/**
 * Renders a transaction row with category dropdown for recategorisation. (T107)
 */
const TransactionCategoryRow = ({ transaction, accountId, year, month }) => {
  const { transactionId, description, amount, category } = transaction;
  const recategorise = useRecategorise(accountId, year, month);

  const fmtAmount = (v) =>
    typeof v === 'number'
      ? v.toLocaleString('en-CA', { style: 'currency', currency: 'CAD' })
      : v;

  const handleChange = (e) => {
    const newCategory = e.target.value;
    if (newCategory && newCategory !== category) {
      recategorise.mutate({ transactionId, category: newCategory });
    }
  };

  return (
    <tr className="transaction-category-row">
      <td>{description}</td>
      <td style={{ fontWeight: 600, color: 'var(--danger-fg)' }}>{fmtAmount(amount)}</td>
      <td>
        <select
          value={category || ''}
          onChange={handleChange}
          disabled={recategorise.isPending}
          aria-label={`Category for ${description}`}
        >
          <option value="">Uncategorised</option>
          {CATEGORIES.map((cat) => (
            <option key={cat} value={cat}>{cat}</option>
          ))}
        </select>
        {recategorise.isPending && <span className="pending-indicator">Saving...</span>}
      </td>
    </tr>
  );
};

export default TransactionCategoryRow;
