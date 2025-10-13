package com.example.pibourse.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // BUY, SELL, DEPOSIT, WITHDRAW
    private double amount;


    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }


    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
}
