import { FormEvent, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { authenticatedApiClient } from "../../../../lib/auth-session";

type CustomerDetail = {
  customerId: number;
  name: string;
  address: string;
  type: "PERSON" | "COMPANY";
  updatedAt: string;
};

export default function EditCustomerPage() {
  const { customerId } = useParams();
  const [customer, setCustomer] = useState<CustomerDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!customerId) {
      return;
    }

    authenticatedApiClient.request<CustomerDetail>(`/customers/${customerId}`)
      .then((result) => setCustomer(result))
      .catch(() => setError("Unable to load customer."));
  }, [customerId]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!customerId || !customer) {
      return;
    }

    try {
      const updated = await authenticatedApiClient.request<CustomerDetail>(`/customers/${customerId}`, {
        method: "PATCH",
        body: customer,
      });
      setCustomer(updated);
      setError(null);
      setSuccess("Customer updated successfully.");
    } catch (caughtError) {
      if (
        typeof caughtError === "object" &&
        caughtError !== null &&
        "payload" in caughtError &&
        typeof (caughtError as { payload?: { message?: unknown } }).payload?.message === "string"
      ) {
        setError((caughtError as { payload: { message: string } }).payload.message);
        return;
      }
      setError("Unable to update customer." );
    }
  }

  if (!customer) {
    return <p>{error ?? "Loading customer..."}</p>;
  }

  return (
    <form onSubmit={handleSubmit}>
      <h1>Edit customer</h1>
      <label htmlFor="edit-name">Name</label>
      <input
        id="edit-name"
        value={customer.name}
        onChange={(event) => setCustomer({ ...customer, name: event.target.value })}
      />

      <label htmlFor="edit-address">Address</label>
      <input
        id="edit-address"
        value={customer.address}
        onChange={(event) => setCustomer({ ...customer, address: event.target.value })}
      />

      {error ? <p role="alert">{error}</p> : null}
      {success ? <p>{success}</p> : null}

      <button type="submit">Save changes</button>
    </form>
  );
}
