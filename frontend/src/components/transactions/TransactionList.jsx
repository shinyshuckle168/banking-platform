import TransactionItem from './TransactionItem';

/**
 * Renders a list of transactions in the order received (timestamp ascending from API). (T037)
 */
const TransactionList = ({ transactions = [] }) => {
  if (transactions.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">🔍</div>
        <p>No transactions found for the selected period.</p>
      </div>
    );
  }

  return (
    <table className="txn-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Description</th>
          <th>Type</th>
          <th>Amount</th>
          <th>Status</th>
          <th>Timestamp</th>
        </tr>
      </thead>
      <tbody>
        {transactions.map((tx) => (
          <TransactionItem key={tx.transactionId} transaction={tx} />
        ))}
      </tbody>
    </table>
  );
};

export default TransactionList;
