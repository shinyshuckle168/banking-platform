import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { transferBetweenAccounts } from '../api/accounts';
import { mapAxiosError } from '../api/axiosClient';
import { useRecategoriseTransaction } from '../hooks/useGroup3';
import { TRANSACTION_CATEGORIES, createIdempotencyKey } from '../types';

const emptyTransferForm = {
  fromAccountId: '',
  toAccountId: '',
  amount: '25.00',
  description: '',
  category: '',
  idempotencyKey: createIdempotencyKey()
};

function validateTransferForm(form) {
  const fromAccountId = Number.parseInt(String(form.fromAccountId).trim(), 10);
  const toAccountId = Number.parseInt(String(form.toAccountId).trim(), 10);
  const amount = Number.parseFloat(String(form.amount).trim());

  if (!Number.isInteger(fromAccountId) || fromAccountId <= 0) {
    return 'From Account ID must be a positive whole number.';
  }

  if (!Number.isInteger(toAccountId) || toAccountId <= 0) {
    return 'To Account ID must be a positive whole number.';
  }

  if (fromAccountId === toAccountId) {
    return 'Source and destination accounts must be different.';
  }

  if (!Number.isFinite(amount) || amount <= 0) {
    return 'Amount must be greater than zero.';
  }

  if (!String(form.idempotencyKey || '').trim()) {
    return 'Idempotency Key is required.';
  }

  return null;
}

function mapTransferError(error) {
  const mapped = mapAxiosError(error);
  const message = String(mapped.message || '').toLowerCase();

  if (message.includes('source account not found')) {
    return {
      ...mapped,
      message: 'Source account not found. Use an existing ACTIVE account ID that has not been deleted.'
    };
  }

  if (message.includes('destination account not found')) {
    return {
      ...mapped,
      message: 'Destination account not found. Use an existing ACTIVE account ID that has not been deleted.'
    };
  }

  return mapped;
}

export function TransferPage() {
  const [searchParams] = useSearchParams();
  const prefilledFromAccountId = searchParams.get('fromAccountId') || '';
  const [form, setForm] = useState({ ...emptyTransferForm, fromAccountId: prefilledFromAccountId });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const transferMutation = useMutation({ mutationFn: transferBetweenAccounts });
  const recategoriseTransaction = useRecategoriseTransaction();

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    const validationMessage = validateTransferForm(form);
    if (validationMessage) {
      setError({ message: validationMessage });
      return;
    }

    const payload = {
      ...form,
      fromAccountId: Number.parseInt(String(form.fromAccountId).trim(), 10),
      toAccountId: Number.parseInt(String(form.toAccountId).trim(), 10),
      amount: String(form.amount).trim(),
      idempotencyKey: String(form.idempotencyKey).trim()
    };

    try {
      const response = await transferMutation.mutateAsync(payload);
      const category = String(form.category || '').trim();
      let nextResult = response;

      if (category && response?.debitTransaction?.transactionId) {
        try {
          await recategoriseTransaction.mutateAsync({
            accountId: payload.fromAccountId,
            transactionId: response.debitTransaction.transactionId,
            category
          });
          nextResult = {
            ...response,
            debitTransaction: {
              ...response.debitTransaction,
              category
            }
          };
        } catch (categoryError) {
          setError({
            message: `Transfer completed, but the selected category could not be saved. ${mapAxiosError(categoryError).message}`
          });
        }
      }

      setResult(nextResult);
    } catch (requestError) {
      setResult(null);
      setError(mapTransferError(requestError));
    }
  }

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">POST /accounts/transfer</p>
          <h2>Transfer Funds</h2>
          <p className="muted">Move money between accounts using an idempotency key and review both resulting transaction records.</p>
        </div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="transfer-from-account-id">From Account ID</label>
            <input
              id="transfer-from-account-id"
              value={form.fromAccountId}
              onChange={(event) => setForm((current) => ({ ...current, fromAccountId: event.target.value }))}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="transfer-to-account-id">To Account ID</label>
            <input
              id="transfer-to-account-id"
              value={form.toAccountId}
              onChange={(event) => setForm((current) => ({ ...current, toAccountId: event.target.value }))}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="transfer-amount">Amount</label>
            <input
              id="transfer-amount"
              value={form.amount}
              onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
              required
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
          <div className="field">
            <label htmlFor="transfer-category">Category</label>
            <select
              id="transfer-category"
              value={form.category}
              onChange={(event) => setForm((current) => ({ ...current, category: event.target.value }))}
            >
              <option value="">No category</option>
              {TRANSACTION_CATEGORIES.map((category) => <option key={category} value={category}>{category}</option>)}
            </select>
            <p className="field-hint">Optional. Category for this transaction.</p>
          </div>
          <div className="field full">
            <label htmlFor="transfer-idempotency-key">Idempotency Key</label>
            <input
              id="transfer-idempotency-key"
              value={form.idempotencyKey}
              onChange={(event) => setForm((current) => ({ ...current, idempotencyKey: event.target.value }))}
              required
            />
          </div>
          <div className="actions">
            <button type="submit" disabled={transferMutation.isPending}>Submit Transfer</button>
            <button
              type="button"
              className="secondary"
              onClick={() => setForm((current) => ({ ...current, idempotencyKey: createIdempotencyKey() }))}
            >
              New Key
            </button>
            <Link className="button-link subtle" to={form.fromAccountId ? `/accounts/${form.fromAccountId}` : '/'}>Back</Link>
          </div>
        </form>
        {error ? <div className="banner error">{error.message}</div> : null}
      </section>

      {result ? (
        <section className="panel stack">
          <h3>{result.message || 'Transfer completed'}</h3>
          <div className="result-grid">
            <div className="result-block">
              <p className="muted">From Account</p>
              <pre className="code">{JSON.stringify(result.fromAccount, null, 2)}</pre>
            </div>
            <div className="result-block">
              <p className="muted">To Account</p>
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
