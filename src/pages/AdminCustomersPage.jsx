function Badge({ type }) {
  const color = type === 'PERSON' ? 'badge-person' : 'badge-company';
  return <span className={`badge ${color}`}>{type}</span>;
}
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { accountApiClient } from '../api/axiosClient';

export default function AdminCustomersPage() {
  function Spinner() {
    return <div className="banner success" style={{ margin: '2em auto', textAlign: 'center' }}>Loading…</div>;
  }

  const { isAdmin, rememberCustomerId } = useAuth();
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState(null);
  useEffect(() => {
    if (!isAdmin) {
      navigate('/');
      return;
    }
    setLoading(true);
    accountApiClient.get('/api/customers')
      .then(res => setCustomers(res.data))
      .catch(() => setError('Failed to load customers.'))
      .finally(() => setLoading(false));
  }, [isAdmin, navigate]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return customers;
    return customers.filter(c =>
      c.name.toLowerCase().includes(q) || String(c.customerId).includes(q)
    );
  }, [customers, search]);

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <h2>Customers</h2>
          <p className="muted" style={{ margin: '0.25rem 0 1.25rem 0' }}>All customers in the system.</p>
        </div>
        <div className="field" style={{ maxWidth: 350 }}>
          <input
            type="text"
            className="input"
            placeholder="Search by name or ID..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ marginBottom: 0, padding: 8, width: '100%', borderRadius: 6, border: '1px solid #ccc' }}
          />
        </div>
        {error ? <div className="banner error" style={{ margin: '1em 0' }}>{error}</div> : null}
        {loading ? <Spinner /> : null}
      </section>
      <section className="table-shell accounts-table-shell">
        {error ? (
          <div className="panel">
            <h3>Customers unavailable</h3>
            <p className="muted">The customers list cannot be displayed right now.</p>
          </div>
        ) : filtered.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>Customer ID</th>
                <th>Name</th>
                <th>Type</th>
                <th>Total Accounts</th>
                <th>Member Since</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((customer) => (
                <tr key={customer.customerId}>
                  <td>{customer.customerId}</td>
                  <td>{customer.name}</td>
                  <td><Badge type={customer.type} /></td>
                  <td>{Array.isArray(customer.accounts) ? customer.accounts.length : 0}</td>
                  <td>{customer.createdAt ? new Date(customer.createdAt).toLocaleDateString() : ''}</td>
                  <td>
                    <button
                      className="table-action-btn"
                      onClick={() => navigate(`/customer/${customer.customerId}`)}
                    >
                      Manage
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : !loading ? (
          <div className="panel">
            <h3>No customers found in the system.</h3>
            <p className="muted">No customers exist yet.</p>
          </div>
        ) : null}
      </section>
    </div>
  );
}
