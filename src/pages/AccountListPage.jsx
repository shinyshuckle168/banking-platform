import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { createAccount } from '../api/accounts';
import { getCustomer, listCustomers } from '../api/customers';
import { mapAxiosError } from '../api/axiosClient';
import { useAuth } from '../auth/AuthContext';
import { useListCustomerAccounts } from '../hooks/useListCustomerAccounts';
import { ACCOUNT_TYPES, emptyCreateAccountForm } from '../types';

export function AccountListPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { customerId } = useParams();
  const { isAdmin, rememberCustomerId } = useAuth();
  const [formState, setFormState] = useState(emptyCreateAccountForm);
  const [error, setError] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);
  const query = useListCustomerAccounts(customerId);
  const customerQuery = useQuery({
    queryKey: ['customer', customerId],
    queryFn: () => getCustomer(customerId),
    enabled: Boolean(customerId)
  });
  const customerListQuery = useQuery({
    queryKey: ['customers'],
    queryFn: listCustomers,
    enabled: isAdmin
  });
  const createAccountMutation = useMutation({ mutationFn: createAccount });
  const deletedAccountMessage = location.state?.deletedAccountMessage || null;
  const accountsError = query.error ? mapDeletedCustomerAccountsError(query.error) : null;
  const customerError = customerQuery.error ? mapDeletedCustomerAccountsError(customerQuery.error) : null;

  async function handleCreateAccount(event) {
    event.preventDefault();
    setError(null);
    setActionMessage(null);

    try {
      const createdAccount = await createAccountMutation.mutateAsync({
        ...formState,
        customerId,
        balance: formState.balance,
        interestRate: formState.interestRate
      });
      setActionMessage(`Account ${createdAccount.accountId} created successfully.`);
      setFormState(emptyCreateAccountForm);
      await Promise.all([query.refetch(), customerQuery.refetch()]);
    } catch (mutationError) {
      setError(mapAxiosError(mutationError));
    }
  }

  function handleCustomerSwitch(event) {
    const nextCustomerId = event.target.value;
    if (!nextCustomerId) {
      return;
    }

    setError(null);
    setActionMessage(null);
    rememberCustomerId(nextCustomerId);
    navigate(`/customer/${nextCustomerId}/accounts`);
  }

  const showInterestRate = formState.accountType === 'SAVINGS';

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">GET /customers/{'{customerId}'}/accounts</p>
          <h2>Customer Accounts</h2>
          <p className="muted">List active accounts and create a new account for customer {customerId} from the same page.</p>
        </div>
        {isAdmin ? (
          <div className="field">
            <label htmlFor="accounts-customer-switcher">Admin Customer Switcher</label>
            <select id="accounts-customer-switcher" value={customerId || ''} onChange={handleCustomerSwitch}>
              <option value="">Select customer</option>
              {(customerListQuery.data || []).map((customerOption) => (
                <option key={customerOption.customerId} value={customerOption.customerId}>
                  {customerOption.customerId} - {customerOption.name}
                </option>
              ))}
            </select>
            {customerListQuery.error ? <p className="field-hint">{mapAxiosError(customerListQuery.error).message}</p> : null}
          </div>
        ) : null}
        <div className="actions">
          <Link className="button-link subtle" to={`/customer/${customerId}`}>Back to Customer</Link>
        </div>
        {deletedAccountMessage ? <div className="banner success">{deletedAccountMessage}</div> : null}
        {actionMessage ? <div className="banner success">{actionMessage}</div> : null}
        {error ? <div className="banner error">{error.message}</div> : null}
        {query.isLoading ? <div className="banner success">Loading accounts...</div> : null}
        {accountsError ? <div className="banner error">{accountsError.message}</div> : null}
        {customerError ? <div className="banner error">{customerError.message}</div> : null}
      </section>
      {!accountsError && !customerError ? (
      <section className="panel stack">
        <div>
          <p className="eyebrow">POST /customers/{'{customerId}'}/accounts</p>
          <h3>Create Account</h3>
          <p className="muted">Open a checking or savings account for {customerQuery.data?.name || `customer ${customerId}`}.</p>
        </div>
        <form className="stack" onSubmit={handleCreateAccount}>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="accountType">Account Type</label>
              <select
                id="accountType"
                value={formState.accountType}
                onChange={(event) => setFormState((current) => ({ ...current, accountType: event.target.value }))}
              >
                {ACCOUNT_TYPES.map((type) => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="balance">Opening Balance</label>
              <input
                id="balance"
                value={formState.balance}
                onChange={(event) => setFormState((current) => ({ ...current, balance: event.target.value }))}
              />
            </div>
            {showInterestRate ? (
              <div className="field">
                <label htmlFor="interestRate">Interest Rate</label>
                <input
                  id="interestRate"
                  value={formState.interestRate}
                  onChange={(event) => setFormState((current) => ({ ...current, interestRate: event.target.value }))}
                />
              </div>
            ) : null}
          </div>
          <div className="actions">
            <button type="submit" disabled={createAccountMutation.isPending}>Create Account</button>
            <button
              type="button"
              className="secondary"
              onClick={() => {
                setFormState(emptyCreateAccountForm);
                setError(null);
                setActionMessage(null);
              }}
            >
              Reset
            </button>
          </div>
        </form>
      </section>
      ) : null}
      <section className="table-shell">
        {accountsError || customerError ? (
          <div className="panel">
            <h3>Accounts unavailable</h3>
            <p className="muted">The selected customer's accounts cannot be displayed right now.</p>
          </div>
        ) : query.data && query.data.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>Account</th>
                <th>Type</th>
                <th>Status</th>
                <th>Balance</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {query.data.map((account) => (
                <tr key={account.accountId}>
                  <td><Link className="table-link" to={`/accounts/${account.accountId}`}>{account.accountId}</Link></td>
                  <td>{account.accountType}</td>
                  <td>{account.status}</td>
                  <td>{account.balance}</td>
                  <td className="actions-cell">
                    <div className="action-buttons">
                      <Link className="button-mini" to={`/accounts/${account.accountId}/deposit`} title="Deposit Funds">Deposit</Link>
                      <Link className="button-mini" to={`/accounts/${account.accountId}/withdraw`} title="Withdraw Funds">Withdraw</Link>
                      <Link className="button-mini" to={`/accounts/transfer?fromAccountId=${account.accountId}`} title="Transfer Funds">Transfer Funds</Link>
                      <Link className="button-mini" to={`/accounts/${account.accountId}/transactions`} title="View Transaction History">Transaction History</Link>
                      <Link className="button-mini" to={`/accounts/${account.accountId}/standing-orders`} title="Manage Standing Orders">Standing Orders</Link>
                      <Link className="button-mini" to={`/accounts/${account.accountId}/statements`} title="View Monthly Statement">Monthly Statement</Link>
                      <Link className="button-mini" to={`/accounts/${account.accountId}/insights`} title="View Spending Insights">Spending Insights</Link>
                    </div>
                  </td>
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
            <button type="button" onClick={() => document.getElementById('accountType')?.focus()}>Create First Account</button>
          </div>
        )}
      </section>
    </div>
  );
}

function mapDeletedCustomerAccountsError(error) {
  const mapped = mapAxiosError(error);

  if (mapped.code === 'CUSTOMER_NOT_FOUND' || mapped.message === 'Customer not found') {
    return {
      ...mapped,
      message: 'This customer may have been deleted or is no longer accessible, so their accounts cannot be shown.'
    };
  }

  return mapped;
}
