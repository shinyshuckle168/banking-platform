import { axiosClient } from './axiosClient';

export async function createAccount(payload) {
  const response = await axiosClient.post(`/customers/${payload.customerId}/accounts`, {
    accountType: payload.accountType,
    balance: payload.balance,
    interestRate: payload.accountType === 'SAVINGS' ? payload.interestRate : null
  });
  return response.data;
}

export async function getAccount(accountId) {
  const response = await axiosClient.get(`/accounts/${accountId}`);
  return response.data;
}

export async function listCustomerAccounts(customerId) {
  const response = await axiosClient.get(`/customers/${customerId}/accounts`);
  return response.data;
}

export async function updateAccount(payload) {
  const response = await axiosClient.put(`/accounts/${payload.accountId}`, {
    interestRate: payload.interestRate !== '' ? payload.interestRate : null
  });
  return response.data;
}

export async function deleteAccount(accountId) {
  const response = await axiosClient.delete(`/accounts/${accountId}`);
  return response.data;
}

export async function deleteCustomer(customerId) {
  const response = await axiosClient.delete(`/customers/${customerId}`);
  return response.data;
}

export async function depositToAccount(payload) {
  const response = await axiosClient.post(
    `/accounts/${payload.accountId}/deposit`,
    {
      amount: payload.amount,
      description: payload.description || null
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
  const response = await axiosClient.post(
    `/accounts/${payload.accountId}/withdraw`,
    {
      amount: payload.amount,
      description: payload.description || null
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey
      }
    }
  );
  return response.data;
}

export async function transferFunds(payload) {
  const response = await axiosClient.post(
    '/accounts/transfer',
    {
      fromAccountId: Number(payload.fromAccountId),
      toAccountId: Number(payload.toAccountId),
      amount: payload.amount,
      description: payload.description || null
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey
      }
    }
  );
  return response.data;
}
