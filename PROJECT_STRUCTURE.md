# Structure du Projet PiBourse - Player & Transaction Management

## 📁 Arborescence Complète

```
PiBourse/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tn/esprit/piboursebackend/
│   │   │       │
│   │   │       ├── Player/                          # Module Player & Transaction
│   │   │       │   │
│   │   │       │   ├── Config/                      # Configuration
│   │   │       │   │   ├── SwaggerConfig.java       # Configuration OpenAPI/Swagger
│   │   │       │   │   └── DataInitializer.java     # Données de test (CommandLineRunner)
│   │   │       │   │
│   │   │       │   ├── Controllers/                 # Couche REST API
│   │   │       │   │   ├── PlayerController.java    # Endpoints Player CRUD
│   │   │       │   │   └── TransactionController.java # Endpoints Transaction CRUD
│   │   │       │   │
│   │   │       │   ├── DTOs/                        # Data Transfer Objects
│   │   │       │   │   ├── PlayerDTO.java           # DTO lecture Player (avec transactions)
│   │   │       │   │   ├── PlayerCreateDTO.java     # DTO création Player (avec validations)
│   │   │       │   │   ├── PlayerUpdateDTO.java     # DTO mise à jour Player (champs optionnels)
│   │   │       │   │   ├── TransactionDTO.java      # DTO lecture Transaction
│   │   │       │   │   └── TransactionCreateDTO.java # DTO création Transaction (avec validations)
│   │   │       │   │
│   │   │       │   ├── Entities/                    # Entités JPA
│   │   │       │   │   ├── Player.java              # Entité Player (avec Lombok)
│   │   │       │   │   ├── Transaction.java         # Entité Transaction (avec Lombok)
│   │   │       │   │   ├── Role.java                # Enum: ROLE_ADMIN, ROLE_PLAYER
│   │   │       │   │   └── TransactionType.java     # Enum: BUY, SELL, DEPOSIT, WITHDRAW
│   │   │       │   │
│   │   │       │   ├── Exceptions/                  # Gestion des exceptions
│   │   │       │   │   ├── ResourceNotFoundException.java  # Exception ressource non trouvée
│   │   │       │   │   ├── ErrorResponse.java       # Format de réponse d'erreur
│   │   │       │   │   └── GlobalExceptionHandler.java # @ControllerAdvice
│   │   │       │   │
│   │   │       │   ├── Mappers/                     # Conversion Entity ↔ DTO
│   │   │       │   │   ├── PlayerMapper.java        # Player <-> PlayerDTO
│   │   │       │   │   └── TransactionMapper.java   # Transaction <-> TransactionDTO
│   │   │       │   │
│   │   │       │   ├── Repositories/                # Couche Data Access
│   │   │       │   │   ├── PlayerRepository.java    # JpaRepository<Player, Long>
│   │   │       │   │   └── TransactionRepository.java # JpaRepository<Transaction, Long>
│   │   │       │   │
│   │   │       │   └── Services/                    # Couche Business Logic
│   │   │       │       ├── IPlayerService.java      # Interface Player Service
│   │   │       │       ├── PlayerService.java       # Implémentation avec @Transactional
│   │   │       │       ├── ITransactionService.java # Interface Transaction Service
│   │   │       │       └── TransactionService.java  # Implémentation avec @Transactional
│   │   │       │
│   │   │       ├── PiBourseBackEndApplication.java  # Main Application
│   │   │       │
│   │   │       └── [Autres modules: Credit, Marche, Order, Portfolio]
│   │   │
│   │   └── resources/
│   │       ├── application.properties               # Configuration Spring Boot
│   │       ├── static/                              # Ressources statiques
│   │       └── templates/                           # Templates (si nécessaire)
│   │
│   └── test/
│       └── java/
│           └── tn/esprit/piboursebackend/
│               └── PiBourseBackEndApplicationTests.java
│
├── target/                                          # Fichiers compilés
│   ├── classes/
│   └── generated-sources/
│
├── pom.xml                                          # Configuration Maven
├── mvnw                                             # Maven Wrapper (Unix)
├── mvnw.cmd                                         # Maven Wrapper (Windows)
├── HELP.md                                          # Documentation Spring Boot
├── README_PLAYER_TRANSACTION.md                     # Documentation complète du module
├── API_EXAMPLES.http                                # Exemples de requêtes HTTP
└── PROJECT_STRUCTURE.md                             # Ce fichier
```

