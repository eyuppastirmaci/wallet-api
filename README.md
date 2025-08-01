# Digital Wallet API

A robust REST API for managing digital wallets, enabling customers and employees to create wallets, perform deposits/withdrawals, and manage transactions with multi-currency support.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Docker Support](#docker-support)
- [Contributing](#contributing)

## Overview

This Digital Wallet API provides a complete backend solution for a digital payment company, allowing:
- **Customers** to manage their own wallets and transactions
- **Employees** to manage all customer wallets and approve/deny pending transactions

The system supports multi-currency wallets (TRY, USD, EUR) with configurable permissions for shopping and withdrawal operations.

## Features

- **Multi-Currency Support**: Create wallets in TRY, USD, EUR
- **Role-Based Access Control**: Different permissions for customers and employees
- **Transaction Management**: Automatic approval for small amounts, manual approval for large transactions
- **Wallet Configuration**: Enable/disable wallets for shopping or withdrawals
- **Security**: JWT-based authentication and authorization
- **Monitoring**: Spring Boot Actuator with custom health indicators and metrics
- **API Documentation**: Interactive Swagger UI for testing endpoints
- **Database**: H2 in-memory database with pre-loaded sample data

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with H2 Database
- **MapStruct** for DTO mapping
- **Lombok** for boilerplate reduction
- **Swagger/OpenAPI** for API documentation
- **Spring Boot Actuator** with Prometheus metrics
- **Maven** for dependency management
- **Docker** support included

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Docker (optional)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd walletapi
   ```

2. **Build the project**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   Or using Java directly:
   ```bash
   java -jar target/walletapi-0.0.1-SNAPSHOT.jar
   ```

4. **Access the application**
   - API Base URL: http://localhost:8080/wallet-api
   - Swagger UI: http://localhost:8080/wallet-api/swagger-ui.html
   - H2 Console: http://localhost:8080/wallet-api/h2-console

### Using Docker

```bash
# Build and run with Docker Compose
docker-compose up -d

# Or build manually
docker build -t digital-wallet-api .
docker run -p 8080:8080 digital-wallet-api
```

## API Documentation

### Authentication

All API endpoints (except `/api/auth/login`) require JWT authentication.

1. **Login** to get JWT token:
   ```bash
   POST /wallet-api/api/auth/login
   {
     "username": "employee",
     "password": "emp123"
   }
   ```

2. **Include token** in subsequent requests:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

### Test Credentials

| Role | Username | Password | Description |
|------|----------|----------|-------------|
| Employee | employee | emp123 | Can manage all wallets |
| Admin | admin | admin123 | Full system access |
| Customer | 12345678901 | cust123 | Can manage own wallets only |

### Core Endpoints

#### 1. Create Wallet
```http
POST /api/wallets/customers/{customerId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "walletName": "My USD Wallet",
  "currency": "USD",
  "activeForShopping": true,
  "activeForWithdraw": true
}
```
**Access**: Employees only

#### 2. List Wallets
```http
GET /api/wallets/customers/{customerId}?currency=TRY
Authorization: Bearer <token>
```
**Access**: Employees (any customer) or Customers (own wallets)

#### 3. Deposit Money
```http
POST /api/wallets/deposit
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 500.00,
  "walletId": 1,
  "source": "TR123456789012345678901234"
}
```
- Amounts ≤ 1000: Auto-approved
- Amounts > 1000: Pending approval

#### 4. Withdraw Money
```http
POST /api/wallets/withdraw
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 250.00,
  "walletId": 1,
  "destination": "PAY12345"
}
```
- Destination starting with "PAY": Shopping (requires `activeForShopping`)
- IBAN format: Bank transfer (requires `activeForWithdraw`)

#### 5. List Transactions
```http
GET /api/transactions/wallets/{walletId}
Authorization: Bearer <token>
```

#### 6. Approve/Deny Transaction
```http
POST /api/transactions/approve
Authorization: Bearer <token>
Content-Type: application/json

{
  "transactionId": 1,
  "status": "APPROVED"
}
```
**Access**: Employees only

### Transaction Logic

#### Balance Management
- **Deposits**:
  - APPROVED: Updates both `balance` and `usableBalance`
  - PENDING: Updates only `balance`
  
- **Withdrawals**:
  - APPROVED: Deducts from both `balance` and `usableBalance`
  - PENDING: Deducts only from `usableBalance`

#### Approval Rules
- Transactions ≤ 1000: Automatically approved
- Transactions > 1000: Requires manual approval by employee

## Security

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Method-level security with `@PreAuthorize`

### Access Rules
- **Customers**: Can only access their own wallets and transactions
- **Employees**: Can access all customer data and approve transactions
- **Public**: Can access health checks and API documentation

## 🧪 Testing

### Unit Tests
Run unit tests:
```bash
./mvnw test
```

The project includes comprehensive unit tests for:
- Service layer business logic
- Transaction approval/denial flows
- Wallet balance calculations
- Authorization rules

### Integration Testing with Postman

Import the provided Postman collection:
```
docs/postman/Wallet-API-Complete.postman_collection.json
docs/postman/Wallet-API.postman_environment.json
```

Test execution order:
1. Health checks
2. Authentication (login)
3. Wallet operations
4. Transaction operations
5. Approval workflows

### Manual Testing with Swagger

1. Navigate to http://localhost:8080/wallet-api/swagger-ui.html
2. Click "Authorize" and enter JWT token
3. Test endpoints interactively

## Monitoring

### Health Checks
```http
GET /actuator/health
```
Returns application health with custom wallet indicators.

### Metrics
```http
GET /actuator/metrics
GET /actuator/prometheus
```

Custom business metrics:
- `wallet.count` - Total wallets
- `customer.count` - Total customers
- `transaction.count` - Total transactions
- `wallet.transactions.deposits` - Deposit operations
- `wallet.transactions.withdraws` - Withdrawal operations
- `wallet.transactions.approved` - Auto-approved transactions
- `wallet.transactions.pending` - Pending transactions

### Configuration
Environment variables for Docker:
- `SPRING_PROFILES_ACTIVE=docker`
- `WALLET_APP_JWT_SECRET` - JWT signing key
- `WALLET_TRANSACTION_PENDING_THRESHOLD` - Auto-approval threshold

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---