import { useState } from 'react';
import { mapAxiosError } from '../api/axiosClient';
import { useListCustomerAccounts } from '../hooks/useListCustomerAccounts';

export function AccountListPage() {
  const [customerIdInput, setCustomerIdInput] = useState('100');
  const [customerId, setCustomerId] = useState('100');
  const query = useListCustomerAccounts(customerId);

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /customers/{'{customerId}'}/accounts</p>
          <h2>Customer Accounts</h2>
        </div>
        <div className="actions">
          <div className="field" style={{ flex: 1 }}>
            <label htmlFor="customerLookup">Customer ID</label>
            <input
              id="customerLookup"
              value={customerIdInput}
              onChange={(event) => setCustomerIdInput(event.target.value)}
            />
          </div>
          <button type="button" onClick={() => setCustomerId(customerIdInput)}>Load Accounts</button>
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
                  <td>{account.accountId}</td>
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
          </div>
        )}
      </section>
    </div>
  );
}
