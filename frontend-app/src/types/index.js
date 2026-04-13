export const ACCOUNT_TYPES = ['CHECKING', 'SAVINGS'];
export const ACCOUNT_STATUSES = ['ACTIVE', 'CLOSED'];
export const CUSTOMER_TYPES = ['PERSON', 'COMPANY'];
export const STANDING_ORDER_FREQUENCIES = ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY'];

function toDateInputValue(date) {
  return date.toISOString().slice(0, 10);
}

function toMonthInputValue(date) {
  return date.toISOString().slice(0, 7);
}

function toDateTimeLocalValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');

  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function getDaysAgo(days) {
  const date = new Date();
  date.setUTCDate(date.getUTCDate() - days);
  return toDateInputValue(date);
}

function getFutureDateTime(daysAhead) {
  const date = new Date();
  date.setDate(date.getDate() + daysAhead);
  date.setHours(9, 0, 0, 0);
  return toDateTimeLocalValue(date);
}

function getPreviousMonthValue() {
  const date = new Date();
  date.setUTCMonth(date.getUTCMonth() - 1);
  return toMonthInputValue(date);
}

function getCurrentMonthValue() {
  return toMonthInputValue(new Date());
}

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

export const emptyTransactionHistoryFilters = {
  startDate: getDaysAgo(28),
  endDate: getDaysAgo(0)
};

export const emptyStandingOrderForm = {
  payeeAccount: '',
  payeeName: '',
  amount: '50.00',
  frequency: 'MONTHLY',
  startDate: getFutureDateTime(2),
  endDate: '',
  reference: ''
};

export const emptyMonthlyStatementLookup = {
  period: getPreviousMonthValue()
};

export const emptySpendingInsightsLookup = {
  period: getCurrentMonthValue()
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
