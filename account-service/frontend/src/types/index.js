export const ACCOUNT_TYPES = ['CHECKING', 'SAVINGS'];
export const ACCOUNT_STATUSES = ['ACTIVE', 'CLOSED'];

export const emptyCreateAccountForm = {
  customerId: '100',
  accountType: 'SAVINGS',
  balance: '0.00',
  interestRate: '0.0500'
};

export function createIdempotencyKey() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `demo-${Date.now()}`;
}

export const emptyMoneyMovementForm = {
  accountId: '1000',
  amount: '25.00',
  description: '',
  idempotencyKey: createIdempotencyKey()
};

export const emptyTransferForm = {
  fromAccountId: '1000',
  toAccountId: '1001',
  amount: '25.00',
  description: '',
  idempotencyKey: createIdempotencyKey()
};

export const emptyAuthState = {
  token: 'demo-token',
  customerId: '100',
  roles: 'CUSTOMER'
};
