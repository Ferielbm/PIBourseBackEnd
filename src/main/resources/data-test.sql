-- ========================================
-- Script d'initialisation base de données TEST
-- ========================================

-- Supprimer les données existantes
DELETE FROM password_reset_tokens;
DELETE FROM players;

-- Insérer des utilisateurs de test
-- Mot de passe : "password123" (encodé en BCrypt)
INSERT INTO players (id, username, email, password, role) VALUES
(1, 'admin', 'admin@pibourse.tn', '$2a$10$QqZ8Z9Z9Z9Z9Z9Z9Z9Z9ZuJxE0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y', 'ROLE_ADMIN'),
(2, 'player1', 'player1@pibourse.tn', '$2a$10$QqZ8Z9Z9Z9Z9Z9Z9Z9Z9ZuJxE0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y', 'ROLE_PLAYER'),
(3, 'player2', 'player2@pibourse.tn', '$2a$10$QqZ8Z9Z9Z9Z9Z9Z9Z9Z9ZuJxE0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y0Y', 'ROLE_PLAYER');

-- Commiter les changements
COMMIT;

