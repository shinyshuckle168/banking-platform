import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { useWithdraw } from '../hooks/useWithdraw';
import { TRANSACTION_CATEGORIES, createIdempotencyKey, emptyMoneyMovementForm } from '../types';

function mapMoneyMovementError(error) {
  const mapped = mapAxiosError(error);
  const message = String(mapped.message || '').toLowerCase();

  if (message.includes('authenticated user not found') || message.includes('authentication required')) {
    return {
      ...mapped,
      message: 'Backend authentication failed for this money movement request. Please log out, log back in, and retry. If the issue persists, this matches the current known backend limitation for deposit/withdraw.'
    };
  }

  return mapped;
}

export function WithdrawPage() {
  const { accountId } = useParams();
  const withdraw = useWithdraw();
  const [form, setForm] = useState({ ...emptyMoneyMovementForm, accountId: accountId || '' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    try {
      const response = await withdraw.mutateAsync({ ...form, accountId: accountId || form.accountId });
      setResult(response);
    } catch (requestError) {
      setResult(null);
      setError(mapMoneyMovementError(requestError));
    }
  }

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">POST /accounts/{'{accountId}'}/withdraw</p>
          <h2>Withdraw Funds</h2>
          <p className="muted">Submit a withdrawal with an idempotency key and inspect the updated account plus resulting transaction.</p>
        </div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="withdraw-account-id">Account ID</label>
            <input id="withdraw-account-id" value={accountId || form.accountId} readOnly />
          </div>
          <div className="field">
            <label htmlFor="withdraw-amount">Amount</label>
            <input
              id="withdraw-amount"
              value={form.amount}
              onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
            />
          </div>
          <div className="field full">
            <label htmlFor="withdraw-description">Description</label>
            <input
              id="withdraw-description"
              value={form.description}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
            />
          </div>
          <div className="field">
            <label htmlFor="withdraw-category">Category</label>
            <select
              id="withdraw-category"
              value={form.category}
              onChange={(event) => setForm((current) => ({ ...current, category: event.target.value }))}
            >
              <option value="">No category</option>
              {TRANSACTION_CATEGORIES.map((category) => <option key={category} value={category}>{category}</option>)}
            </select>
            <p className="field-hint">Optional. Category for this transaction.</p>
          </div>
          <div className="field full">
            <label htmlFor="withdraw-idempotency-key">Idempotency Key</label>
            <input
              id="withdraw-idempotency-key"
              value={form.idempotencyKey}
              onChange={(event) => setForm((current) => ({ ...current, idempotencyKey: event.target.value }))}
            />
          </div>
          <div className="actions">
            <button type="submit" disabled={withdraw.isPending}>Submit Withdrawal</button>
            <button
              type="button"
              className="secondary"
              onClick={() => setForm((current) => ({ ...current, idempotencyKey: createIdempotencyKey() }))}
            >
              New Key
            </button>
            <Link className="button-link subtle" to={`/accounts/${accountId}`}>Back to Account</Link>
          </div>
        </form>
        {error ? <div className="banner error">{error.message}</div> : null}
      </section>
      {result ? (
        <section className="panel stack">
          <h3>{result.message}</h3>
          <div className="result-grid">
            <div className="result-block">
              <p className="muted">Updated Account</p>
              <pre className="code">{JSON.stringify(result.account, null, 2)}</pre>
            </div>
            <div className="result-block">
              <p className="muted">Transaction</p>
              <pre className="code">{JSON.stringify(result.transaction, null, 2)}</pre>
            </div>
          </div>
        </section>
      ) : null}
    </div>
  );
}
