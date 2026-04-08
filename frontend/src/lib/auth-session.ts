import { ApiClient } from "./api-client";

export type AuthSession = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
};

const SESSION_KEY = "bankapp.auth.session";

export function persistSession(session: AuthSession): void {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function readSession(): AuthSession | null {
  const stored = sessionStorage.getItem(SESSION_KEY);
  if (!stored) {
    return null;
  }

  try {
    return JSON.parse(stored) as AuthSession;
  } catch {
    sessionStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function clearSession(): void {
  sessionStorage.removeItem(SESSION_KEY);
}

export function getAccessToken(): string | null {
  return readSession()?.accessToken ?? null;
}

export const authenticatedApiClient = new ApiClient({
  getAccessToken,
});
