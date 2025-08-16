#!/bin/bash
set -euo pipefail

echo "🚀 Setting up Keycloak for E-Commerce Demo..."

# Configuration
KEYCLOAK_URL="http://localhost:8180"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin123"
REALM_NAME="ecommerce-demo"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="$SCRIPT_DIR/realm-config.json"

# Validate config file
if [ ! -f "$CONFIG_FILE" ]; then
  echo "❌ Realm config not found at $CONFIG_FILE"
  exit 1
fi

# Obtain token
echo "🔑 Obtaining admin access token..."
ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then
  echo "❌ Failed to retrieve admin token"
  exit 1
fi
echo "✅ Admin token obtained"

# Check existence
echo "🔍 Checking if realm '$REALM_NAME' exists..."
HTTP_GET=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KEYCLOAK_URL/admin/realms/$REALM_NAME")

if [ "$HTTP_GET" -eq 200 ]; then
  echo "⚠️ Realm exists. Deleting..."
  HTTP_DEL=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM_NAME")
  if [ "$HTTP_DEL" -ne 204 ]; then
    echo "❌ Failed to delete realm (HTTP $HTTP_DEL)"
    exit 1
  fi
  echo "🗑️ Realm deleted"
elif [ "$HTTP_GET" -ne 404 ]; then
  echo "❌ Unexpected response when checking realm (HTTP $HTTP_GET)"
  exit 1
fi

# Import realm
echo "📥 Importing realm from $CONFIG_FILE..."
HTTP_POST=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @"$CONFIG_FILE")

if [ "$HTTP_POST" -ne 201 ]; then
  echo "❌ Realm import failed (HTTP $HTTP_POST)"
  curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "$KEYCLOAK_URL/admin/realms/$REALM_NAME" || true
  exit 1
fi
echo "✅ Realm imported"

# Verify
HTTP_VERIFY=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KEYCLOAK_URL/admin/realms/$REALM_NAME")

if [ "$HTTP_VERIFY" -ne 200 ]; then
  echo "❌ Realm verification failed (HTTP $HTTP_VERIFY)"
  exit 1
fi

echo "🎉 Setup complete! Realm '$REALM_NAME' is ready."
