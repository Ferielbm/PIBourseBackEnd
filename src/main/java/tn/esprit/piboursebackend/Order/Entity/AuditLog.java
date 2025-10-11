package tn.esprit.piboursebackend.Order.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 160)
    private String actor;   // identifiant joueur / module

    @Column(nullable = false, length = 80)
    private String action;  // ORDER_PLACED, TRADE_EXECUTED, ...

    @Column(length = 2000)
    private String details; // JSON / description

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}