## 🎯 Responsabilités par Couche

### 1️⃣ **Entities** (Modèle de données)
- Définition des tables de base de données
- Relations JPA (OneToMany, ManyToOne)
- Validations au niveau de la persistance
- Annotations Lombok pour réduire le boilerplate

**Fichiers :**
- `Player.java` : Informations joueur, relation avec transactions
- `Transaction.java` : Détails transaction, référence au joueur
- `Role.java` : Énumération des rôles utilisateur
- `TransactionType.java` : Énumération des types de transaction

### 2️⃣ **DTOs** (Transfert de données)
- Séparation de la couche API et de la persistance
- Validations spécifiques à l'API (Jakarta Validation)
- Évite les références circulaires JSON
- Contrôle des données exposées

**Fichiers :**
- `PlayerDTO.java` : Réponse complète avec transactions
- `PlayerCreateDTO.java` : Création avec validations strictes
- `PlayerUpdateDTO.java` : Mise à jour avec champs optionnels
- `TransactionDTO.java` : Réponse avec infos joueur
- `TransactionCreateDTO.java` : Création avec validations

### 3️⃣ **Mappers** (Conversion)
- Transformation Entity → DTO (pour les réponses)
- Transformation DTO → Entity (pour les créations)
- Logique de conversion centralisée
- Méthodes statiques utilitaires

**Fichiers :**
- `PlayerMapper.java` : Conversions Player
- `TransactionMapper.java` : Conversions Transaction

### 4️⃣ **Repositories** (Accès données)
- Interface Spring Data JPA
- Méthodes CRUD automatiques
- Requêtes personnalisées (derived queries)
- Gestion de la persistance

**Fichiers :**
- `PlayerRepository.java` : findByEmail, existsByEmail
- `TransactionRepository.java` : findByPlayerId

### 5️⃣ **Services** (Logique métier)
- Logique applicative
- Gestion des transactions (@Transactional)
- Validation métier
- Logging des opérations
- Gestion des erreurs

**Fichiers :**
- `IPlayerService.java` : Contrat du service
- `PlayerService.java` : Implémentation CRUD complète
- `ITransactionService.java` : Contrat du service
- `TransactionService.java` : Implémentation CRUD complète

### 6️⃣ **Controllers** (API REST)
- Endpoints HTTP
- Documentation Swagger (@Operation, @ApiResponses)
- Validation des entrées (@Valid)
- Codes de statut HTTP appropriés
- CORS activé

**Fichiers :**
- `PlayerController.java` : 6 endpoints Player
- `TransactionController.java` : 5 endpoints Transaction

### 7️⃣ **Exceptions** (Gestion erreurs)
- Exceptions personnalisées
- Gestion centralisée (@ControllerAdvice)
- Réponses d'erreur standardisées
- Messages clairs pour le client

**Fichiers :**
- `ResourceNotFoundException.java` : Ressource non trouvée
- `ErrorResponse.java` : Format de réponse d'erreur
- `GlobalExceptionHandler.java` : Gestion globale

### 8️⃣ **Config** (Configuration)
- Configuration Swagger/OpenAPI
- Initialisation des données
- Beans de configuration

**Fichiers :**
- `SwaggerConfig.java` : Configuration OpenAPI
- `DataInitializer.java` : Données de test (4 players, 10 transactions)

## 📊 Diagramme de Flux

```
Client HTTP Request
       ↓
[Controller] ← @RestController, @RequestMapping
       ↓ @Valid
   [DTOs] ← Validation
       ↓
   [Service] ← @Transactional, Business Logic
       ↓
   [Mapper] ← Entity ↔ DTO conversion
       ↓
[Repository] ← JPA, Database queries
       ↓
   [Database] ← MySQL
       ↑
   [Response]
       ↑
   [Mapper] ← DTO conversion
       ↑
   [Service]
       ↑
  [Controller]
       ↑
Client HTTP Response (JSON)
```

## 🔄 Flux de Données Exemple

### Création d'un Player

```
1. POST /api/players
   Body: PlayerCreateDTO
   
2. PlayerController.createPlayer()
   - Validation @Valid
   
3. PlayerService.createPlayer()
   - Vérification email unique
   - PlayerMapper.toEntity() → Player
   - playerRepository.save()
   - PlayerMapper.toDTO() → PlayerDTO
   
4. Response 201 Created
   Body: PlayerDTO
```

