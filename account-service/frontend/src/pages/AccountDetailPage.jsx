import { useState } from 'react';
import { mapAxiosError } from '../api/axiosClient';
import { useDeleteAccount } from '../hooks/useDeleteAccount';
import { useGetAccount } from '../hooks/useGetAccount';
import { useUpdateAccount } from '../hooks/useUpdateAccount';

export function AccountDetailPage() {
  const [accountIdInput, setAccountIdInput] = useState('1000');
  const [accountId, setAccountId] = useState('1000');
  const [error, setError] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);
  const [updateForm, setUpdateForm] = useState({ interestRate: '' });
  const query = useGetAccount(accountId);
  const updateAccount = useUpdateAccount();
  const deleteAccount = useDeleteAccount();

  async function refreshAccount() {
    setError(null);
    try {
      await query.refetch();
    } catch (queryError) {
      setError(mapAxiosError(queryError));
    }
  }

  async function handleUpdate() {
    if (!query.data) {
      return;
    }

    setError(null);
    setActionMessage(null);
    try {
      const result = await updateAccount.mutateAsync({
        accountId: query.data.accountId,
        interestRate: updateForm.interestRate
      });
      setActionMessage('Account updated successfully.');
      setUpdateForm({
        interestRate: result.interestRate ?? ''
      });
      setAccountId(String(result.accountId));
      setAccountIdInput(String(result.accountId));
      await query.refetch();
    } catch (mutationError) {
      setError(mapAxiosError(mutationError));
    }
  }

  async function handleDelete() {
    if (!query.data) {
      return;
    }

    setError(null);
    setActionMessage(null);
    try {
      const result = await deleteAccount.mutateAsync(query.data.accountId);
      setActionMessage(result.message);
      await query.refetch();
    } catch (mutationError) {
      setError(mapAxiosError(mutationError));
    }
  }

  const account = query.data;
  const showInterestRate = account?.accountType === 'SAVINGS';

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /accounts/{'{accountId}'}</p>
          <h2>Account Detail</h2>
        </div>
        <div className="actions">
          <div className="field" style={{ flex: 1 }}>
            <label htmlFor="accountId">Account ID</label>
            <input
              id="accountId"
              value={accountIdInput}
              onChange={(event) => setAccountIdInput(event.target.value)}
            />
          </div>
          <button
            type="button"
            onClick={() => {
              setAccountId(accountIdInput);
              setActionMessage(null);
              void refreshAccount();
            }}
          >
            Load Account
          </button>
        </div>
        {query.isLoading ? <div className="banner success">Loading account...</div> : null}
        {actionMessage ? <div className="banner success">{actionMessage}</div> : null}
        {error ? <div className="banner error">{error.message}</div> : null}
        {query.error ? <div className="banner error">{mapAxiosError(query.error).message}</div> : null}
      </section>
      {account ? (
        <section className="panel stack">
          <h3>Account Payload</h3>
          <div className="card-grid">
            <article className="metric">
              <p className="muted">Account ID</p>
              <strong>{account.accountId}</strong>
            </article>
            <article className="metric">
              <p className="muted">Customer ID</p>
              <strong>{account.customerId}</strong>
            </article>
            <article className="metric">
              <p className="muted">Balance</p>
              <strong>{account.balance}</strong>
            </article>
          </div>
          <div className="form-grid">
            {showInterestRate ? (
              <div className="field">
                <label htmlFor="update-interest-rate">Interest Rate</label>
                <input
                  id="update-interest-rate"
                  value={updateForm.interestRate}
                  placeholder={String(account.interestRate ?? '')}
                  onChange={(event) => setUpdateForm((current) => ({ ...current, interestRate: event.target.value }))}
                />
              </div>
            ) : null}
          </div>
          <div className="actions">
            {showInterestRate ? <button type="button" onClick={handleUpdate} disabled={updateAccount.isPending}>Update Account</button> : null}
            <button type="button" className="secondary" onClick={handleDelete} disabled={deleteAccount.isPending}>Delete Account</button>
          </div>
          {!showInterestRate ? <p className="muted">No mutable account-specific fields remain for this account type.</p> : null}
          <pre className="code">{JSON.stringify(account, null, 2)}</pre>
        </section>
      ) : null}
    </div>
  );
}
