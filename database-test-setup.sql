-- ========================================
-- Script de création de la base de données de TEST
-- Base : pibourse_test
-- ========================================

-- Supprimer la base si elle existe
DROP DATABASE IF EXISTS pibourse_test;

-- Créer la nouvelle base
CREATE DATABASE pibourse_test 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Utiliser la base
USE pibourse_test;

-- ========================================
-- Les tables seront créées automatiquement par Hibernate
-- Mais voici la structure pour référence
-- ========================================

-- Table des joueurs (players)
CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT chk_role CHECK (role IN ('ROLE_ADMIN', 'ROLE_PLAYER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des wallets
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(19,2) DEFAULT 0.00,
    cash DECIMAL(19,2) DEFAULT 0.00,
    total_assets DECIMAL(19,2) DEFAULT 0.00,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des tokens de réinitialisation de mot de passe
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    player_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_expiry (expiry_date),
    INDEX idx_player (player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========================================
-- Données de test
-- ========================================

-- Mot de passe pour tous : "password123"
-- Hash BCrypt : $2a$10$N9qo8uLOickgx2ZMRZoMye7I73TKF.hSJNCashKZKKNCashKZ (exemple)

-- Admin
INSERT INTO players (username, email, password, role) VALUES 
('admin', 'admin@pibourse.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN');

-- Joueurs de test
INSERT INTO players (username, email, password, role) VALUES 
('player1', 'player1@pibourse.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_PLAYER'),
('player2', 'player2@pibourse.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_PLAYER'),
('testuser', 'test@pibourse.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_PLAYER');

-- Wallets pour les joueurs
INSERT INTO wallets (player_id, balance, cash, total_assets) VALUES 
(1, 10000.00, 10000.00, 10000.00),
(2, 5000.00, 5000.00, 5000.00),
(3, 7500.00, 7500.00, 7500.00),
(4, 1000.00, 1000.00, 1000.00);

-- ========================================
-- Afficher les données créées
-- ========================================
SELECT 'Base de données pibourse_test créée avec succès!' AS message;
SELECT * FROM players;
SELECT * FROM wallets;

-- ========================================
-- NOTES
-- ========================================
-- Pour utiliser cette base de test, modifiez application.properties :
-- spring.datasource.url=jdbc:mysql://localhost:3306/pibourse_test?createDatabaseIfNotExist=true
-- 
-- Ou créez un profil de test dans application-test.properties

