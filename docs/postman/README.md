# Wallet API Documentation

## Testing with Postman

### Setup

1. **Import Collection**
   ```
   Import: docs/postman/Wallet-API-Complete.postman_collection.json
   ```

2. **Import Environment**
   ```
   Import: docs/postman/Wallet-API.postman_environment.json
   ```

3. **Select Environment**
    - In Postman, select "Wallet API Environment" from the environment dropdown

### Test Execution Order

#### Phase 1: Application Health
1. **Actuator → Health Check** ✅ (Public)
2. **Actuator → Application Info** ✅ (Public)
3. **Actuator → Metrics List** ✅ (Public)

#### Phase 2: Authentication
1. **Auth → Login Admin** (Saves admin_token)
2. **Auth → Login Employee** (Saves employee_token)
3. **Auth → Login Customer** (Saves customer_token)
4. **Auth → Get Current User**

#### Phase 3: Basic Wallet Operations
1. **Wallet → List Wallet Customer**
2. **Wallet → Create Wallet** (Employee only)
3. **Wallet → Get Specific Wallet**

#### Phase 4: Transaction Operations
1. **Wallet → Make Deposit (Small - Approved)**
2. **Wallet → Make Deposit (Large - Pending)**
3. **Wallet → Make Withdrawal (Small - Approved)**
4. **Transactions → List Transactions for Wallet**
5. **Transactions → Approve Transaction** (Employee only)

#### Phase 5: Custom Metrics Verification
1. **Custom Metrics → Wallet Count**
2. **Custom Metrics → Customer Count**
3. **Custom Metrics → Transaction Count**
4. **Custom Metrics → Deposit Transactions Counter**
5. **Custom Metrics → Withdraw Transactions Counter**
6. **Custom Metrics → Approved Transactions Counter**
7. **Custom Metrics → Pending Transactions Counter**

#### Phase 6: System Monitoring
1. **JVM and System Metrics → JVM Memory Used**
2. **JVM and System Metrics → System CPU Usage**
3. **JVM and System Metrics → HTTP Server Requests**
4. **Actuator → Prometheus Format**

#### Phase 7: Employee-Only Endpoints
1. **Actuator → Environment Variables** (Employee token required)
2. **Actuator → Configuration Properties** (Employee token required)
3. **Actuator → Spring Beans** (Employee token required)
4. **Actuator → Request Mappings** (Employee token required)
5. **Actuator → Thread Dump** (Employee token required)

#### Phase 8: Security Testing
1. **Security Tests → Test Unauthorized Access** (Should return 401)
2. **Security Tests → Test Customer Access to Employee Endpoint** (Should return 403)
3. **Security Tests → Test Customer Access to Own Wallets** (Should return 200)
4. **Security Tests → Test Customer Access to Another Customer's Wallets** (Should return 403)

#### Phase 9: Error Scenarios
1. **Error Scenarios → Insufficient Balance Withdrawal** (Should return 400)
2. **Error Scenarios → Invalid Transaction ID** (Should return 400)
3. **Error Scenarios → Invalid Wallet ID** (Should return 400)
4. **Error Scenarios → Validation Error - Negative Amount** (Should return 400)

### Expected Results

#### ✅ Success Cases
- **Health Check**: Status "UP" with wallet component details
- **Custom Metrics**: Numeric values for wallet, customer, transaction counts
- **Authentication**: JWT tokens saved automatically
- **Wallet Operations**: Successful deposit/withdraw with balance updates
- **Employee Access**: Full access to all endpoints and admin functions

#### ❌ Security Cases
- **401 Unauthorized**: No token provided for protected endpoints
- **403 Forbidden**: Customer trying to access employee endpoints
- **403 Forbidden**: Customer trying to access other customer's data

#### ⚠️ Error Cases
- **400 Bad Request**: Invalid data, insufficient balance, not found errors
- **Validation Errors**: Negative amounts, missing required fields

### Custom Metrics Explanation

| Metric | Description | Type |
|--------|-------------|------|
| `wallet.count` | Total wallets in system | Gauge |
| `customer.count` | Total customers in system | Gauge |
| `transaction.count` | Total transactions in system | Gauge |
| `wallet.transactions.deposits` | Deposit operations performed | Counter |
| `wallet.transactions.withdraws` | Withdraw operations performed | Counter |
| `wallet.transactions.approved` | Auto-approved transactions | Counter |
| `wallet.transactions.pending` | Pending approval transactions | Counter |

### Actuator Endpoints Summary

| Endpoint | Access Level | Description |
|----------|-------------|-------------|
| `/actuator/health` | Public | Application health status |
| `/actuator/info` | Public | Application information |
| `/actuator/metrics` | Public | Metrics list |
| `/actuator/prometheus` | Public | Prometheus format metrics |
| `/actuator/env` | Employee | Environment variables |
| `/actuator/configprops` | Employee | Configuration properties |
| `/actuator/beans` | Employee | Spring beans |
| `/actuator/mappings` | Employee | Request mappings |
| `/actuator/threaddump` | Employee | Thread dump |
| `/actuator/heapdump` | Employee | Heap dump |

### Troubleshooting

#### Common Issues

1. **401 Unauthorized**
    - Run authentication requests first
    - Check if tokens are saved in environment

2. **403 Forbidden**
    - Verify user role permissions
    - Use correct token for endpoint access level

3. **404 Not Found**
    - Verify application is running on http://localhost:8080
    - Check context path: `/wallet-api`

4. **Metric Values are 0**
    - Run some wallet operations first
    - Check if custom metrics are properly registered

#### Debug Commands

```bash
# Check if application is running
curl http://localhost:8080/wallet-api/actuator/health

# Check available metrics
curl http://localhost:8080/wallet-api/actuator/metrics

# Check specific custom metric
curl http://localhost:8080/wallet-api/actuator/metrics/wallet.count
```