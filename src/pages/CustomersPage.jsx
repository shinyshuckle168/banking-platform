import { useQuery } from '@tanstack/react-query';
import { listCustomers } from '../api/customers';
import { useNavigate } from 'react-router-dom';

async function deleteCustomer(customerId) {
  await fetch(`/api/customers/${customerId}`, { method: 'DELETE' });
}

export function CustomersPage() {
  const navigate = useNavigate();
  const { data: customers, isLoading, error, refetch } = useQuery(['customers'], listCustomers);

  const handleEdit = (customerId) => {
    navigate(`/customer/${customerId}/edit`);
  };

  const handleDelete = async (customerId, hasAccounts) => {
    if (hasAccounts) return;
    if (window.confirm('Are you sure you want to delete this customer?')) {
      await deleteCustomer(customerId);
      refetch();
    }
  };

  if (isLoading) return <div>Loading customers...</div>;
  if (error) return <div>Error loading customers.</div>;

  return (
    <section className="panel stack">
      <h2>Customers</h2>
      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Type</th>
            <th>Accounts</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {customers && customers.length === 0 && (
            <tr><td colSpan={6}>No customers found.</td></tr>
          )}
          {customers && customers.map((customer) => (
            <tr key={customer.customerId}>
              <td>{customer.customerId}</td>
              <td>{customer.name}</td>
              <td>{customer.email}</td>
              <td>{customer.type}</td>
              <td>{customer.accounts ? customer.accounts.length : 0}</td>
              <td>
                <button onClick={() => handleEdit(customer.customerId)}>Edit</button>
                <button
                  onClick={() => handleDelete(customer.customerId, customer.accounts && customer.accounts.length > 0)}
                  disabled={customer.accounts && customer.accounts.length > 0}
                  className={customer.accounts && customer.accounts.length > 0 ? 'disabled' : ''}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
