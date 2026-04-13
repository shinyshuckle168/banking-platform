import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register, login } from '../../api/authApi';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(username, password);
      const res = await login(username, password);
      const token = res.data?.token || res.data?.accessToken || res.data?.jwt;
      if (!token) throw new Error('No token in response');
      localStorage.setItem('jwt', token);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Username may already exist.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Digital <span>Banking</span></h1>
        <h2 className="auth-heading">Create account</h2>
        <form onSubmit={handleSubmit} className="auth-form">
          <label htmlFor="reg-username">Username</label>
          <input
            id="reg-username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            autoComplete="username"
          />
          <label htmlFor="reg-password">Password</label>
          <input
            id="reg-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="new-password"
          />
          {error && <p className="auth-error">{error}</p>}
          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? 'Creating account…' : 'Register'}
          </button>
        </form>
        <p className="auth-link">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
