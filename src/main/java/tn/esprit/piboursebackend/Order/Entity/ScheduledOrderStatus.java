package tn.esprit.piboursebackend.Order.Entity;

public enum ScheduledOrderStatus {
    PENDING,        // En attente
    TRIGGERED,      // Conditions atteintes (ticket créé ou ordre prêt)
    EXECUTED,       // Ordre effectif placé
    REFUSED,        // Ticket rejeté par l'utilisateur
    CANCELLED,      // Annulé
    FAILED          // Erreur
}
