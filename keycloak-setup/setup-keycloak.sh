#!/bin/bash

# Keycloak Setup Script for E-Commerce Demo
# This script sets up Keycloak with demo realm, users, groups, and clients

echo "🚀 Setting up Keycloak for E-Commerce Demo..."

# Configuration
KEYCLOAK_URL="http://localhost:8180"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin123"
REALM_NAME="ecommerce-demo"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Wait for Keycloak to be ready
echo "⏳ Waiting for Keycloak to be ready..."
until curl -sf "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1; do
    echo "Waiting for Keycloak to start..."
    sleep 5
done

echo "✅ Keycloak is ready!"

# Get admin access token
echo "🔑 Getting admin access token..."
ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Failed to get admin token"
    exit 1
fi

echo "✅ Admin token obtained"

# Check if realm already exists
echo "🔍 Checking if realm exists..."
REALM_EXISTS=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KEYCLOAK_URL/admin/realms/$REALM_NAME" | jq -r '.realm // empty')

if [ "$REALM_EXISTS" == "$REALM_NAME" ]; then
    echo "⚠️  Realm $REALM_NAME already exists. Deleting and recreating..."
    curl -s -X DELETE -H "Authorization: Bearer $ADMIN_TOKEN" \
      "$KEYCLOAK_URL/admin/realms/$REALM_NAME"
    echo "🗑️  Existing realm deleted"
fi

# Import realm configuration
echo "📥 Importing realm configuration..."
IMPORT_RESULT=$(curl -s -X POST "$KEYCLOAK_URL/admin/realms" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/realm-config.json")

if [ $? -eq 0 ]; then
    echo "✅ Realm imported successfully"
else
    echo "❌ Failed to import realm"
    echo "$IMPORT_RESULT"
    exit 1
fi

# Verify realm creation
echo "🔍 Verifying realm creation..."
REALM_VERIFY=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  "$KEYCLOAK_URL/admin/realms/$REALM_NAME" | jq -r '.realm // empty')

if [ "$REALM_VERIFY" == "$REALM_NAME" ]; then
    echo "✅ Realm verification successful"
else
    echo "❌ Realm verification failed"
    exit 1
fi

# Display setup summary
echo ""
echo "🎉 Keycloak setup completed successfully!"
echo ""
echo "📋 Setup Summary:"
echo "=================="
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Admin Console: $KEYCLOAK_URL/admin"
echo "Realm: $REALM_NAME"
echo ""
echo "👥 Demo Users:"
echo "admin / admin123 (Administrator)"
echo "manager / manager123 (Manager)"
echo "employee1 / employee123 (Sales Employee)"
echo "employee2 / employee123 (Support Employee)"
echo "customer1 / customer123 (Premium Customer)"
echo "customer2 / customer123 (Standard Customer)"
echo ""
echo "🏢 Groups:"
echo "- Administrators"
echo "- Managers"
echo "- Employees"
echo "  └── Sales"
echo "  └── Support"
echo "- Customers"
echo "  └── Premium"
echo "  └── Standard"
echo ""
echo "🔑 Roles:"
echo "- admin (Full access)"
echo "- manager (Management access)"
echo "- employee (Limited access)"
echo "- customer (Customer access)"
echo "- user (Default role)"
echo ""
echo "🔧 Clients:"
echo "- ecommerce-backend (Backend service)"
echo "- ecommerce-frontend (Angular frontend)"
echo ""
echo "🚀 Next steps:"
echo "1. Configure your backend application.properties"
echo "2. Configure your frontend Keycloak settings"
echo "3. Test authentication with demo users"
echo ""
