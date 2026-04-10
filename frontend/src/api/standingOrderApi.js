import axiosInstance from './axiosInstance';

export const createStandingOrder = (accountId, payload) =>
  axiosInstance.post(`/accounts/${accountId}/standing-orders`, payload);

export const listStandingOrders = (accountId) =>
  axiosInstance.get(`/accounts/${accountId}/standing-orders`);

export const cancelStandingOrder = (standingOrderId) =>
  axiosInstance.delete(`/standing-orders/${standingOrderId}`);
