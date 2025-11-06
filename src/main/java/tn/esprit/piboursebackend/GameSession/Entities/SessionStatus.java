package tn.esprit.piboursebackend.GameSession.Entities;

public enum SessionStatus {
    
    CREATED,        // Session créée, en attente de joueurs
    READY,          // Tous les joueurs ajoutés, prête à démarrer
    ACTIVE,         // Session en cours
    PAUSED,         // Session en pause
    COMPLETED,      // Session terminée normalement
    CANCELLED       // Session annulée
}

