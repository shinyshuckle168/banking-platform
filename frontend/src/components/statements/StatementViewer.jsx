/**
 * Renders a monthly statement. (T088)
 * Shows correctionSummary section only when versionNumber > 1 and correctionSummary is non-null.
 */
const fmt = (val) => val
  ? new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(val)
  : '—';

const fmtDate = (val) => val
  ? new Intl.DateTimeFormat('en-GB', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(val))
  : '—';

const StatementViewer = ({ statement }) => {
  const {
    accountId,
    period,
    openingBalance,
    closingBalance,
    totalMoneyIn,
    totalMoneyOut,
    transactions = [],
    versionNumber,
    correctionSummary,
    generatedAt,
  } = statement;

  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 8, marginBottom: 16 }}>
          <div>
            <div className="card-title" style={{ marginBottom: 2 }}>Statement — {period}</div>
            <div style={{ fontSize: 12, color: 'var(--neutral-400)' }}>Account {accountId} · Version {versionNumber} · Generated {fmtDate(generatedAt)}</div>
          </div>
          {versionNumber > 1 && correctionSummary && (
            <div style={{ background: 'var(--warning-bg)', color: 'var(--warning-fg)', border: '1px solid #fde68a', borderRadius: 6, padding: '6px 12px', fontSize: 13 }}>
              ⚠️ Correction: {correctionSummary}
            </div>
          )}
        </div>

        <div className="statement-meta">
          <div className="stat-box">
            <div className="stat-box-label">Opening Balance</div>
            <div className="stat-box-value">{fmt(openingBalance)}</div>
          </div>
          <div className="stat-box">
            <div className="stat-box-label">Closing Balance</div>
            <div className="stat-box-value">{fmt(closingBalance)}</div>
          </div>
          <div className="stat-box">
            <div className="stat-box-label">Money In</div>
            <div className="stat-box-value" style={{ color: 'var(--success-fg)' }}>{fmt(totalMoneyIn)}</div>
          </div>
          <div className="stat-box">
            <div className="stat-box-label">Money Out</div>
            <div className="stat-box-value" style={{ color: 'var(--danger-fg)' }}>{fmt(totalMoneyOut)}</div>
          </div>
        </div>
      </div>

      {transactions.length > 0 && (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <div style={{ padding: '16px 20px 0' }}><p className="card-title">Transactions</p></div>
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
                <tr key={tx.transactionId}>
                  <td className="txn-id">{tx.transactionId}</td>
                  <td className="txn-description">{tx.description}</td>
                  <td><span className="type-badge">{tx.type}</span></td>
                  <td className="txn-amount">{fmt(tx.amount)}</td>
                  <td><span className={`badge ${{ SUCCESS: 'badge-green', PENDING: 'badge-amber', FAILED: 'badge-red' }[tx.status] || 'badge-grey'}`}>{tx.status}</span></td>
                  <td className="txn-date">{fmtDate(tx.timestamp)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default StatementViewer;