### Création d'une Transaction

```
1. POST /api/transactions
   Body: TransactionCreateDTO
   
2. TransactionController.createTransaction()
   - Validation @Valid
   
3. TransactionService.createTransaction()
   - Vérification Player existe
   - TransactionMapper.toEntity() → Transaction
   - Association avec Player
   - transactionRepository.save()
   - TransactionMapper.toDTO() → TransactionDTO
   
4. Response 201 Created
   Body: TransactionDTO
```

## 🗄️ Modèle de Données

### Table `players`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| username | VARCHAR(50) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |
| role | VARCHAR(20) | NOT NULL (ENUM) |

### Table `transactions`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| type | VARCHAR(20) | NOT NULL (ENUM) |
| amount | DOUBLE | NOT NULL, > 0 |
| created_at | DATETIME | NOT NULL, AUTO |
| player_id | BIGINT | FK → players(id), NOT NULL |

### Relation

```
Player 1 ──────── * Transaction
       ↑           ↑
    @OneToMany  @ManyToOne
    (cascade)   (fetch=LAZY)
```

## 🔐 Validations

### PlayerCreateDTO
- username: 3-50 caractères, @NotBlank
- email: Format email, @Email, @NotBlank
- password: Min 6 caractères, @NotBlank, @Size
- role: @NotNull

### TransactionCreateDTO
- type: @NotNull (BUY|SELL|DEPOSIT|WITHDRAW)
- amount: @NotNull, @Positive
- playerId: @NotNull, doit exister

## 📝 Annotations Clés Utilisées

### Spring
- `@RestController` : Contrôleur REST
- `@RequestMapping` : Mapping d'URL
- `@GetMapping`, `@PostMapping`, etc. : Méthodes HTTP
- `@PathVariable` : Paramètre d'URL
- `@RequestBody` : Corps de requête
- `@Valid` : Validation Bean

### JPA/Hibernate
- `@Entity` : Entité JPA
- `@Table` : Nom de table
- `@Id`, `@GeneratedValue` : Clé primaire
- `@Column` : Configuration colonne
- `@OneToMany`, `@ManyToOne` : Relations
- `@Enumerated` : Énumération
- `@CreationTimestamp` : Timestamp auto

### Validation
- `@NotBlank` : Non vide
- `@NotNull` : Non null
- `@Email` : Format email
- `@Size` : Taille min/max
- `@Positive` : Nombre positif

### Lombok
- `@Data` : Getters/Setters/toString/equals/hashCode
- `@Builder` : Pattern Builder
- `@NoArgsConstructor`, `@AllArgsConstructor` : Constructeurs
- `@RequiredArgsConstructor` : Constructeur avec final fields
- `@Slf4j` : Logger

### Swagger/OpenAPI
- `@Tag` : Catégorie API
- `@Operation` : Description endpoint
- `@ApiResponses`, `@ApiResponse` : Réponses possibles
- `@Parameter` : Description paramètre
- `@Schema` : Schéma de données

### Exception Handling
- `@ControllerAdvice` : Gestion globale
- `@ExceptionHandler` : Handler exception spécifique
- `@RestControllerAdvice` : @ControllerAdvice + @ResponseBody

### Transaction
- `@Transactional` : Gestion transaction
- `@Transactional(readOnly = true)` : Lecture seule

## 🚀 Points Forts de cette Architecture

✅ **Séparation des préoccupations** : Chaque couche a une responsabilité claire

✅ **Testabilité** : Services avec interfaces, facile à mocker

✅ **Maintenabilité** : Code organisé, facile à comprendre et modifier

✅ **Scalabilité** : Architecture modulaire, facile à étendre

✅ **Sécurité** : Validations multiples, DTOs pour contrôle des données

✅ **Documentation** : Swagger auto-généré, code auto-documenté

✅ **Performance** : Transactions optimisées, Lazy loading

✅ **Robustesse** : Gestion d'erreurs complète, validations strictes

## 📚 Ressources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Swagger/OpenAPI: https://springdoc.org/
- Bean Validation: https://beanvalidation.org/
- Lombok: https://projectlombok.org/

---

**Architecture créée pour PiBourse - Module Player & Transaction Management**

