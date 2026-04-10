import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { useDeposit } from '../hooks/useDeposit';
import { createIdempotencyKey, emptyMoneyMovementForm } from '../types';

export function DepositPage() {
  const { accountId } = useParams();
  const deposit = useDeposit();
  const [form, setForm] = useState({ ...emptyMoneyMovementForm, accountId: accountId || '' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    try {
      const response = await deposit.mutateAsync({ ...form, accountId: accountId || form.accountId });
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
          <p className="eyebrow">POST /accounts/{'{accountId}'}/deposit</p>
          <h2>Deposit Funds</h2>
          <p className="muted">Submit a deposit with an idempotency key and inspect the updated account plus resulting transaction.</p>
        </div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="deposit-account-id">Account ID</label>
            <input id="deposit-account-id" value={accountId || form.accountId} readOnly />
          </div>
          <div className="field">
            <label htmlFor="deposit-amount">Amount</label>
            <input
              id="deposit-amount"
              value={form.amount}
              onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
            />
          </div>
          <div className="field full">
            <label htmlFor="deposit-description">Description</label>
            <input
              id="deposit-description"
              value={form.description}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
            />
          </div>
          <div className="field full">
            <label htmlFor="deposit-idempotency-key">Idempotency Key</label>
            <input
              id="deposit-idempotency-key"
              value={form.idempotencyKey}
              onChange={(event) => setForm((current) => ({ ...current, idempotencyKey: event.target.value }))}
            />
          </div>
          <div className="actions">
            <button type="submit" disabled={deposit.isPending}>Submit Deposit</button>
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
