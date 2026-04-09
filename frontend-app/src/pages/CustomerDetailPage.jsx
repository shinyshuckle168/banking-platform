import { useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deleteCustomer } from '../api/accounts';
import { getCustomer } from '../api/customers';
import { mapAxiosError } from '../api/axiosClient';
import { useAuth } from '../auth/AuthContext';

export function CustomerDetailPage() {
  const navigate = useNavigate();
  const { customerId } = useParams();
  const { authState, clearCustomerId, isAdmin, rememberCustomerId } = useAuth();
  const [error, setError] = useState(null);
  const query = useQuery({
    queryKey: ['customer', customerId],
    queryFn: () => getCustomer(customerId),
    enabled: Boolean(customerId)
  });
  const deleteMutation = useMutation({ mutationFn: deleteCustomer });

  useEffect(() => {
    if (query.data && (!authState.customerId || authState.customerId === String(customerId))) {
      rememberCustomerId(customerId);
    }
  }, [authState.customerId, customerId, query.data, rememberCustomerId]);

  async function handleDelete() {
    setError(null);

    if (!window.confirm('Delete this customer? This succeeds only when there are no active accounts.')) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(customerId);
      if (authState.customerId === String(customerId)) {
        clearCustomerId();
      }
      navigate('/');
    } catch (requestError) {
      setError(mapAxiosError(requestError));
    }
  }

  const customer = query.data;

  return (
    <div className="stack">
      <section className="panel stack">
        <div className="section-header">
          <div>
            <p className="eyebrow">GET /api/customers/{'{customerId}'}</p>
            <h2>Customer Profile</h2>
            <p className="muted">View the stored customer profile and branch into the account flows.</p>
          </div>
          <div className="actions">
            <Link className="button-link subtle" to={`/customer/${customerId}/edit`}>Edit Customer</Link>
            <Link className="button-link" to={`/customer/${customerId}/accounts`}>View Accounts</Link>
          </div>
        </div>
        {query.isLoading ? <div className="banner success">Loading customer profile...</div> : null}
        {error ? <div className="banner error">{error.message}</div> : null}
        {query.error ? <div className="banner error">{mapAxiosError(query.error).message}</div> : null}
      </section>

      {customer ? (
        <section className="panel stack">
          <div className="detail-grid">
            <article className="detail-item">
              <p className="muted">Customer ID</p>
              <strong>{customer.customerId}</strong>
            </article>
            <article className="detail-item">
              <p className="muted">Name</p>
              <strong>{customer.name}</strong>
            </article>
            <article className="detail-item">
              <p className="muted">Type</p>
              <strong>{customer.type}</strong>
            </article>
            <article className="detail-item">
              <p className="muted">Address</p>
              <strong>{customer.address}</strong>
            </article>
          </div>
          <div className="actions">
            <Link className="button-link" to={`/customer/${customerId}/accounts/create`}>Create Account</Link>
            <Link className="button-link subtle" to={`/customer/${customerId}/accounts`}>Account List</Link>
            {isAdmin ? <button type="button" className="danger" onClick={handleDelete} disabled={deleteMutation.isPending}>Delete Customer</button> : null}
          </div>
          <pre className="code">{JSON.stringify(customer, null, 2)}</pre>
        </section>
      ) : null}
    </div>
  );
}