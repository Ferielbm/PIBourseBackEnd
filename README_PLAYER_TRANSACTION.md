# PiBourse - Player & Transaction Management API

## 📋 Description

Backend Spring Boot complet pour la gestion des joueurs (Players) et des transactions dans le système PiBourse. Ce module fournit une API RESTful complète avec documentation Swagger/OpenAPI intégrée.

## 🚀 Fonctionnalités

### ✅ Gestion des Players
- CRUD complet (Create, Read, Update, Delete)
- Recherche par ID ou Email
- Validation des données (email unique, password minimum 6 caractères)
- Gestion des rôles (ROLE_ADMIN, ROLE_PLAYER)

### ✅ Gestion des Transactions
- CRUD complet
- Types de transactions : BUY, SELL, DEPOSIT, WITHDRAW
- Relation OneToMany avec les Players
- Horodatage automatique
- Consultation des transactions par joueur

### ✅ Fonctionnalités Techniques
- **Swagger UI** activé pour tester toutes les routes
- **Validation** avec Bean Validation (Jakarta)
- **Exception Handling** globale avec @ControllerAdvice
- **DTOs** pour séparer la couche API de la couche persistance
- **Mappers** pour conversion Entity ↔ DTO
- **Logging** avec SLF4J/Lombok
- **Transactions** JPA pour l'intégrité des données
- **Données de test** initialisées automatiquement

## 🏗️ Architecture

```
src/main/java/tn/esprit/piboursebackend/Player/
├── Config/
│   ├── SwaggerConfig.java          # Configuration OpenAPI/Swagger
│   └── DataInitializer.java        # Données de test initiales
├── Controllers/
│   ├── PlayerController.java       # REST endpoints pour Players
│   └── TransactionController.java  # REST endpoints pour Transactions
├── DTOs/
│   ├── PlayerDTO.java              # DTO de lecture Player
│   ├── PlayerCreateDTO.java        # DTO de création Player
│   ├── PlayerUpdateDTO.java        # DTO de mise à jour Player
│   ├── TransactionDTO.java         # DTO de lecture Transaction
│   └── TransactionCreateDTO.java   # DTO de création Transaction
├── Entities/
│   ├── Player.java                 # Entité JPA Player
│   ├── Transaction.java            # Entité JPA Transaction
│   ├── Role.java                   # Enum des rôles
│   └── TransactionType.java        # Enum des types de transaction
├── Exceptions/
│   ├── ResourceNotFoundException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java # Gestion centralisée des erreurs
├── Mappers/
│   ├── PlayerMapper.java           # Conversion Player ↔ DTO
│   └── TransactionMapper.java      # Conversion Transaction ↔ DTO
├── Repositories/
│   ├── PlayerRepository.java       # Repository JPA Player
│   └── TransactionRepository.java  # Repository JPA Transaction
└── Services/
    ├── IPlayerService.java         # Interface Player Service
    ├── PlayerService.java          # Implémentation Player Service
    ├── ITransactionService.java    # Interface Transaction Service
    └── TransactionService.java     # Implémentation Transaction Service
```

## 📦 Dépendances

```xml
<!-- Spring Boot -->
- spring-boot-starter-data-jpa
- spring-boot-starter-web
- spring-boot-starter-validation

<!-- Database -->
- mysql-connector-j

<!-- Documentation -->
- springdoc-openapi-starter-webmvc-ui (2.1.0)

<!-- Outils -->
- lombok
```

## ⚙️ Configuration

### Base de données (application.properties)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pibourse?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
server.port=8084
```

### Swagger UI

Accessible sur : **http://localhost:8084/swagger-ui.html**

API Docs JSON : **http://localhost:8084/api-docs**

## 🚀 Démarrage

### Prérequis
- Java 17 ou supérieur
- MySQL 8.0+
- Maven 3.6+

### Étapes

1. **Cloner le projet**
```bash
cd PiBourse
```

2. **Configurer MySQL**
- Démarrer MySQL sur le port 3306
- La base de données `pibourse` sera créée automatiquement

3. **Lancer l'application**
```bash
# Avec Maven
./mvnw spring-boot:run

# Ou avec votre IDE
# Run PiBourseBackEndApplication.java
```

4. **Accéder à Swagger UI**
```
http://localhost:8084/swagger-ui.html
```

## 📊 Données de Test

L'application initialise automatiquement des données de test au démarrage :

### Players créés
| Username | Email | Password | Role |
|----------|-------|----------|------|
| admin | admin@pibourse.tn | admin123 | ROLE_ADMIN |
| john_trader | john@example.com | password123 | ROLE_PLAYER |
| sarah_investor | sarah@example.com | password123 | ROLE_PLAYER |
| mike_stocks | mike@example.com | password123 | ROLE_PLAYER |

### Transactions créées
- 10 transactions réparties entre les joueurs
- Types variés : DEPOSIT, BUY, SELL, WITHDRAW
- Montants entre 2000€ et 20000€

## 📡 Endpoints API

### 🎮 Player Management (`/api/players`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/players` | Liste tous les joueurs |
| GET | `/api/players/{id}` | Récupère un joueur par ID |
| GET | `/api/players/email/{email}` | Récupère un joueur par email |
| POST | `/api/players` | Crée un nouveau joueur |
| PUT | `/api/players/{id}` | Met à jour un joueur |
| DELETE | `/api/players/{id}` | Supprime un joueur |

