package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actor;   // identifiant ou nom du joueur / module

    private String action;  // type d’action (ex : ORDER_PLACED, TRADE_EXECUTED)

    @Column(length = 2000)
    private String details; // description détaillée de l’événement

    private LocalDateTime timestamp = LocalDateTime.now();
}
