function formatTimestamp(value) {
  if (!value) {
    return 'Not provided';
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  return parsed.toLocaleString();
}

function getProcessingState(transaction) {
  return transaction.processingState || transaction.postingState || transaction.clearingState || transaction.lifecycleState || null;
}

export function TransactionsTable({ transactions, emptyTitle, emptyMessage }) {
  if (!Array.isArray(transactions) || transactions.length === 0) {
    return (
      <div className="panel empty-state stack tight-gap">
        <h3>{emptyTitle}</h3>
        <p className="muted">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="table-shell">
      <table>
        <thead>
          <tr>
            <th>Timestamp</th>
            <th>Description</th>
            <th>Type</th>
            <th>Outcome</th>
            <th>Progress</th>
            <th>Amount</th>
            <th>Idempotency Key</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => {
            const processingState = getProcessingState(transaction);

            return (
              <tr key={transaction.transactionId}>
                <td>{formatTimestamp(transaction.timestamp)}</td>
                <td>{transaction.description || 'No description'}</td>
                <td>{transaction.type || 'Unknown'}</td>
                <td>
                  <span className={`status-pill ${String(transaction.status || '').toLowerCase() || 'neutral'}`}>
                    {transaction.status || 'Unknown'}
                  </span>
                </td>
                <td>
                  {processingState ? (
                    <span className="status-pill pending">{processingState}</span>
                  ) : (
                    <span className="muted compact-text">Not provided</span>
                  )}
                </td>
                <td>{transaction.amount ?? '0.00'}</td>
                <td>{transaction.idempotencyKey || 'None'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}