const STORAGE_KEY = 'banking-app-auth';

export const emptyAuthState = {
  accessToken: '',
  refreshToken: '',
  tokenType: 'Bearer',
  expiresIn: null,
  expiresAt: null,
  username: '',
  subject: '',
  roles: [],
  customerId: ''
};

function decodeBase64Url(value) {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');
  return window.atob(padded);
}

export function decodeJwt(token) {
  if (!token || !token.includes('.')) {
    return {};
  }

  try {
    const [, payload] = token.split('.');
    return JSON.parse(decodeBase64Url(payload));
  } catch {
    return {};
  }
}

function normalizeRoles(roles) {
  if (Array.isArray(roles)) {
    return roles.filter(Boolean);
  }

  if (typeof roles === 'string' && roles.trim().length > 0) {
    return roles.split(',').map((value) => value.trim()).filter(Boolean);
  }

  return [];
}

export function readStoredAuthState() {
  const saved = window.localStorage.getItem(STORAGE_KEY);
  if (!saved) {
    return emptyAuthState;
  }

  try {
    const parsed = JSON.parse(saved);
    return {
      ...emptyAuthState,
      ...parsed,
      roles: normalizeRoles(parsed.roles)
    };
  } catch {
    return emptyAuthState;
  }
}

export function writeStoredAuthState(state) {
  if (!state.accessToken) {
    window.localStorage.removeItem(STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

export function buildAuthenticatedState(authResponse, username) {
  const existingState = readStoredAuthState();
  const decoded = decodeJwt(authResponse.accessToken);
  const subject = decoded.sub || existingState.subject || '';
  const roles = normalizeRoles(decoded.roles || existingState.roles);
  const expiresIn = Number(authResponse.expiresIn || 0) || null;
  const expiresAt = expiresIn ? Date.now() + expiresIn * 1000 : null;
  const canReuseCustomerContext = existingState.subject && existingState.subject === subject;

  return {
    accessToken: authResponse.accessToken,
    refreshToken: authResponse.refreshToken || '',
    tokenType: authResponse.tokenType || 'Bearer',
    expiresIn,
    expiresAt,
    username,
    subject,
    roles,
    customerId: canReuseCustomerContext ? existingState.customerId : ''
  };
}