import { accountApiClient } from './axiosClient';

function toStartOfDayIso(value) {
  return `${value}T00:00:00.000Z`;
}

function toEndOfDayIso(value) {
  return `${value}T23:59:59.999Z`;
}

function splitPeriod(period) {
  const [year, month] = String(period || '').split('-');
  return { year, month };
}

export async function getTransactionHistory({ accountId, startDate, endDate }) {
  const response = await accountApiClient.get(`/accounts/${accountId}/transactions`, {
    params: {
      startDate: startDate ? toStartOfDayIso(startDate) : undefined,
      endDate: endDate ? toEndOfDayIso(endDate) : undefined
    }
  });

  return response.data;
}

export async function exportTransactionHistoryPdf({ accountId, startDate, endDate }) {
  const response = await accountApiClient.get(`/accounts/${accountId}/transactions/export`, {
    params: {
      startDate: toStartOfDayIso(startDate),
      endDate: toEndOfDayIso(endDate)
    },
    responseType: 'blob'
  });

  return response.data;
}

export async function createStandingOrder(payload) {
  const response = await accountApiClient.post(`/accounts/${payload.accountId}/standing-orders`, {
    payeeAccount: payload.payeeAccount,
    payeeName: payload.payeeName,
    amount: payload.amount,
    frequency: payload.frequency,
    startDate: new Date(payload.startDate).toISOString(),
    endDate: payload.endDate ? new Date(payload.endDate).toISOString() : null,
    reference: payload.reference
  });

  return response.data;
}

export async function listStandingOrders(accountId) {
  const response = await accountApiClient.get(`/accounts/${accountId}/standing-orders`);
  return response.data;
}

export async function cancelStandingOrder(standingOrderId) {
  const response = await accountApiClient.delete(`/standing-orders/${standingOrderId}`);
  return response.data;
}

export async function getMonthlyStatement({ accountId, period }) {
  const response = await accountApiClient.get(`/accounts/${accountId}/statements/${period}`);
  return response.data;
}

export async function getSpendingInsights({ accountId, period }) {
  const { year, month } = splitPeriod(period);
  const response = await accountApiClient.get(`/accounts/${accountId}/insights`, {
    params: {
      year,
      month
    }
  });

  return response.data;
}