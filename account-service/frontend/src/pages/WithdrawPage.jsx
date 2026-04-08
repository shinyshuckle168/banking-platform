import { useState } from 'react';
import { mapAxiosError } from '../api/axiosClient';
import { useWithdraw } from '../hooks/useWithdraw';
import { createIdempotencyKey, emptyMoneyMovementForm } from '../types';

export function WithdrawPage() {
  const [form, setForm] = useState({ ...emptyMoneyMovementForm, amount: '10.00' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const withdraw = useWithdraw();

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    try {
      const response = await withdraw.mutateAsync(form);
      setResult(response);
    } catch (requestError) {
      setResult(null);
      setError(mapAxiosError(requestError));
    }
  }

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">POST /accounts/{'{accountId}'}/withdraw</p>
          <h2>Withdraw</h2>
          <p className="muted">Debit funds from an active account while preserving the original result for duplicate retries.</p>
        </div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="withdraw-account-id">Account ID</label>
            <input
              id="withdraw-account-id"
              value={form.accountId}
              onChange={(event) => setForm((current) => ({ ...current, accountId: event.target.value }))}
            />
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
              <p className="muted">Recorded Transaction</p>
              <pre className="code">{JSON.stringify(result.transaction, null, 2)}</pre>
            </div>
          </div>
        </section>
      ) : null}
    </div>
  );
}
