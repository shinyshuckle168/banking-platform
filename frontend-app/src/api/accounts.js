import { accountApiClient } from './axiosClient';

export async function createAccount(payload) {
  const body = {
    accountType: payload.accountType,
    balance: payload.balance
  };

  if (payload.accountType === 'SAVINGS') {
    body.interestRate = payload.interestRate;
  }

  const response = await accountApiClient.post(`/customers/${payload.customerId}/accounts`, body);
  return response.data;
}

export async function getAccount(accountId) {
  const response = await accountApiClient.get(`/accounts/${accountId}`);
  return response.data;
}

export async function listCustomerAccounts(customerId) {
  const response = await accountApiClient.get(`/customers/${customerId}/accounts`);
  return response.data;
}

export async function updateAccount(payload) {
  const body = {};

  if (payload.interestRate !== '') {
    body.interestRate = payload.interestRate;
  }

  const response = await accountApiClient.put(`/accounts/${payload.accountId}`, body);
  return response.data;
}

export async function deleteAccount(accountId) {
  const response = await accountApiClient.delete(`/accounts/${accountId}`);
  return response.data;
}

export async function deleteCustomer(customerId) {
  const response = await accountApiClient.delete(`/api/customers/${customerId}`);
  return response.data;
}

export async function depositToAccount(payload) {
  const response = await accountApiClient.post(
    `/accounts/${payload.accountId}/deposit`,
    {
      amount: payload.amount,
      description: payload.description || null,
      category: payload.category || null
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey
      }
    }
  );
  return response.data;
}

export async function withdrawFromAccount(payload) {
  const response = await accountApiClient.post(
    `/accounts/${payload.accountId}/withdraw`,
    {
      amount: payload.amount,
      description: payload.description || null,
      category: payload.category || null
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey
      }
    }
  );
  return response.data;
}

export async function transferBetweenAccounts(payload) {
  const response = await accountApiClient.post(
    '/accounts/transfer',
    {
      fromAccountId: payload.fromAccountId,
      toAccountId: payload.toAccountId,
      amount: payload.amount,
      description: payload.description || null,
      category: payload.category || null
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey
      }
    }
  );
  return response.data;
}
