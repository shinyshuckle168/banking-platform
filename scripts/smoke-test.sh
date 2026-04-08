#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="smoke.$(date +%s)@example.com"
PASSWORD="SecurePass1!"

echo "Running smoke flow against ${BASE_URL}"

register_response=$(curl -sS -X POST "${BASE_URL}/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}")

login_response=$(curl -sS -X POST "${BASE_URL}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}")

access_token=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])' <<<"${login_response}")

create_response=$(curl -sS -X POST "${BASE_URL}/api/customers" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${access_token}" \
  -d '{"name":"Jamie Customer","address":"10 Main Street","type":"PERSON"}')

customer_id=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["customerId"])' <<<"${create_response}")
updated_at=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["updatedAt"])' <<<"${create_response}")

get_response=$(curl -sS -X GET "${BASE_URL}/api/customers/${customer_id}" \
  -H "Authorization: Bearer ${access_token}")

patch_response=$(curl -sS -X PATCH "${BASE_URL}/api/customers/${customer_id}" \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${access_token}" \
  -d "{\"name\":\"Jamie Customer Updated\",\"address\":\"20 Main Street\",\"type\":\"COMPANY\",\"updatedAt\":\"${updated_at}\"}")

echo "Registered user: ${EMAIL}"
echo "Created customer: ${customer_id}"
echo "Get response: ${get_response}"
echo "Patch response: ${patch_response}"
echo "Smoke test completed successfully."
