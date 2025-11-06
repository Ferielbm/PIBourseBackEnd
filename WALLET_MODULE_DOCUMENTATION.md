# 🏦 Wallet Module Documentation

## Overview

The **Wallet Module** is a complete financial management system integrated into the PiBourseFin application. It provides each player with a virtual wallet to manage their funds for stock market trading.

## 📋 Table of Contents

1. [Architecture](#architecture)
2. [Database Schema](#database-schema)
3. [Components](#components)
4. [API Endpoints](#api-endpoints)
5. [Configuration](#configuration)
6. [Usage Examples](#usage-examples)
7. [Security](#security)
8. [Transaction Types](#transaction-types)

---

## Architecture

### Package Structure

```
tn.esprit.piboursebackend.Player/
├── Entities/
│   ├── Wallet.java                    # Wallet entity (one-to-one with Player)
│   └── WalletTransaction.java         # Transaction history entity
├── Repositories/
│   ├── WalletRepository.java          # Wallet data access layer
│   └── WalletTransactionRepository.java # Transaction data access layer
├── Services/
│   ├── IWalletService.java            # Service interface
│   └── WalletService.java             # Service implementation
├── Controllers/
│   └── WalletController.java          # REST API endpoints
└── DTOs/
    ├── WalletDTO.java                 # Wallet response DTO
    ├── WalletTransactionDTO.java      # Transaction response DTO
    ├── DepositRequest.java            # Deposit request DTO
    ├── WithdrawRequest.java           # Withdraw request DTO
    └── TransferRequest.java           # Transfer request DTO
```

---

## Database Schema

### Table: `wallets`

| Column              | Type            | Constraints          | Description                    |
|---------------------|-----------------|----------------------|--------------------------------|
| id                  | BIGINT          | PK, AUTO_INCREMENT   | Unique wallet identifier       |
| player_id           | BIGINT          | FK, UNIQUE, NOT NULL | Reference to player            |
| balance             | DECIMAL(19,2)   | NOT NULL             | Current available balance      |
| currency            | VARCHAR(10)     | NOT NULL             | Currency code (USD, EUR, etc.) |
| total_deposits      | DECIMAL(19,2)   | NOT NULL             | Cumulative deposits            |
| total_withdrawals   | DECIMAL(19,2)   | NOT NULL             | Cumulative withdrawals         |
| created_at          | DATETIME        | NOT NULL             | Wallet creation timestamp      |
| updated_at          | DATETIME        | NOT NULL             | Last update timestamp          |

### Table: `wallet_transactions`

| Column         | Type            | Constraints          | Description                      |
|----------------|-----------------|----------------------|----------------------------------|
| id             | BIGINT          | PK, AUTO_INCREMENT   | Unique transaction identifier    |
| wallet_id      | BIGINT          | FK, NOT NULL         | Reference to wallet              |
| type           | VARCHAR(50)     | NOT NULL             | Transaction type (enum)          |
| amount         | DECIMAL(19,2)   | NOT NULL             | Transaction amount               |
| balance_before | DECIMAL(19,2)   | NOT NULL             | Balance before transaction       |
| balance_after  | DECIMAL(19,2)   | NOT NULL             | Balance after transaction        |
| description    | VARCHAR(500)    |                      | Transaction description          |
| reference      | VARCHAR(100)    |                      | Unique transaction reference     |
| created_at     | DATETIME        | NOT NULL             | Transaction timestamp            |

---

## Components

### 1. **Wallet Entity** (`Wallet.java`)

**Key Features:**
- One-to-one relationship with Player
- BigDecimal for precise financial calculations
- Automatic timestamp management with `@PrePersist` and `@PreUpdate`
- Built-in validation methods

**Key Methods:**
```java
void deposit(BigDecimal amount)           // Add funds to wallet
void withdraw(BigDecimal amount)          // Remove funds from wallet
boolean hasSufficientBalance(BigDecimal)  // Check balance availability
```

### 2. **WalletTransaction Entity** (`WalletTransaction.java`)

Tracks all wallet operations with:
- Complete audit trail (before/after balance)
- Unique reference codes
- Multiple transaction types
- Immutable records (no update operations)

### 3. **WalletService** (`WalletService.java`)

**Core Business Logic:**
- Wallet creation with initial balance
- Deposit/withdrawal operations
- Inter-player transfers
- Transaction history management
- Admin operations (credit/debit)

**Configuration:**
```properties
wallet.default.currency=USD
wallet.initial.balance=10000.00
```

### 4. **WalletController** (`WalletController.java`)

**REST API** with role-based access control:
- Player endpoints: manage own wallet
- Admin endpoints: manage all wallets

---

## API Endpoints

### 📊 Player Endpoints

#### Get My Wallet
```http
GET /api/wallet/my-wallet
Authorization: Bearer {jwt_token}

Response:
{
  "id": 1,
  "playerId": 5,
  "playerUsername": "john_doe",
  "playerEmail": "john@example.com",
  "balance": 10000.00,
  "currency": "USD",
  "totalDeposits": 10000.00,
  "totalWithdrawals": 0.00,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Get Balance
```http
GET /api/wallet/balance
Authorization: Bearer {jwt_token}

Response: 10000.00
```

#### Deposit Funds
```http
POST /api/wallet/deposit
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "amount": 5000.00,
  "description": "Monthly deposit"
}

Response:
{
  "id": 12,
  "walletId": 1,
  "type": "DEPOSIT",
  "amount": 5000.00,
  "balanceBefore": 10000.00,
  "balanceAfter": 15000.00,
  "description": "Monthly deposit",
  "reference": "TXN-A7B3C9D2",
  "createdAt": "2024-01-15T11:00:00"
}
```

#### Withdraw Funds
```http
POST /api/wallet/withdraw
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "amount": 2000.00,
  "description": "Cash withdrawal"
}

Response: {WalletTransactionDTO}
```

#### Transfer to Another Player
```http
POST /api/wallet/transfer
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "recipientPlayerId": 7,
  "amount": 1000.00,
  "description": "Payment for services"
}

Response:
{
  "message": "Transfert effectué avec succès"
}
```

#### Check Sufficient Balance
```http
GET /api/wallet/check-balance?amount=5000.00
Authorization: Bearer {jwt_token}

Response: true
```

#### Get Transaction History
```http
GET /api/wallet/transactions
Authorization: Bearer {jwt_token}

Response: [
  {WalletTransactionDTO},
  {WalletTransactionDTO},
  ...
]
```

#### Get Paginated Transactions
```http
GET /api/wallet/transactions/paginated?page=0&size=10
Authorization: Bearer {jwt_token}

Response:
{
  "content": [{WalletTransactionDTO}],
  "totalPages": 5,
  "totalElements": 47,
  "size": 10,
  "number": 0
}
```

#### Get Transactions by Type
```http
GET /api/wallet/transactions/type/DEPOSIT
Authorization: Bearer {jwt_token}

Response: [{WalletTransactionDTO}]
```

#### Get Transactions by Date Range
```http
GET /api/wallet/transactions/date-range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
Authorization: Bearer {jwt_token}

Response: [{WalletTransactionDTO}]
```

---

### 🔐 Admin Endpoints

#### Get Player's Wallet
```http
GET /api/wallet/player/{playerId}
Authorization: Bearer {admin_jwt_token}

Response: {WalletDTO}
```

#### Admin Credit
```http
POST /api/wallet/admin/credit/{playerId}?amount=5000.00&reason=Bonus
Authorization: Bearer {admin_jwt_token}

Response: {WalletTransactionDTO}
```

#### Admin Debit
```http
POST /api/wallet/admin/debit/{playerId}?amount=1000.00&reason=Penalty
Authorization: Bearer {admin_jwt_token}

Response: {WalletTransactionDTO}
```

#### Get Player Transaction History
```http
GET /api/wallet/admin/transactions/{playerId}
Authorization: Bearer {admin_jwt_token}

Response: [{WalletTransactionDTO}]
```

---

## Configuration

### Application Properties

Add to `src/main/resources/application.properties`:

```properties
# Wallet Configuration
wallet.default.currency=USD
wallet.initial.balance=10000.00
```

**Parameters:**
- `wallet.default.currency`: Default currency for new wallets (USD, EUR, TND, etc.)
- `wallet.initial.balance`: Starting balance for new player accounts

---

## Usage Examples

### 1. Automatic Wallet Creation on Registration

When a player registers, a wallet is automatically created:

```java
// In AuthController.registerUser()
Player savedPlayer = playerRepository.save(player);
walletService.createWalletForPlayer(savedPlayer, null, null);
// Uses default configuration values
```

### 2. Manual Wallet Creation

```java
Wallet wallet = walletService.createWalletForPlayer(
    player,
    new BigDecimal("5000.00"),  // custom initial balance
    "EUR"                        // custom currency
);
```

### 3. Checking Balance Before Purchase

```java
if (walletService.hasSufficientBalance(playerId, purchaseAmount)) {
    // Proceed with purchase
    walletService.withdraw(playerId, purchaseAmount, "Stock purchase: AAPL");
} else {
    throw new InsufficientBalanceException();
}
```

### 4. Admin Operations

```java
// Credit a player for winning a contest
walletService.adminCredit(playerId, new BigDecimal("1000.00"), "Contest prize");

// Apply penalty
walletService.adminDebit(playerId, new BigDecimal("100.00"), "Rule violation");
```

---

## Security

### Role-Based Access Control

- **ROLE_PLAYER**: 
  - Can view and manage own wallet
  - Can make deposits, withdrawals, and transfers
  - Can view own transaction history

- **ROLE_ADMIN**:
  - Has all player privileges
  - Can view any player's wallet
  - Can perform administrative credits/debits
  - Can view any player's transaction history

### JWT Authentication

All endpoints require JWT authentication via Bearer token:

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Validation

- Amount must be positive for all operations
- Withdrawal checks for sufficient balance
- Transfer validates both sender and recipient wallets
- Currency consistency enforced

---

## Transaction Types

| Type            | Description                        | Used For                     |
|-----------------|------------------------------------|------------------------------|
| DEPOSIT         | Funds added to wallet              | Player deposits              |
| WITHDRAWAL      | Funds removed from wallet          | Player withdrawals           |
| STOCK_PURCHASE  | Payment for stock purchase         | Trading operations           |
| STOCK_SALE      | Proceeds from stock sale           | Trading operations           |
| ADMIN_CREDIT    | Manual credit by administrator     | Bonuses, corrections         |
| ADMIN_DEBIT     | Manual debit by administrator      | Penalties, corrections       |
| TRANSFER_IN     | Incoming transfer from another user| P2P transfers                |
| TRANSFER_OUT    | Outgoing transfer to another user  | P2P transfers                |
| BONUS           | System-generated bonus             | Rewards, promotions          |
| PENALTY         | System-generated penalty           | Fees, violations             |

---

## Integration with Trading System

### Example: Stock Purchase Flow

```java
@Service
public class OrderService {
    
    @Autowired
    private IWalletService walletService;
    
    public void executeStockPurchase(Long playerId, String stockSymbol, 
                                     int quantity, BigDecimal pricePerShare) {
        BigDecimal totalCost = pricePerShare.multiply(new BigDecimal(quantity));
        
        // Check balance
        if (!walletService.hasSufficientBalance(playerId, totalCost)) {
            throw new InsufficientFundsException();
        }
        
        // Deduct funds
        walletService.withdraw(playerId, totalCost, 
            String.format("Purchase: %d shares of %s @ %s", 
                         quantity, stockSymbol, pricePerShare));
        
        // Execute order...
    }
    
    public void executeStockSale(Long playerId, String stockSymbol, 
                                 int quantity, BigDecimal pricePerShare) {
        BigDecimal totalProceeds = pricePerShare.multiply(new BigDecimal(quantity));
        
        // Credit wallet
        walletService.deposit(playerId, totalProceeds, 
            String.format("Sale: %d shares of %s @ %s", 
                         quantity, stockSymbol, pricePerShare));
        
        // Execute sale...
    }
}
```

---

## Testing

### Swagger UI Testing

1. Start the application
2. Navigate to: `http://localhost:8084/swagger-ui.html`
3. Authenticate using `/api/auth/login`
4. Copy the JWT token
5. Click "Authorize" and paste: `Bearer {token}`
6. Test wallet endpoints

### Sample Test Scenarios

**Scenario 1: New Player Registration**
1. Register via `/api/auth/register`
2. Login via `/api/auth/login`
3. Check wallet via `/api/wallet/my-wallet`
4. Verify initial balance = 10000.00

**Scenario 2: Deposit and Withdrawal**
1. Deposit 5000: `/api/wallet/deposit`
2. Check balance = 15000
3. Withdraw 3000: `/api/wallet/withdraw`
4. Check balance = 12000
5. View transactions: `/api/wallet/transactions`

**Scenario 3: Player-to-Player Transfer**
1. Player A transfers 1000 to Player B
2. Player A balance decreases by 1000
3. Player B balance increases by 1000
4. Both players see transaction in history

---

## Error Handling

### Common Error Responses

**Insufficient Balance:**
```json
{
  "message": "Solde insuffisant ! Solde disponible: 500.00"
}
```

**Invalid Amount:**
```json
{
  "message": "Le montant du dépôt doit être positif"
}
```

**Wallet Not Found:**
```json
{
  "message": "Portefeuille non trouvé pour le joueur ID: 5"
}
```

**Unauthorized:**
```http
HTTP/1.1 401 Unauthorized
{
  "message": "Invalid username or password"
}
```

**Forbidden:**
```http
HTTP/1.1 403 Forbidden
{
  "message": "Access Denied"
}
```

---

## Future Enhancements

### Possible Extensions

1. **Multi-Currency Support**
   - Real-time exchange rates
   - Currency conversion during transfers

2. **Wallet Limits**
   - Daily withdrawal limits
   - Maximum balance caps
   - Transaction frequency limits

3. **Notifications**
   - Email alerts for large transactions
   - Low balance warnings
   - Transaction confirmations

4. **Analytics Dashboard**
   - Spending patterns
   - Income vs expenses
   - Monthly reports

5. **Scheduled Transactions**
   - Recurring deposits
   - Automated savings

6. **Wallet Freeze/Lock**
   - Admin can freeze accounts
   - Temporary locks for security

---

## Troubleshooting

### Issue: Wallet not created on registration

**Solution:**
- Check logs for errors during wallet creation
- Verify database connection
- Ensure `WalletService` is properly autowired
- Check `application.properties` for valid configuration

### Issue: Transaction history not showing

**Solution:**
- Verify wallet ID is correct
- Check database for transaction records
- Ensure proper JOIN in repository queries

### Issue: Insufficient balance error on valid withdrawal

**Solution:**
- Verify balance calculation
- Check for concurrent transactions
- Ensure `@Transactional` is properly configured

---

## Contact & Support

For questions or issues related to the Wallet module:
- Review this documentation
- Check Swagger API documentation
- Examine the code comments
- Review test cases

---

## Changelog

### Version 1.0.0 (Initial Release)
- ✅ Wallet entity with Player one-to-one relationship
- ✅ Transaction history tracking
- ✅ Deposit/Withdrawal operations
- ✅ Player-to-player transfers
- ✅ Admin credit/debit operations
- ✅ REST API with role-based security
- ✅ Automatic wallet creation on registration
- ✅ Configurable initial balance and currency
- ✅ Complete audit trail
- ✅ BigDecimal for financial precision
- ✅ Swagger/OpenAPI documentation

---

**Built with ❤️ for PiBourseFin - The Pedagogical Stock Market Simulator**

