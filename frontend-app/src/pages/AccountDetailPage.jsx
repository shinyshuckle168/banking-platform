import { useEffect, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { deleteAccount, updateAccount } from '../api/accounts';
import { mapAxiosError } from '../api/axiosClient';
import { useGetAccount } from '../hooks/useGetAccount';
import { useAuth } from '../auth/AuthContext';
import { emptyAccountUpdateForm } from '../types';

export function AccountDetailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { accountId } = useParams();
  const { isAdmin } = useAuth();
  const [error, setError] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);
  const [updateForm, setUpdateForm] = useState(emptyAccountUpdateForm);
  const query = useGetAccount(accountId);
  const updateAccountMutation = useMutation({ mutationFn: updateAccount });
  const deleteAccountMutation = useMutation({ mutationFn: deleteAccount });

  useEffect(() => {
    if (query.data?.interestRate != null) {
      setUpdateForm({ interestRate: String(query.data.interestRate) });
    } else {
      setUpdateForm(emptyAccountUpdateForm);
    }
  }, [query.data]);

  async function handleUpdate() {
    if (!query.data) {
      return;
    }

    setError(null);
    setActionMessage(null);
    try {
      const result = await updateAccountMutation.mutateAsync({
        accountId: query.data.accountId,
        interestRate: updateForm.interestRate
      });
      setActionMessage('Account updated successfully.');
      setUpdateForm({
        interestRate: result.interestRate ?? ''
      });
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
      if (!window.confirm('Delete this account? This action is restricted to zero-balance accounts and cannot be undone from the UI.')) {
        return;
      }

      const result = await deleteAccountMutation.mutateAsync(query.data.accountId);
      setActionMessage(result.message);
      navigate(query.data.customerId ? `/customer/${query.data.customerId}/accounts` : '/');
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
          <p className="muted">Inspect account details and account-type-specific actions.</p>
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
              <p className="muted">Status</p>
              <strong>{account.status}</strong>
            </article>
            <article className="metric">
              <p className="muted">Balance</p>
              <strong>{account.balance}</strong>
            </article>
          </div>
          <div className="actions">
            <Link className="button-link subtle" to={`/customer/${account.customerId}/accounts`}>Back to Account List</Link>
            <Link className="button-link" to={`/accounts/${account.accountId}/deposit`}>Deposit</Link>
            <Link className="button-link" to={`/accounts/${account.accountId}/withdraw`}>Withdraw</Link>
            <Link className="button-link" to={`/accounts/transfer?fromAccountId=${account.accountId}`}>Transfer</Link>
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
            {showInterestRate ? <button type="button" onClick={handleUpdate} disabled={updateAccountMutation.isPending}>Update Account</button> : null}
            {showInterestRate ? <span className="inline-note">Savings accounts currently expose `interestRate` as the only mutable field in the running backend.</span> : null}
            {!showInterestRate ? <p className="muted compact-text">Checking accounts have no mutable fields in the current backend implementation.</p> : null}
            {isAdmin ? <button type="button" className="secondary danger" onClick={handleDelete} disabled={deleteAccountMutation.isPending}>Delete Account</button> : null}
          </div>
          {location.pathname.endsWith('/edit') ? <div className="banner success">You are viewing the edit route for this account.</div> : null}
          <pre className="code">{JSON.stringify(account, null, 2)}</pre>
        </section>
      ) : null}
    </div>
  );
}
