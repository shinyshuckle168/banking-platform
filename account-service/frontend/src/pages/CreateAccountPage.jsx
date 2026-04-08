import { useState } from 'react';
import { mapAxiosError } from '../api/axiosClient';
import { useCreateAccount } from '../hooks/useCreateAccount';
import { ACCOUNT_TYPES, emptyCreateAccountForm } from '../types';

export function CreateAccountPage() {
  const [formState, setFormState] = useState(emptyCreateAccountForm);
  const [error, setError] = useState(null);
  const createAccount = useCreateAccount();

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    try {
      await createAccount.mutateAsync({
        ...formState,
        balance: formState.balance,
        interestRate: formState.interestRate
      });
    } catch (mutationError) {
      setError(mapAxiosError(mutationError));
    }
  }

  const createdAccount = createAccount.data;
  const showInterestRate = formState.accountType === 'SAVINGS';

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">POST /customers/{'{customerId}'}/accounts</p>
          <h2>Create Account</h2>
        </div>
        {error ? <div className="banner error">{error.message}</div> : null}
        {createdAccount ? (
          <div className="banner success">
            Account {createdAccount.accountId} created with status {createdAccount.status}.
          </div>
        ) : null}
        <form className="stack" onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="customerId">Customer ID</label>
              <input
                id="customerId"
                value={formState.customerId}
                onChange={(event) => setFormState((current) => ({ ...current, customerId: event.target.value }))}
              />
            </div>
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
            <button type="submit" disabled={createAccount.isPending}>Create Account</button>
            <button
              type="button"
              className="secondary"
              onClick={() => {
                setFormState(emptyCreateAccountForm);
                setError(null);
              }}
            >
              Reset
            </button>
          </div>
        </form>
      </section>
      {createdAccount ? (
        <section className="panel stack">
          <h3>Created Resource</h3>
          <pre className="code">{JSON.stringify(createdAccount, null, 2)}</pre>
        </section>
      ) : null}
    </div>
  );
}
