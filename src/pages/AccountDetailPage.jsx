import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { deleteAccount } from '../api/accounts';
import { mapAxiosError } from '../api/axiosClient';
import { useGetAccount } from '../hooks/useGetAccount';

export function AccountDetailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { accountId } = useParams();
  const [error, setError] = useState(null);
  const query = useGetAccount(accountId);
  const deleteAccountMutation = useMutation({ mutationFn: deleteAccount });

  async function handleDelete() {
    if (!query.data) {
      return;
    }

    setError(null);
    try {
      if (!window.confirm('Delete this account? This action is restricted to zero-balance accounts and cannot be undone from the UI.')) {
        return;
      }

      const result = await deleteAccountMutation.mutateAsync(query.data.accountId);
      navigate(query.data.customerId ? `/customer/${query.data.customerId}/accounts` : '/', {
        state: {
          deletedAccountMessage: result.message || `Account ${query.data.accountId} has been deleted.`
        }
      });
    } catch (mutationError) {
      setError(mapAxiosError(mutationError));
    }
  }

  const account = query.data;
  const canDeleteAccount = Number(account?.balance) === 0;
  const queryError = query.error ? mapDeletedAccountError(query.error) : null;

  return (
    <div className="stack">
      {query.isLoading ? <div className="banner success">Loading account...</div> : null}
      {error ? <div className="banner error">{error.message}</div> : null}
      {queryError ? <div className="banner error">{queryError.message}</div> : null}
      {account ? (
        <section className="panel stack">
          <div className="section-header">
            <div>
              <h2 style={{ margin: 0, fontSize: '1.75rem' }}>Account Overview</h2>
              <p className="muted" style={{ margin: '0.25rem 0 0 0' }}>View your account balance and access account features.</p>
            </div>
            <div className="actions">
              <Link className="button-link subtle" to={`/customer/${account.customerId}/accounts`}>Back to Account List</Link>
              <button type="button" className="secondary danger" onClick={handleDelete} disabled={deleteAccountMutation.isPending || !canDeleteAccount}>Delete Account</button>
            </div>
          </div>
          <div style={{ display: 'flex', gap: '3rem', padding: '2.25rem 3rem', background: 'var(--color-surface, #f8f9fa)', borderRadius: '12px', border: '1px solid var(--color-border, #e2e6ea)', margin: '0.5rem 0 1.5rem 0', width: '100%', boxSizing: 'border-box', boxShadow: '0 2px 16px 0 rgba(0,0,0,0.04)' }}>
            <div style={{ flex: 1, textAlign: 'center' }}>
              <p className="muted" style={{ margin: '0 0 0.4rem 0', fontSize: '1rem', textAlign: 'center' }}>Account Type</p>
              <p style={{ margin: 0, fontSize: '2.5rem', fontWeight: 700, lineHeight: 1, textAlign: 'center' }}>{account.accountType}</p>
            </div>
            <div style={{ flex: 1, borderLeft: '1px solid var(--color-border, #e2e6ea)', paddingLeft: '3rem', textAlign: 'center' }}>
              <p className="muted" style={{ margin: '0 0 0.4rem 0', fontSize: '1rem', textAlign: 'center' }}>Balance</p>
              <p style={{ margin: 0, fontSize: '2.5rem', fontWeight: 700, lineHeight: 1, textAlign: 'center' }}>{account.balance}</p>
            </div>
          </div>
          <div className="section-divider" />
          <div className="actions">
            <Link className="button-link subtle" to={`/accounts/transfer?fromAccountId=${account.accountId}`}>Transfer Funds</Link>
            <Link className="button-link subtle" to={`/accounts/${account.accountId}/transactions`}>Transaction History</Link>
            <Link className="button-link subtle" to={`/accounts/${account.accountId}/standing-orders`}>Standing Orders</Link>
            <Link className="button-link subtle" to={`/accounts/${account.accountId}/statements`}>Monthly Statement</Link>
            <Link className="button-link subtle" to={`/accounts/${account.accountId}/insights`}>Spending Insights</Link>
          </div>
          {!canDeleteAccount ? <p className="muted compact-text">Balance must be exactly zero to delete this account.</p> : null}
          {location.pathname.endsWith('/edit') ? <div className="banner success">You are viewing the edit route for this account.</div> : null}
        </section>
      ) : null}
    </div>
  );
}

function mapDeletedAccountError(error) {
  const mapped = mapAxiosError(error);

  if (mapped.code === 'ACCOUNT_NOT_FOUND' || mapped.message === 'Account not found') {
    return {
      ...mapped,
      message: 'This account may have been deleted or is no longer accessible.'
    };
  }

  return mapped;
}
