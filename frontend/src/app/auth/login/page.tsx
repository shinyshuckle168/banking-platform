import { FormEvent, useState } from "react";
import { ApiError, apiClient } from "../../../lib/api-client";
import { persistSession } from "../../../lib/auth-session";

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

  return "Login failed. Please try again.";
}

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSuccess(null);

    if (!email.trim() || !password) {
      setError("Email and password are required.");
      return;
    }

    try {
      const session = await apiClient.request<{
        accessToken: string;
        refreshToken: string;
        tokenType: string;
        expiresIn: number;
      }>("/auth/login", {
        method: "POST",
        body: {
          email: email.trim().toLowerCase(),
          password,
        },
      });
      persistSession(session);
      setSuccess("Login successful.");
    } catch (caughtError) {
      setError(extractErrorMessage(caughtError));
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h1>Login</h1>
      <label htmlFor="login-email">Email</label>
      <input
        id="login-email"
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
      />

      <label htmlFor="login-password">Password</label>
      <input
        id="login-password"
        type="password"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
      />

      {error ? <p role="alert">{error}</p> : null}
      {success ? <p>{success}</p> : null}

      <button type="submit">Sign in</button>
    </form>
  );
}
