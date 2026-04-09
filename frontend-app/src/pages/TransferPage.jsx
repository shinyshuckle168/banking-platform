import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { useTransfer } from '../hooks/useTransfer';
import { createIdempotencyKey, emptyTransferForm } from '../types';

export function TransferPage() {
  const [searchParams] = useSearchParams();
  const initialFromAccountId = searchParams.get('fromAccountId') || '';
  const [form, setForm] = useState({ ...emptyTransferForm, fromAccountId: initialFromAccountId });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const transfer = useTransfer();

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    if (form.fromAccountId && form.toAccountId && form.fromAccountId === form.toAccountId) {
      setResult(null);
      setError({ message: 'Source and destination account IDs must be different.' });
      return;
    }

    try {
      const response = await transfer.mutateAsync(form);
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
          <p className="eyebrow">POST /accounts/transfer</p>
          <h2>Transfer Funds</h2>
          <p className="muted">Move funds atomically between accounts and retain the original outcome behind the same idempotency key.</p>
        </div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="transfer-from-account-id">From Account ID</label>
            <input
              id="transfer-from-account-id"
              value={form.fromAccountId}
              onChange={(event) => setForm((current) => ({ ...current, fromAccountId: event.target.value }))}
            />
          </div>
          <div className="field">
            <label htmlFor="transfer-to-account-id">To Account ID</label>
            <input
              id="transfer-to-account-id"
              value={form.toAccountId}
              onChange={(event) => setForm((current) => ({ ...current, toAccountId: event.target.value }))}
            />
          </div>
          <div className="field">
            <label htmlFor="transfer-amount">Amount</label>
            <input
              id="transfer-amount"
              value={form.amount}
              onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
            />
          </div>
          <div className="field full">
            <label htmlFor="transfer-description">Description</label>
            <input
              id="transfer-description"
              value={form.description}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
            />
          </div>
          <div className="field full">
            <label htmlFor="transfer-idempotency-key">Idempotency Key</label>
            <input
              id="transfer-idempotency-key"
              value={form.idempotencyKey}
              onChange={(event) => setForm((current) => ({ ...current, idempotencyKey: event.target.value }))}
            />
          </div>
          <div className="actions">
            <button type="submit" disabled={transfer.isPending}>Submit Transfer</button>
            <button
              type="button"
              className="secondary"
              onClick={() => setForm((current) => ({ ...current, idempotencyKey: createIdempotencyKey() }))}
            >
              New Key
            </button>
            {initialFromAccountId ? <Link className="button-link subtle" to={`/accounts/${initialFromAccountId}`}>Back to Source Account</Link> : null}
          </div>
        </form>
        {error ? <div className="banner error">{error.message}</div> : null}
      </section>
      {result ? (
        <section className="panel stack">
          <h3>{result.message}</h3>
          <div className="result-grid">
            <div className="result-block">
              <p className="muted">Source Account</p>
              <pre className="code">{JSON.stringify(result.fromAccount, null, 2)}</pre>
            </div>
            <div className="result-block">
              <p className="muted">Destination Account</p>
              <pre className="code">{JSON.stringify(result.toAccount, null, 2)}</pre>
            </div>
            <div className="result-block">
              <p className="muted">Debit Transaction</p>
              <pre className="code">{JSON.stringify(result.debitTransaction, null, 2)}</pre>
            </div>
            <div className="result-block">
              <p className="muted">Credit Transaction</p>
              <pre className="code">{JSON.stringify(result.creditTransaction, null, 2)}</pre>
            </div>
          </div>
        </section>
      ) : null}
    </div>
  );
}
