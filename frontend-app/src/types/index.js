export const ACCOUNT_TYPES = ['CHECKING', 'SAVINGS'];
export const ACCOUNT_STATUSES = ['ACTIVE', 'CLOSED'];
export const CUSTOMER_TYPES = ['PERSON', 'COMPANY'];

export const emptyCreateAccountForm = {
  accountType: 'SAVINGS',
  balance: '0.00',
  interestRate: '0.0500'
};

export const emptyRegisterForm = {
  username: '',
  password: '',
  name: '',
  address: '',
  type: 'PERSON'
};

export const emptyLoginForm = {
  username: '',
  password: ''
};

export const emptyCustomerForm = {
  name: '',
  address: '',
  type: 'PERSON'
};

export const emptyCustomerLookup = {
  customerId: '',
  accountId: ''
};

export const emptyAccountUpdateForm = {
  interestRate: ''
};

export function createIdempotencyKey() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `demo-${Date.now()}`;
}

export const emptyMoneyMovementForm = {
  accountId: '',
  amount: '25.00',
  description: '',
  idempotencyKey: createIdempotencyKey()
};

export function isEmailLike(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}
