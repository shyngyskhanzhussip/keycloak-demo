# 🔐 Keycloak Authentication Setup Guide

This guide provides step-by-step instructions for setting up Keycloak authentication with the E-Commerce Demo application.

## 📋 Prerequisites

- Docker and Docker Compose installed
- Node.js 18+ and npm
- Java 17+ and Maven
- Git
- curl and jq (for setup scripts)

## 🚀 Quick Start

### 1. Start Keycloak Infrastructure

```bash
# Start Keycloak and PostgreSQL
docker-compose up -d

# Wait for services to be ready (this may take 2-3 minutes)
docker-compose logs -f keycloak
```

### 2. Setup Demo Realm and Users

```bash
# Make the setup script executable
chmod +x keycloak-setup/setup-keycloak.sh

# Run the setup script
./keycloak-setup/setup-keycloak.sh
```

### 3. Start Backend Application

```bash
cd backend
./mvnw spring-boot:run
```

### 4. Start Frontend Application

```bash
cd frontend
npm install
ng serve
```

## 🔧 Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Keycloak Admin Console | http://localhost:8180/admin | admin / admin123 |
| Frontend Application | http://localhost:4200 | See demo users below |
| Backend API | http://localhost:8080 | Protected by JWT |
| PostgreSQL Database | localhost:5433 | keycloak / keycloak123 |
| PgAdmin (Optional) | http://localhost:8081 | admin@example.com / admin123 |

## 👥 Demo Users

| Username | Password | Role | Access Level |
|----------|----------|------|--------------|
| admin | admin123 | Administrator | Full access to all features |
| manager | manager123 | Manager | Product and order management |
| employee1 | employee123 | Sales Employee | Limited product and order access |
| employee2 | employee123 | Support Employee | Order viewing and updates |
| customer1 | customer123 | Premium Customer | Customer features only |
| customer2 | customer123 | Standard Customer | Basic customer features |

## 🏢 Organizational Structure

### Groups
- **Administrators**: Full system access
- **Managers**: Management functions
- **Employees**: 
  - Sales: Product-focused access
  - Support: Customer service access
- **Customers**:
  - Premium: Enhanced features
  - Standard: Basic features

### Roles and Permissions

| Role | Products | Orders | Admin Functions |
|------|----------|--------|-----------------|
| admin | Full CRUD | Full CRUD | ✅ All admin features |
| manager | Create, Update, View | Full CRUD | ❌ Limited admin access |
| employee | View | View, Update status | ❌ No admin access |
| customer | View | Create own orders | ❌ No admin access |

## 🔑 API Authentication

### Backend Configuration

The backend is configured as an OAuth2 Resource Server with the following features:

- **JWT Token Validation**: Validates tokens against Keycloak
- **Role-based Access Control**: Method-level security annotations
- **CORS Configuration**: Allows frontend access
- **Token Refresh**: Automatic token refresh handling

### Key Endpoints

```bash
# Authentication Status
GET /api/auth/check

# User Profile
GET /api/auth/user-info

# Role Information
GET /api/auth/roles

# Protected Product Endpoints
GET /api/products          # All authenticated users
POST /api/products         # Admin, Manager only
PUT /api/products/{id}     # Admin, Manager only
DELETE /api/products/{id}  # Admin only

# Protected Order Endpoints
GET /api/orders            # Admin, Manager, Employee
POST /api/orders           # All authenticated users
PUT /api/orders/{id}       # Admin, Manager, Employee
DELETE /api/orders/{id}    # Admin only
```

## 🎨 Frontend Integration

### Features Implemented

1. **Keycloak Service**: Complete authentication management
2. **Auth Guards**: Route protection based on roles
3. **HTTP Interceptor**: Automatic token attachment and refresh
4. **User Profile Component**: Rich user interface with role display
5. **Role-based UI**: Dynamic interface based on user permissions

### Key Components

- `KeycloakService`: Core authentication service
- `AuthGuard`: Route-level authentication
- `RoleGuard`: Role-based route protection
- `AuthInterceptor`: HTTP request authentication
- `UserProfileComponent`: User interface component

## 🛡️ Security Features

### Backend Security
- JWT token validation with Keycloak public keys
- Role-based method security with `@PreAuthorize`
- CORS configuration for cross-origin requests
- Token expiration handling and refresh
- Comprehensive audit logging

### Frontend Security
- Automatic token refresh before expiration
- Route guards for authentication and authorization
- Secure token storage in memory (not localStorage)
- PKCE flow for enhanced security
- Silent authentication checks

## 🔄 Token Management

### Token Lifecycle
1. **Initial Authentication**: User logs in via Keycloak
2. **Token Storage**: JWT stored in memory (not persistent)
3. **Automatic Refresh**: Tokens refreshed before expiration
4. **Request Authentication**: Tokens automatically added to API calls
5. **Logout Handling**: Complete session cleanup

### Token Refresh Strategy
- Tokens are refreshed automatically 30 seconds before expiration
- Failed refresh attempts trigger re-authentication
- Silent refresh minimizes user interruption

## 🧪 Testing Authentication

### Test User Login
1. Go to http://localhost:4200
2. Click "Login" button
3. Use any of the demo users listed above
4. Verify role-based access to features

### Test API Access
```bash
# Get token (replace with actual login)
TOKEN="your-jwt-token-here"

# Test authenticated endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/auth/user-info

# Test role-based endpoint (admin only)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/auth/admin/info
```

## 🚨 Troubleshooting

### Common Issues

1. **Keycloak not starting**
   ```bash
   # Check logs
   docker-compose logs keycloak
   
   # Restart services
   docker-compose down && docker-compose up -d
   ```

2. **Realm import failed**
   ```bash
   # Check if Keycloak is ready
   curl http://localhost:8180/health/ready
   
   # Re-run setup script
   ./keycloak-setup/setup-keycloak.sh
   ```

3. **Frontend authentication issues**
   - Check browser console for errors
   - Verify Keycloak is running on port 8180
   - Check CORS configuration in backend

4. **Backend JWT validation errors**
   - Verify issuer URI in application.properties
   - Check Keycloak realm configuration
   - Ensure client secrets match

### Logs and Debugging

```bash
# Keycloak logs
docker-compose logs -f keycloak

# Backend logs (authentication details)
# Check console output when running ./mvnw spring-boot:run

# Frontend logs
# Check browser developer console
```

## 🔧 Configuration Files

### Key Configuration Files
- `docker-compose.yml`: Infrastructure setup
- `keycloak-setup/realm-config.json`: Keycloak realm configuration
- `backend/src/main/resources/application.properties`: Backend auth config
- `frontend/src/app/services/keycloak.service.ts`: Frontend auth service

### Environment Variables
- `KEYCLOAK_ADMIN`: Keycloak admin username
- `KEYCLOAK_ADMIN_PASSWORD`: Keycloak admin password
- `KC_DB_URL`: Database connection URL
- `KC_DB_USERNAME`: Database username
- `KC_DB_PASSWORD`: Database password

## 📚 Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Angular Keycloak Integration](https://github.com/mauriciovigolo/keycloak-angular)

## 🎯 Production Considerations

### Security Hardening
1. Change default passwords
2. Use HTTPS for all communications
3. Configure proper CORS policies
4. Set up proper certificate management
5. Enable audit logging
6. Configure rate limiting

### Scalability
1. Use external database (not H2)
2. Configure Keycloak clustering
3. Implement proper caching strategies
4. Set up load balancing
5. Monitor token usage and refresh patterns

### Monitoring
1. Set up application metrics
2. Monitor authentication success/failure rates
3. Track token refresh patterns
4. Set up alerting for authentication issues
