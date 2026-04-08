import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { authenticatedApiClient } from "../../../lib/auth-session";

type CustomerDetail = {
  customerId: number;
  name: string;
  address: string;
  type: string;
  createdAt: string;
  updatedAt: string;
};

export default function CustomerDetailPage() {
  const { customerId } = useParams();
  const [customer, setCustomer] = useState<CustomerDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!customerId) {
      return;
    }

    authenticatedApiClient.request<CustomerDetail>(`/customers/${customerId}`)
      .then((result) => setCustomer(result))
      .catch(() => setError("Unable to load customer."));
  }, [customerId]);

  if (error) {
    return <p role="alert">{error}</p>;
  }

  if (!customer) {
    return <p>Loading customer...</p>;
  }

  return (
    <section>
      <h1>{customer.name}</h1>
      <p>{customer.address}</p>
      <p>{customer.type}</p>
      <p>{customer.updatedAt}</p>
    </section>
  );
}
