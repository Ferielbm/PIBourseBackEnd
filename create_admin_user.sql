-- ============================================================
-- Script de création d'un utilisateur ADMIN pour les tests
-- ============================================================
-- Base de données : pibourse
-- Date : 5 novembre 2025
-- ============================================================

USE pibourse;

-- ============================================================
-- ÉTAPE 1 : Créer un utilisateur ADMIN
-- ============================================================

-- Mot de passe : "Password123!" (hashé avec BCrypt)
-- Hash BCrypt : $2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6

INSERT INTO players (username, email, password, role) 
VALUES (
    'Admin',
    'admin@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6',
    'ROLE_ADMIN'
);

-- Récupérer l'ID du joueur créé
SET @admin_id = LAST_INSERT_ID();

-- ============================================================
-- ÉTAPE 2 : Créer un wallet pour l'admin
-- ============================================================

INSERT INTO wallets (player_id, balance, currency, created_at, updated_at)
VALUES (
    @admin_id,
    100000.00,
    'USD',
    NOW(),
    NOW()
);

-- ============================================================
-- ÉTAPE 3 : Créer un utilisateur GAME_MASTER
-- ============================================================

INSERT INTO players (username, email, password, role) 
VALUES (
    'GameMaster',
    'gm@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6',
    'ROLE_GAME_MASTER'
);

-- Récupérer l'ID du game master créé
SET @gm_id = LAST_INSERT_ID();

-- Créer un wallet pour le game master
INSERT INTO wallets (player_id, balance, currency, created_at, updated_at)
VALUES (
    @gm_id,
    100000.00,
    'USD',
    NOW(),
    NOW()
);

-- ============================================================
-- ÉTAPE 4 : Créer quelques joueurs de test
-- ============================================================

-- Joueur 1
INSERT INTO players (username, email, password, role) 
VALUES (
    'Player1',
    'player1@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6',
    'ROLE_PLAYER'
);
SET @player1_id = LAST_INSERT_ID();
INSERT INTO wallets (player_id, balance, currency, created_at, updated_at)
VALUES (@player1_id, 10000.00, 'USD', NOW(), NOW());

-- Joueur 2
INSERT INTO players (username, email, password, role) 
VALUES (
    'Player2',
    'player2@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6',
    'ROLE_PLAYER'
);
SET @player2_id = LAST_INSERT_ID();
INSERT INTO wallets (player_id, balance, currency, created_at, updated_at)
VALUES (@player2_id, 10000.00, 'USD', NOW(), NOW());

-- Joueur 3
INSERT INTO players (username, email, password, role) 
VALUES (
    'Player3',
    'player3@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6',
    'ROLE_PLAYER'
);
SET @player3_id = LAST_INSERT_ID();
INSERT INTO wallets (player_id, balance, currency, created_at, updated_at)
VALUES (@player3_id, 10000.00, 'USD', NOW(), NOW());

-- ============================================================
-- ÉTAPE 5 : Vérification
-- ============================================================

SELECT 
    p.id,
    p.username,
    p.email,
    p.role,
    w.balance,
    w.currency
FROM players p
LEFT JOIN wallets w ON p.id = w.player_id
ORDER BY p.id DESC
LIMIT 5;

-- ============================================================
-- RÉSUMÉ DES COMPTES CRÉÉS
-- ============================================================

/*
┌─────────────────────────────────────────────────────────────────┐
│                    COMPTES DE TEST CRÉÉS                        │
├─────────────────────────────────────────────────────────────────┤
│ ADMIN                                                           │
│   Email    : admin@test.com                                     │
│   Password : Password123!                                       │
│   Role     : ROLE_ADMIN                                         │
│   Balance  : 100,000 USD                                        │
│                                                                 │
│ GAME MASTER                                                     │
│   Email    : gm@test.com                                        │
│   Password : Password123!                                       │
│   Role     : ROLE_GAME_MASTER                                   │
│   Balance  : 100,000 USD                                        │
│                                                                 │
│ JOUEUR 1                                                        │
│   Email    : player1@test.com                                   │
│   Password : Password123!                                       │
│   Role     : ROLE_PLAYER                                        │
│   Balance  : 10,000 USD                                         │
│                                                                 │
│ JOUEUR 2                                                        │
│   Email    : player2@test.com                                   │
│   Password : Password123!                                       │
│   Role     : ROLE_PLAYER                                        │
│   Balance  : 10,000 USD                                         │
│                                                                 │
│ JOUEUR 3                                                        │
│   Email    : player3@test.com                                   │
│   Password : Password123!                                       │
│   Role     : ROLE_PLAYER                                        │
│   Balance  : 10,000 USD                                         │
└─────────────────────────────────────────────────────────────────┘
*/

-- ============================================================
-- COMMANDES UTILES
-- ============================================================

-- Voir tous les utilisateurs
-- SELECT id, username, email, role FROM players;

-- Modifier le rôle d'un utilisateur existant
-- UPDATE players SET role = 'ROLE_ADMIN' WHERE email = 'votre@email.com';
-- UPDATE players SET role = 'ROLE_GAME_MASTER' WHERE email = 'votre@email.com';
-- UPDATE players SET role = 'ROLE_PLAYER' WHERE email = 'votre@email.com';

-- Supprimer un utilisateur
-- DELETE FROM players WHERE email = 'email@example.com';

-- Réinitialiser le mot de passe (Password123!)
-- UPDATE players SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye7J954CEha7JYR.g3Kz4Kk8R9G2VVLj6' WHERE email = 'votre@email.com';

-- ============================================================
-- FIN DU SCRIPT
-- ============================================================


