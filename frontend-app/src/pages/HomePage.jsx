import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { getCustomer } from '../api/customers';
import { mapAxiosError } from '../api/axiosClient';
import { useAuth } from '../auth/AuthContext';
import { emptyCustomerLookup } from '../types';

export function HomePage() {
  const navigate = useNavigate();
  const { authState, isAuthenticated, isAdmin, rememberCustomerId } = useAuth();
  const [lookup, setLookup] = useState(emptyCustomerLookup);
  const [linkError, setLinkError] = useState(null);
  const [linkMessage, setLinkMessage] = useState(null);
  const [isLinkingCustomer, setIsLinkingCustomer] = useState(false);

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

  async function linkCustomerProfile() {
    if (!lookup.customerId) {
      setLinkError({ message: 'Enter your customer ID first.' });
      setLinkMessage(null);
      return;
    }

    setIsLinkingCustomer(true);
    setLinkError(null);
    setLinkMessage(null);

    try {
      const customer = await getCustomer(lookup.customerId);
      rememberCustomerId(customer.customerId);
      setLinkMessage(`Customer profile ${customer.customerId} linked for this browser session.`);
      navigate(`/customer/${customer.customerId}`);
    } catch (requestError) {
      const mapped = mapAxiosError(requestError);

      if (mapped.code === 'UNAUTHORISED' || mapped.code === 'UNAUTHORIZED' || mapped.code === 'CUSTOMER_NOT_FOUND') {
        setLinkError({
          ...mapped,
          message: 'That customer profile is not available for the logged-in user. Check the customer ID and try again.'
        });
      } else {
        setLinkError(mapped);
      }
    } finally {
      setIsLinkingCustomer(false);
    }
  }

  return (
    <div className="stack">
      <section className="hero">
        <span className="kicker">Unified banking client</span>
        <h2>Authentication, customer, account, and transfer flows in one UI</h2>
        <p className="lede">
          This frontend targets the merged Spring Boot backend running on a single API surface for auth, customer management, accounts, and transfers.
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
            <strong>1 merged service</strong>
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
          <p className="muted">Registration now creates the initial customer profile, and admins can switch active customer context from the profile and accounts views.</p>
        </article>
        <article className="panel">
          <h3>Accounts</h3>
          <p className="muted">List and create checking or savings accounts from one page, with admin-only customer switching for cross-customer support work.</p>
        </article>
        <article className="panel">
          <h3>Money Movement</h3>
          <p className="muted">Deposit, withdraw, and transfer are all live through the merged backend, with a fresh idempotency key generated automatically for each submit.</p>
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
          {isAdmin ? (
            <>
              <p className="muted">
                Admin users can jump to any customer or account by ID, then switch customer context directly from the profile or accounts page.
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
            </>
          ) : (
            <>
              <p className="muted">
                Customer users only see their own remembered customer context in the navigation. Registration creates that context automatically for new users, and existing users can relink it manually if this browser has lost the saved link.
              </p>
              {!authState.customerId ? (
                <div className="stack tight-gap">
                  <div className="field">
                    <label htmlFor="link-customer-id">Link Existing Customer ID</label>
                    <input
                      id="link-customer-id"
                      value={lookup.customerId}
                      onChange={(event) => setLookup((current) => ({ ...current, customerId: event.target.value }))}
                      placeholder="Enter your customer ID"
                    />
                    <p className="field-hint">The app verifies the ID through your existing customer access before saving it locally.</p>
                  </div>
                  {linkError ? <div className="banner error">{linkError.message}</div> : null}
                  {linkMessage ? <div className="banner success">{linkMessage}</div> : null}
                </div>
              ) : null}
            </>
          )}
          <div className="actions">
            {isAdmin ? <button type="button" onClick={openCustomer} disabled={!lookup.customerId}>Open Customer</button> : null}
            {isAdmin ? <button type="button" onClick={openAccount} disabled={!lookup.accountId}>Open Account</button> : null}
            {authState.customerId ? <Link className="button-link" to={`/customer/${authState.customerId}`}>My Profile</Link> : null}
            {authState.customerId ? <Link className="button-link subtle" to={`/customer/${authState.customerId}/accounts`}>My Accounts</Link> : null}
            {!isAdmin && !authState.customerId ? <button type="button" onClick={linkCustomerProfile} disabled={!lookup.customerId || isLinkingCustomer}>Link My Profile</button> : null}
            {!authState.customerId ? <Link className="button-link subtle" to="/customer/create">Create Customer</Link> : null}
            {isAdmin ? <span className="inline-note">Admin users can open any customer or account by ID.</span> : null}
          </div>
        </section>
      )}
    </div>
  );
}
