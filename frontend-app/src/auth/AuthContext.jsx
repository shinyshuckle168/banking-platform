import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { buildAuthenticatedState, emptyAuthState, readStoredAuthState, writeStoredAuthState } from './authState';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [authState, setAuthState] = useState(readStoredAuthState);

  useEffect(() => {
    writeStoredAuthState(authState);
  }, [authState]);

  const value = useMemo(() => ({
    authState,
    isAuthenticated: Boolean(authState.accessToken),
    isAdmin: authState.roles.includes('ADMIN') || authState.roles.includes('ROLE_ADMIN'),
    completeLogin(authResponse, username) {
      setAuthState(buildAuthenticatedState(authResponse, username));
    },
    rememberCustomerId(customerId) {
      setAuthState((current) => ({ ...current, customerId: String(customerId) }));
    },
    clearCustomerId() {
      setAuthState((current) => ({ ...current, customerId: '' }));
    },
    logout() {
      setAuthState(emptyAuthState);
    }
  }), [authState]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return context;
}