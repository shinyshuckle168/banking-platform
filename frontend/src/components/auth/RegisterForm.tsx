import { FormEvent, useState } from "react";
import { ApiError, apiClient } from "../../lib/api-client";

const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;
const passwordPattern = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

function validate(email: string, password: string): string | null {
  if (!email.trim()) {
    return "Email is required.";
  }

  if (!emailPattern.test(email.trim())) {
    return "Enter a valid email address.";
  }

  if (!password) {
    return "Password is required.";
  }

  if (!passwordPattern.test(password)) {
    return "Password must be at least 8 characters and include an uppercase letter, digit, and special character.";
  }

  return null;
}

function extractErrorMessage(caughtError: unknown): string {
  if (caughtError instanceof ApiError && caughtError.payload?.message) {
    return caughtError.payload.message;
  }

  if (
    typeof caughtError === "object" &&
    caughtError !== null &&
    "payload" in caughtError &&
    typeof (caughtError as { payload?: { message?: unknown } }).payload?.message === "string"
  ) {
    return (caughtError as { payload: { message: string } }).payload.message;
  }

  return "Registration failed. Please try again.";
}

export default function RegisterForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSuccess(null);

    const validationError = validate(email, password);
    if (validationError) {
      setError(validationError);
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await apiClient.request("/auth/register", {
        method: "POST",
        body: {
          email: email.trim().toLowerCase(),
          password,
        },
      });
      setSuccess("Registration complete. You can now log in.");
      setEmail("");
      setPassword("");
    } catch (caughtError) {
      setError(extractErrorMessage(caughtError));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h1>Register</h1>
      <label htmlFor="email">Email</label>
      <input
        id="email"
        name="email"
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
        autoComplete="email"
      />

      <label htmlFor="password">Password</label>
      <input
        id="password"
        name="password"
        type="password"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
        autoComplete="new-password"
      />

      {error ? <p role="alert">{error}</p> : null}
      {success ? <p>{success}</p> : null}

      <button type="submit" disabled={submitting}>
        {submitting ? "Creating account..." : "Create account"}
      </button>
    </form>
  );
}
