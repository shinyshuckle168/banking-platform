import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  iterations: 20,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PASSWORD = 'SecurePass1!';

export default function () {
  const suffix = `${__VU}-${__ITER}-${Date.now()}`;
  const email = `perf.${suffix}@example.com`;

  const registerResponse = http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
    email,
    password: PASSWORD,
  }), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(registerResponse, {
    'register status is 201': (response) => response.status === 201,
  });

  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email,
    password: PASSWORD,
  }), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(loginResponse, {
    'login status is 200': (response) => response.status === 200,
  });

  const accessToken = loginResponse.json('accessToken');
  const authHeaders = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
  };

  const createResponse = http.post(`${BASE_URL}/api/customers`, JSON.stringify({
    name: 'Jamie Customer',
    address: '10 Main Street',
    type: 'PERSON',
  }), authHeaders);
  check(createResponse, {
    'create customer status is 201': (response) => response.status === 201,
  });

  const customerId = createResponse.json('customerId');
  const updatedAt = createResponse.json('updatedAt');

  const getResponse = http.get(`${BASE_URL}/api/customers/${customerId}`, authHeaders);
  check(getResponse, {
    'get customer status is 200': (response) => response.status === 200,
  });

  const patchResponse = http.patch(`${BASE_URL}/api/customers/${customerId}`, JSON.stringify({
    name: 'Jamie Customer Updated',
    address: '20 Main Street',
    type: 'COMPANY',
    updatedAt,
  }), authHeaders);
  check(patchResponse, {
    'patch customer status is 200': (response) => response.status === 200,
  });

  sleep(1);
}
