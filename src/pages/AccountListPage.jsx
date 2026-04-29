import { Fragment, useState } from 'react';
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
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [expandedAccountId, setExpandedAccountId] = useState(null);

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
      setIsCreateModalOpen(false);
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

  function openCreateModal() {
    setError(null);
    setActionMessage(null);
    setIsCreateModalOpen(true);
  }

  function closeCreateModal() {
    if (createAccountMutation.isPending) {
      return;
    }

    setIsCreateModalOpen(false);
    setError(null);
  }

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <h2>My Accounts</h2>
           <p className="muted" style={{ margin: '0.25rem 0 1.25rem 0' }}>View and manage your accounts.</p>
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
          {!accountsError && !customerError ? <button type="button" onClick={openCreateModal}>Create Account</button> : null}
        </div>
        {deletedAccountMessage ? <div className="banner success">{deletedAccountMessage}</div> : null}
        {actionMessage ? <div className="banner success">{actionMessage}</div> : null}
        {error ? <div className="banner error">{error.message}</div> : null}
        {query.isLoading ? <div className="banner success">Loading accounts...</div> : null}
        {accountsError ? <div className="banner error">{accountsError.message}</div> : null}
        {customerError ? <div className="banner error">{customerError.message}</div> : null}
      </section>

      {isCreateModalOpen ? (
        <div className="modal-backdrop" onClick={closeCreateModal}>
          <div className="modal-panel stack" role="dialog" aria-modal="true" aria-labelledby="create-account-modal-title" onClick={(event) => event.stopPropagation()}>
            <div className="section-header">
              <div>
                <h3 id="create-account-modal-title">Create Account</h3>
              </div>
              <button type="button" className="secondary" onClick={closeCreateModal} disabled={createAccountMutation.isPending}>Close</button>
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
              <div className="actions centered-actions">
                <button type="submit" disabled={createAccountMutation.isPending}>Create Account</button>
                <button
                  type="button"
                  className="secondary"
                  onClick={() => {
                    setFormState(emptyCreateAccountForm);
                    setError(null);
                  }}
                  disabled={createAccountMutation.isPending}
                >
                  Reset
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      <section className="table-shell accounts-table-shell">
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
                <th>Balance</th>
              </tr>
            </thead>
            <tbody>
              {query.data.map((account) => {
                const isExpanded = expandedAccountId === account.accountId;

                return (
                  <Fragment key={account.accountId}>
                    <tr>
                      <td><Link className="table-link" to={`/accounts/${account.accountId}`}>{account.accountId}</Link></td>
                      <td>{account.accountType}</td>
                      <td>{account.balance}</td>
                      {/* Actions column removed */}
                    </tr>
                    {/* Expanded actions row removed */}
                  </Fragment>
                );
              })}
            </tbody>
          </table>
        ) : null}
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
