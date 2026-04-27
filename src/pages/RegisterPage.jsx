import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { loginUser, registerUser } from '../api/auth';
import { createCustomer } from '../api/customers';
import { useAuth } from '../auth/AuthContext';
import { mapAxiosError } from '../api/axiosClient';
import { CUSTOMER_TYPES, emptyRegisterForm, isEmailLike } from '../types';

const REGISTER_CUSTOMER_TYPE_LABELS = {
  PERSON: 'Personal',
  COMPANY: 'Business'
};

export function RegisterPage() {
  const navigate = useNavigate();
  const { completeLogin, rememberCustomerId } = useAuth();
  const [formState, setFormState] = useState(emptyRegisterForm);
  const [error, setError] = useState(null);
  const mutation = useMutation({
    mutationFn: async (form) => {
      const authPayload = {
        username: form.username,
        password: form.password
      };
      const customerPayload = {
        name: form.name,
        address: form.address,
        type: form.type
      };

      await registerUser(authPayload);
      const authResponse = await loginUser(authPayload);
      const customerResponse = await createCustomer(customerPayload, authResponse.accessToken);

      return {
        authResponse,
        customerResponse
      };
    }
  });
  const [step, setStep] = useState('selectType');
  const isPerson = formState.type === 'PERSON';

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    if (!isEmailLike(formState.username)) {
      setError({ message: 'Enter a valid email address.' });
      return;
    }

    if (!formState.password) {
      setError({ message: 'Password is required.' });
      return;
    }

    if (!formState.name.trim()) {
      setError({ message: 'Name is required.' });
      return;
    }

    if (!formState.address.trim()) {
      setError({ message: 'Address is required.' });
      return;
    }

    try {
      const { authResponse, customerResponse } = await mutation.mutateAsync(formState);
      completeLogin(authResponse, formState.username);
      rememberCustomerId(customerResponse.customerId);
      navigate(`/customer/${customerResponse.customerId}`, { replace: true });
    } catch (requestError) {
      setError(mapAxiosError(requestError));
    }
  }

  return (
    <div className="register-page">
      <div className="register-card stack">
        <div>
          <h2>Register</h2>
        </div>
        <div className="register-stepper">
          <span className={`register-step ${step === 'selectType' ? 'active' : 'completed'}`}>
            {step !== 'selectType' && <span className="register-step-check">✓</span>}
            1. Account Type
          </span>
          <span className="register-step-arrow">→</span>
          <span className={`register-step ${step === 'details' ? 'active' : ''}`}>2. Details</span>
        </div>
        {error ? <div className="banner error">{error.message}</div> : null}
        <form className="stack" onSubmit={handleSubmit}>
          {step === 'selectType' ? (
            <div className="form-grid">
              <div className="field full">
                <label htmlFor="register-type">Account Type</label>
                <select
                  id="register-type"
                  value={formState.type}
                  onChange={(event) => setFormState((current) => ({ ...current, type: event.target.value }))}
                >
                  {CUSTOMER_TYPES.map((type) => (
                    <option key={type} value={type}>{REGISTER_CUSTOMER_TYPE_LABELS[type] || type}</option>
                  ))}
                </select>
              </div>
            </div>
          ) : (
            <div className="stack">
              <div className="field">
                <label htmlFor="register-username">Email</label>
                <input
                  id="register-username"
                  type="email"
                  value={formState.username}
                  onChange={(event) => setFormState((current) => ({ ...current, username: event.target.value }))}
                />
              </div>
              <div className="field">
                <label htmlFor="register-password">Password</label>
                <input
                  id="register-password"
                  type="password"
                  value={formState.password}
                  onChange={(event) => setFormState((current) => ({ ...current, password: event.target.value }))}
                />
                <p className="field-hint">Minimum 8 characters with uppercase, digit, and special character.</p>
              </div>
              <div className="field">
                <label htmlFor="register-name">{isPerson ? 'Full Name' : 'Company Name'}</label>
                <input
                  id="register-name"
                  value={formState.name}
                  onChange={(event) => setFormState((current) => ({ ...current, name: event.target.value }))}
                />
              </div>
              <div className="field">
                <label htmlFor="register-address">Address</label>
                <input
                  id="register-address"
                  value={formState.address}
                  onChange={(event) => setFormState((current) => ({ ...current, address: event.target.value }))}
                />
              </div>
            </div>
          )}
          <div className="actions">
            {step === 'selectType' ? (
              <>
                <button type="button" onClick={() => setStep('details')}>Continue</button>
                <Link className="button-link subtle" to="/login">Back to Login</Link>
              </>
            ) : (
              <>
                <button type="button" className="secondary" onClick={() => setStep('selectType')}>Back</button>
                <button type="submit" disabled={mutation.isPending}>Create Account</button>
              </>
            )}
          </div>
        </form>
      </div>
    </div>
  );
}