# Digital Wallet API - Swagger Documentation

## Overview

Interactive API documentation using Swagger UI for the Digital Wallet management system.

## Quick Start

### Access Points

- **Swagger UI**: http://localhost:8080/wallet-api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/wallet-api/api-docs
- **OpenAPI YAML**: http://localhost:8080/wallet-api/api-docs.yaml

### Getting Started

1. Start the application: `mvn spring-boot:run`
2. Open Swagger UI: http://localhost:8080/wallet-api/swagger-ui.html
3. Authenticate using test credentials
4. Test API endpoints interactively

## Authentication

### Test Accounts

**Employee** (Full Access):
```json
{
  "username": "employee",
  "password": "emp123"
}
```

**Customer** (Limited Access):
```json
{
  "username": "12345678901",
  "password": "cust123"
}
```

**Admin** (System Access):
```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Authorization Steps

1. Use `POST /api/auth/login` to get JWT token
2. Click "Authorize" button in Swagger UI
3. Enter: `Bearer <your-token>`
4. Click "Authorize"

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login and JWT token generation
- `GET /api/auth/me` - Current user information

### Wallet Management
- `POST /api/wallets/customers/{customerId}` - Create wallet (Employee only)
- `GET /api/wallets/customers/{customerId}` - List customer wallets
- `GET /api/wallets/{walletId}/customers/{customerId}` - Get wallet details
- `POST /api/wallets/deposit` - Deposit money
- `POST /api/wallets/withdraw` - Withdraw money

### Transaction Management
- `GET /api/transactions/wallets/{walletId}` - List transactions
- `GET /api/transactions/{transactionId}` - Get transaction details
- `POST /api/transactions/approve` - Approve/deny transactions (Employee only)

### System Monitoring
- `GET /actuator/health` - Application health
- `GET /actuator/metrics` - System metrics
- `GET /actuator/prometheus` - Prometheus metrics

## User Roles

### CUSTOMER
- Access own wallets and transactions only
- Deposit/withdraw from own wallets
- View basic system information

### EMPLOYEE
- All customer permissions
- Create wallets for any customer
- Approve/deny pending transactions
- Access system metrics

### ADMIN
- All employee permissions
- Access administrative endpoints
- Full system configuration

## Key Features

- **Interactive Testing**: Execute API calls directly from browser
- **Auto-generated Documentation**: Always up-to-date with code
- **Request/Response Examples**: Pre-populated test data
- **Schema Validation**: Real-time parameter validation
- **JWT Integration**: Built-in authentication flow

## Usage Examples

### Create Wallet
```json
POST /api/wallets/customers/1
{
  "walletName": "USD Savings",
  "currency": "USD",
  "activeForShopping": false,
  "activeForWithdraw": true
}
```

### Make Deposit
```json
POST /api/wallets/deposit
{
  "amount": 1500.00,
  "walletId": 1,
  "source": "TR123456789012345678901234"
}
```

### Approve Transaction
```json
POST /api/transactions/approve
{
  "transactionId": 5,
  "status": "APPROVED"
}
```

## Transaction Logic

### Deposits
- **Under 1000**: Auto-approved, immediately available
- **Over 1000**: Pending approval, added to total balance only

### Withdrawals
- **Under 1000**: Auto-approved, immediately deducted
- **Over 1000**: Pending approval, reserved from usable balance

### Destination Types
- **IBAN** (TR123...): Bank transfer (requires activeForWithdraw)
- **PAY** prefix: Shopping payment (requires activeForShopping)

## Monitoring

### Custom Metrics
Available at `/actuator/metrics/{metric-name}`:

- `wallet.count` - Total wallets
- `customer.count` - Total customers
- `transaction.count` - Total transactions
- `wallet.transactions.deposits` - Deposit operations
- `wallet.transactions.withdraws` - Withdraw operations
- `wallet.transactions.approved` - Auto-approved transactions
- `wallet.transactions.pending` - Pending transactions

### Health Checks
- Application health: `/actuator/health`
- Database connectivity included
- Custom wallet health indicators

## Troubleshooting

### Common Issues

**401 Unauthorized**
- Missing or invalid JWT token
- Solution: Re-login and update authorization header
- Format: `Bearer <token>`

**403 Forbidden**
- Insufficient permissions
- Solution: Use appropriate user role
- Note: Customers can only access own resources

**400 Bad Request**
- Invalid request data
- Solution: Check request format against schema
- Verify required fields and data types

**Connection Refused**
- Application not running
- Solution: Start with `mvn spring-boot:run`

## Configuration

Swagger settings can be modified in:
- `SwaggerConfig.java` - Main configuration
- `application.yml` - SpringDoc settings

## Client Generation

Generate API clients using OpenAPI specification:

```bash
curl http://localhost:8080/wallet-api/api-docs > wallet-api.json
npx @openapitools/openapi-generator-cli generate -i wallet-api.json -g javascript -o ./client
```