import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { emptyCustomerLookup } from '../types';

export function HomePage() {
  const navigate = useNavigate();
  const { authState, isAuthenticated, isAdmin } = useAuth();
  const [lookup, setLookup] = useState(emptyCustomerLookup);

  function openCustomer() {
    if (lookup.customerId) {
      navigate(`/customer/${lookup.customerId}`);
    }
  }

  function openAccount() {
    if (lookup.accountId) {
      navigate(`/accounts/${lookup.accountId}`);
    }
  }

  return (
    <div className="stack">
      <section className="hero">
        <span className="kicker">Unified banking client</span>
        <h2>Auth, customer, account, and money movement flows in one UI</h2>
        <p className="lede">
          This frontend is structured around login-api for authentication and customer profiles, plus account-service for account management and money movement.
        </p>
        <div className="hero-grid">
          <article className="metric">
            <p className="muted">Customer context</p>
            <strong>{authState.customerId || 'Not linked'}</strong>
          </article>
          <article className="metric">
            <p className="muted">Access level</p>
            <strong>{isAuthenticated ? authState.roles.join(', ') || 'Authenticated' : 'Public'}</strong>
          </article>
          <article className="metric">
            <p className="muted">Backend targets</p>
            <strong>2 services</strong>
          </article>
        </div>
      </section>
      <section className="card-grid">
        <article className="panel">
          <h3>Authentication</h3>
          <p className="muted">Register, sign in, and maintain a customer-linked session using JWT Bearer tokens.</p>
        </article>
        <article className="panel">
          <h3>Customer Profile</h3>
          <p className="muted">Create, retrieve, and update customer data through login-api.</p>
        </article>
        <article className="panel">
          <h3>Accounts</h3>
          <p className="muted">Create and inspect checking or savings accounts, then update eligible fields.</p>
        </article>
        <article className="panel">
          <h3>Money Movement</h3>
          <p className="muted">Submit deposits, withdrawals, and transfers with idempotency support.</p>
        </article>
      </section>

      {!isAuthenticated ? (
        <section className="panel stack">
          <h3>Get Started</h3>
          <p className="muted">Create an account or sign in to access the authenticated banking routes.</p>
          <div className="actions">
            <Link className="button-link" to="/login">Open Login</Link>
            <Link className="button-link subtle" to="/register">Open Registration</Link>
            <Link className="button-link subtle" to="/password-reset">Password Reset</Link>
          </div>
        </section>
      ) : (
        <section className="panel stack">
          <h3>Quick Navigation</h3>
          <p className="muted">
            Current backends do not expose a dedicated "get my customer profile" endpoint, so this app stores the most recent customer context after create or fetch.
          </p>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="home-customer-id">Open Customer ID</label>
              <input
                id="home-customer-id"
                value={lookup.customerId}
                onChange={(event) => setLookup((current) => ({ ...current, customerId: event.target.value }))}
                placeholder={authState.customerId || 'Enter customer ID'}
              />
            </div>
            <div className="field">
              <label htmlFor="home-account-id">Open Account ID</label>
              <input
                id="home-account-id"
                value={lookup.accountId}
                onChange={(event) => setLookup((current) => ({ ...current, accountId: event.target.value }))}
                placeholder="Enter account ID"
              />
            </div>
          </div>
          <div className="actions">
            <button type="button" onClick={openCustomer} disabled={!lookup.customerId}>Open Customer</button>
            <button type="button" onClick={openAccount} disabled={!lookup.accountId}>Open Account</button>
            {authState.customerId ? <Link className="button-link subtle" to={`/customer/${authState.customerId}/accounts`}>My Accounts</Link> : null}
            {!authState.customerId ? <Link className="button-link subtle" to="/customer/create">Create Customer</Link> : null}
            {isAdmin ? <span className="inline-note">Admin users can open any customer or account by ID.</span> : null}
          </div>
        </section>
      )}
    </div>
  );
}
