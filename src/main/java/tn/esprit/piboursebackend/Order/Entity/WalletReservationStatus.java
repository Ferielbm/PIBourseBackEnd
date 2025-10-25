package tn.esprit.piboursebackend.Order.Entity;

public enum WalletReservationStatus {
    ACTIVE,    // réservée, bloque la dispo
    CONSUMED,  // consommée par un trade
    RELEASED   // libérée après annulation/rejet
}