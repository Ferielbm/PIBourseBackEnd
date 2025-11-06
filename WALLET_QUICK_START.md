# 🚀 Wallet Module - Quick Start Guide

## Prerequisites

- Java 17+
- MySQL database running
- Application configured and running on port 8084

---

## Step 1: Start the Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or if already built
java -jar target/piboursefin.jar
```

Wait for the message: "Started PiBourseBackEndApplication"

---

## Step 2: Register a New Player

**Endpoint:** `POST http://localhost:8084/api/auth/register`

**Request:**
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "message": "User registered successfully!"
}
```

✅ **A wallet is automatically created with the initial balance!**

---

## Step 3: Login

**Endpoint:** `POST http://localhost:8084/api/auth/login`

**Request:**
```json
{
  "email": "alice@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "role": "ROLE_PLAYER"
}
```

🔑 **Copy the token value for subsequent requests**

---

## Step 4: View Your Wallet

**Endpoint:** `GET http://localhost:8084/api/wallet/my-wallet`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response:**
```json
{
  "id": 1,
  "playerId": 1,
  "playerUsername": "alice",
  "playerEmail": "alice@example.com",
  "balance": 10000.00,
  "currency": "USD",
  "totalDeposits": 10000.00,
  "totalWithdrawals": 0.00,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

💰 **You have $10,000 in your wallet!**

---

## Step 5: Make a Deposit

**Endpoint:** `POST http://localhost:8084/api/wallet/deposit`

**Headers:**
```
Authorization: Bearer {your-token}
Content-Type: application/json
```

**Request:**
```json
{
  "amount": 5000.00,
  "description": "My first deposit"
}
```

**Response:**
```json
{
  "id": 2,
  "walletId": 1,
  "type": "DEPOSIT",
  "amount": 5000.00,
  "balanceBefore": 10000.00,
  "balanceAfter": 15000.00,
  "description": "My first deposit",
  "reference": "TXN-A7B3C9D2",
  "createdAt": "2024-01-15T11:00:00"
}
```

✅ **Balance increased to $15,000!**

---

## Step 6: Make a Withdrawal

**Endpoint:** `POST http://localhost:8084/api/wallet/withdraw`

**Request:**
```json
{
  "amount": 2000.00,
  "description": "Test withdrawal"
}
```

**Response:**
```json
{
  "id": 3,
  "walletId": 1,
  "type": "WITHDRAWAL",
  "amount": 2000.00,
  "balanceBefore": 15000.00,
  "balanceAfter": 13000.00,
  "description": "Test withdrawal",
  "reference": "TXN-B8C4D1E3",
  "createdAt": "2024-01-15T11:15:00"
}
```

✅ **Balance decreased to $13,000!**

---

## Step 7: View Transaction History

**Endpoint:** `GET http://localhost:8084/api/wallet/transactions`

**Response:**
```json
[
  {
    "id": 3,
    "type": "WITHDRAWAL",
    "amount": 2000.00,
    "balanceAfter": 13000.00,
    "description": "Test withdrawal",
    "createdAt": "2024-01-15T11:15:00"
  },
  {
    "id": 2,
    "type": "DEPOSIT",
    "amount": 5000.00,
    "balanceAfter": 15000.00,
    "description": "My first deposit",
    "createdAt": "2024-01-15T11:00:00"
  },
  {
    "id": 1,
    "type": "ADMIN_CREDIT",
    "amount": 10000.00,
    "balanceAfter": 10000.00,
    "description": "Solde initial du compte",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

📊 **Complete audit trail of all transactions!**

---

## Step 8: Transfer to Another Player

First, register and login as a second player, then:

**Endpoint:** `POST http://localhost:8084/api/wallet/transfer`

**Request:**
```json
{
  "recipientPlayerId": 2,
  "amount": 1000.00,
  "description": "Payment for services"
}
```

**Response:**
```json
{
  "message": "Transfert effectué avec succès"
}
```

✅ **$1,000 transferred to player 2!**

---

## Using Swagger UI (Recommended)

### 1. Open Swagger UI
Navigate to: `http://localhost:8084/swagger-ui.html`

### 2. Authenticate
1. Click the **"Authorize"** button (🔓 icon)
2. Enter: `Bearer {your-jwt-token}`
3. Click **"Authorize"**
4. Click **"Close"**

### 3. Test Endpoints
All wallet endpoints are under the **"Wallet"** section:
- Expand any endpoint
- Click **"Try it out"**
- Fill in parameters
- Click **"Execute"**

---

## Quick Test Script (Bash)