### 💰 Transaction Management (`/api/transactions`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/transactions` | Liste toutes les transactions |
| GET | `/api/transactions/{id}` | Récupère une transaction par ID |
| GET | `/api/transactions/player/{playerId}` | Transactions d'un joueur |
| POST | `/api/transactions` | Crée une nouvelle transaction |
| DELETE | `/api/transactions/{id}` | Supprime une transaction |

## 📝 Exemples d'utilisation

### Créer un joueur

**POST** `/api/players`

```json
{
  "username": "new_trader",
  "email": "trader@example.com",
  "password": "securepass123",
  "role": "ROLE_PLAYER"
}
```

**Réponse (201 Created)**
```json
{
  "id": 5,
  "username": "new_trader",
  "email": "trader@example.com",
  "role": "ROLE_PLAYER",
  "transactions": null
}
```

### Créer une transaction

**POST** `/api/transactions`

```json
{
  "type": "DEPOSIT",
  "amount": 5000.0,
  "playerId": 1
}
```

**Réponse (201 Created)**
```json
{
  "id": 11,
  "type": "DEPOSIT",
  "amount": 5000.0,
  "createdAt": "2025-10-21T14:30:00",
  "playerId": 1,
  "playerUsername": "john_trader"
}
```

### Mettre à jour un joueur

**PUT** `/api/players/1`

```json
{
  "username": "john_pro_trader",
  "role": "ROLE_ADMIN"
}
```

### Récupérer les transactions d'un joueur

**GET** `/api/transactions/player/1`

```json
[
  {
    "id": 1,
    "type": "DEPOSIT",
    "amount": 10000.0,
    "createdAt": "2025-10-21T10:00:00",
    "playerId": 1,
    "playerUsername": "john_trader"
  },
  {
    "id": 2,
    "type": "BUY",
    "amount": 2500.0,
    "createdAt": "2025-10-21T11:15:00",
    "playerId": 1,
    "playerUsername": "john_trader"
  }
]
```

## 🛡️ Validations

### Player
- **username** : 3-50 caractères, obligatoire
- **email** : format email valide, unique, obligatoire
- **password** : minimum 6 caractères, obligatoire
- **role** : ROLE_ADMIN ou ROLE_PLAYER, obligatoire

### Transaction
- **type** : BUY, SELL, DEPOSIT, ou WITHDRAW, obligatoire
- **amount** : nombre positif, obligatoire
- **playerId** : doit correspondre à un joueur existant, obligatoire

## ⚠️ Gestion des erreurs

### Erreur de validation (400 Bad Request)
```json
{
  "timestamp": "2025-10-21T14:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/players",
  "validationErrors": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  }
}
```

### Ressource non trouvée (404 Not Found)
```json
{
  "timestamp": "2025-10-21T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Player not found with id : '999'",
  "path": "/api/players/999"
}
```

## 🔧 Tests avec Swagger

1. Ouvrir **http://localhost:8084/swagger-ui.html**
2. Explorer les sections :
   - **Player Management** : Tous les endpoints players
   - **Transaction Management** : Tous les endpoints transactions
3. Cliquer sur un endpoint
4. Cliquer sur **"Try it out"**
5. Remplir les paramètres/body
6. Cliquer sur **"Execute"**
7. Voir la réponse et le code HTTP

## 📈 Améliorations futures possibles

- [ ] Authentification JWT
- [ ] Pagination pour les listes
- [ ] Filtrage et recherche avancée
- [ ] Export des transactions (CSV, PDF)
- [ ] Statistiques et rapports
- [ ] Websockets pour notifications temps réel
- [ ] Cache Redis
- [ ] Tests unitaires et d'intégration
- [ ] Docker containerization
- [ ] CI/CD pipeline

## 👥 Auteurs

**PiBourse Team** - ESPRIT

## 📄 Licence

Ce projet est sous licence Apache 2.0

---

## 🎯 Quick Start avec Swagger

**Une fois l'application lancée :**

1. Aller sur http://localhost:8084/swagger-ui.html
2. Tester GET `/api/players` pour voir les joueurs de test
3. Tester GET `/api/transactions` pour voir les transactions
4. Créer un nouveau joueur avec POST `/api/players`
5. Créer une transaction pour ce joueur avec POST `/api/transactions`
6. Explorer toutes les fonctionnalités CRUD !

**Bon développement ! 🚀**

