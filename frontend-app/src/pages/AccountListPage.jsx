import { Link, useParams } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { useListCustomerAccounts } from '../hooks/useListCustomerAccounts';

export function AccountListPage() {
  const { customerId } = useParams();
  const query = useListCustomerAccounts(customerId);

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /customers/{'{customerId}'}/accounts</p>
          <h2>Customer Accounts</h2>
          <p className="muted">List active accounts for customer {customerId}.</p>
        </div>
        <div className="actions">
          <Link className="button-link" to={`/customer/${customerId}/accounts/create`}>Create Account</Link>
          <Link className="button-link subtle" to={`/customer/${customerId}`}>Back to Customer</Link>
        </div>
        {query.isLoading ? <div className="banner success">Loading accounts...</div> : null}
        {query.error ? <div className="banner error">{mapAxiosError(query.error).message}</div> : null}
      </section>
      <section className="table-shell">
        {query.data && query.data.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>Account</th>
                <th>Type</th>
                <th>Status</th>
                <th>Balance</th>
              </tr>
            </thead>
            <tbody>
              {query.data.map((account) => (
                <tr key={account.accountId}>
                  <td><Link className="table-link" to={`/accounts/${account.accountId}`}>{account.accountId}</Link></td>
                  <td>{account.accountType}</td>
                  <td>{account.status}</td>
                  <td>{account.balance}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="panel">
            <h3>No active accounts returned</h3>
            <p className="muted">
              If the customer exists, this empty state is still a valid success outcome for the current spec.
            </p>
            <Link className="button-link subtle" to={`/customer/${customerId}/accounts/create`}>Create First Account</Link>
          </div>
        )}
      </section>
    </div>
  );
}