Save as `test-wallet.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8084"

# 1. Register
echo "=== Registering new player ==="
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }')
echo $REGISTER_RESPONSE

# 2. Login
echo -e "\n=== Logging in ==="
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }')
echo $LOGIN_RESPONSE

# Extract token
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo -e "\nToken: $TOKEN"

# 3. Get wallet
echo -e "\n=== Getting wallet ==="
curl -s -X GET "$BASE_URL/api/wallet/my-wallet" \
  -H "Authorization: Bearer $TOKEN" | jq

# 4. Deposit
echo -e "\n=== Making deposit ==="
curl -s -X POST "$BASE_URL/api/wallet/deposit" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00,
    "description": "Test deposit"
  }' | jq

# 5. Get balance
echo -e "\n=== Checking balance ==="
curl -s -X GET "$BASE_URL/api/wallet/balance" \
  -H "Authorization: Bearer $TOKEN"

# 6. Get transactions
echo -e "\n=== Getting transactions ==="
curl -s -X GET "$BASE_URL/api/wallet/transactions" \
  -H "Authorization: Bearer $TOKEN" | jq

echo -e "\n=== Test completed ==="
```

Make it executable and run:
```bash
chmod +x test-wallet.sh
./test-wallet.sh
```

---

## Quick Test Script (PowerShell)

Save as `test-wallet.ps1`:

```powershell
$BaseUrl = "http://localhost:8084"

# 1. Register
Write-Host "=== Registering new player ===" -ForegroundColor Cyan
$registerBody = @{
    username = "testuser"
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" `
    -Method Post -Body $registerBody -ContentType "application/json"
Write-Host $registerResponse.message

# 2. Login
Write-Host "`n=== Logging in ===" -ForegroundColor Cyan
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" `
    -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Host "Token obtained: $($token.Substring(0,20))..."

# 3. Get wallet
Write-Host "`n=== Getting wallet ===" -ForegroundColor Cyan
$headers = @{
    "Authorization" = "Bearer $token"
}
$wallet = Invoke-RestMethod -Uri "$BaseUrl/api/wallet/my-wallet" `
    -Method Get -Headers $headers
$wallet | ConvertTo-Json

# 4. Deposit
Write-Host "`n=== Making deposit ===" -ForegroundColor Cyan
$depositBody = @{
    amount = 5000.00
    description = "Test deposit"
} | ConvertTo-Json

$depositResponse = Invoke-RestMethod -Uri "$BaseUrl/api/wallet/deposit" `
    -Method Post -Headers $headers -Body $depositBody -ContentType "application/json"
$depositResponse | ConvertTo-Json

# 5. Get balance
Write-Host "`n=== Checking balance ===" -ForegroundColor Cyan
$balance = Invoke-RestMethod -Uri "$BaseUrl/api/wallet/balance" `
    -Method Get -Headers $headers
Write-Host "Current balance: `$$balance"

# 6. Get transactions
Write-Host "`n=== Getting transactions ===" -ForegroundColor Cyan
$transactions = Invoke-RestMethod -Uri "$BaseUrl/api/wallet/transactions" `
    -Method Get -Headers $headers
$transactions | ConvertTo-Json

Write-Host "`n=== Test completed ===" -ForegroundColor Green
```

Run:
```powershell
.\test-wallet.ps1
```

---

## Configuration Options

Edit `src/main/resources/application.properties`:

```properties
# Change initial balance (default: 10000.00)
wallet.initial.balance=50000.00

# Change default currency (default: USD)
wallet.default.currency=EUR
```

Restart the application for changes to take effect.

---

## Common Issues

### Issue: "Wallet not found"
**Cause:** Wallet wasn't created during registration  
**Solution:** Check logs, manually create wallet via admin endpoint

### Issue: "Insufficient balance"
**Cause:** Trying to withdraw more than available  
**Solution:** Check balance first, deposit more funds

### Issue: 401 Unauthorized
**Cause:** Missing or invalid JWT token  
**Solution:** Login again and use the new token

### Issue: 403 Forbidden
**Cause:** Insufficient permissions  
**Solution:** Ensure you have the correct role (PLAYER or ADMIN)

---

## Next Steps

1. ✅ **Integrate with Trading System**
   - Use wallet for stock purchases
   - Credit wallet from stock sales

2. ✅ **Add Frontend UI**
   - Display wallet balance
   - Transaction history table
   - Deposit/Withdraw forms

3. ✅ **Implement Notifications**
   - Email on large transactions
   - Low balance alerts

4. ✅ **Add Analytics**
   - Spending patterns
   - Monthly reports

---

## Need Help?

- 📖 Read the full documentation: `WALLET_MODULE_DOCUMENTATION.md`
- 🔍 Check Swagger UI: `http://localhost:8084/swagger-ui.html`
- 📝 Review API examples: `API_EXAMPLES_WALLET.http`

---

**Happy Trading! 🚀📈**

