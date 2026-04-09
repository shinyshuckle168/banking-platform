import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { createAccount } from '../api/accounts';
import { mapAxiosError } from '../api/axiosClient';
import { ACCOUNT_TYPES, emptyCreateAccountForm } from '../types';

export function CreateAccountPage() {
  const navigate = useNavigate();
  const { customerId } = useParams();
  const [formState, setFormState] = useState(emptyCreateAccountForm);
  const [error, setError] = useState(null);
  const createAccountMutation = useMutation({ mutationFn: createAccount });

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    try {
      const createdAccount = await createAccountMutation.mutateAsync({
        ...formState,
        customerId,
        balance: formState.balance,
        interestRate: formState.interestRate
      });
      navigate(`/accounts/${createdAccount.accountId}`);
    } catch (mutationError) {
      setError(mapAxiosError(mutationError));
    }
  }

  const showInterestRate = formState.accountType === 'SAVINGS';

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <p className="eyebrow">POST /customers/{'{customerId}'}/accounts</p>
          <h2>Create Account</h2>
          <p className="muted">Create a new account for customer {customerId}.</p>
        </div>
        {error ? <div className="banner error">{error.message}</div> : null}
        <form className="stack" onSubmit={handleSubmit}>
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
              }}
            >
              Reset
            </button>
            <Link className="button-link subtle" to={`/customer/${customerId}/accounts`}>Back to Accounts</Link>
          </div>
        </form>
      </section>
    </div>
  );
}
