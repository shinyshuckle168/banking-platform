import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../api/auth';
import { mapAxiosError } from '../api/axiosClient';
import { emptyRegisterForm, isEmailLike } from '../types';

export function RegisterPage() {
  const navigate = useNavigate();
  const [formState, setFormState] = useState(emptyRegisterForm);
  const [error, setError] = useState(null);
  const mutation = useMutation({ mutationFn: registerUser });

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

    try {
      await mutation.mutateAsync(formState);
      navigate('/login', { state: { registered: true } });
    } catch (requestError) {
      setError(mapAxiosError(requestError));
    }
  }

  return (
    <section className="panel stack auth-panel-page">
      <div>
        <p className="eyebrow">login-api</p>
        <h2>Register</h2>
        <p className="muted">Create a new user account. The backend currently defaults registration to the `CUSTOMER` role.</p>
      </div>
      {error ? <div className="banner error">{error.message}</div> : null}
      <form className="stack" onSubmit={handleSubmit}>
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
        <div className="actions">
          <button type="submit" disabled={mutation.isPending}>Create Account</button>
          <Link className="button-link subtle" to="/login">Back to Login</Link>
        </div>
      </form>
    </section>
  );
}