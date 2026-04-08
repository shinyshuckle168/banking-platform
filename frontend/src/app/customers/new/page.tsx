import { FormEvent, useState } from "react";
import { ApiError } from "../../../lib/api-client";
import { authenticatedApiClient } from "../../../lib/auth-session";

export default function CreateCustomerPage() {
  const [name, setName] = useState("");
  const [address, setAddress] = useState("");
  const [type, setType] = useState("PERSON");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSuccess(null);

    if (!name.trim() || !address.trim()) {
      setError("Name and address are required.");
      return;
    }

    try {
      await authenticatedApiClient.request("/customers", {
        method: "POST",
        body: {
          name: name.trim(),
          address: address.trim(),
          type,
        },
      });
      setSuccess("Customer created successfully.");
      setName("");
      setAddress("");
      setType("PERSON");
    } catch (caughtError) {
      if (caughtError instanceof ApiError && caughtError.payload?.message) {
        setError(caughtError.payload.message);
        return;
      }

      if (
        typeof caughtError === "object" &&
        caughtError !== null &&
        "payload" in caughtError &&
        typeof (caughtError as { payload?: { message?: unknown } }).payload?.message === "string"
      ) {
        setError((caughtError as { payload: { message: string } }).payload.message);
        return;
      }

      setError("Customer creation failed. Please try again.");
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h1>Create customer</h1>
      <label htmlFor="customer-name">Name</label>
      <input
        id="customer-name"
        value={name}
        onChange={(event) => setName(event.target.value)}
      />

      <label htmlFor="customer-address">Address</label>
      <input
        id="customer-address"
        value={address}
        onChange={(event) => setAddress(event.target.value)}
      />

      <label htmlFor="customer-type">Type</label>
      <select
        id="customer-type"
        value={type}
        onChange={(event) => setType(event.target.value)}
      >
        <option value="PERSON">PERSON</option>
        <option value="COMPANY">COMPANY</option>
      </select>

      {error ? <p role="alert">{error}</p> : null}
      {success ? <p>{success}</p> : null}

      <button type="submit">Create customer</button>
    </form>
  );
}
