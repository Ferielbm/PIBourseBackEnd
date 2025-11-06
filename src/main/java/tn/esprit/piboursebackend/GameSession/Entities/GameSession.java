package tn.esprit.piboursebackend.GameSession.Entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.piboursebackend.Player.Entities.Player;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "game_master_id", nullable = false)
    private Player gameMaster;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.CREATED;

    @Column(nullable = false)
    private BigDecimal initialBalance;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column
    private LocalDateTime actualStartTime;

    @Column
    private LocalDateTime actualEndTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SessionPlayer> sessionPlayers = new ArrayList<>();

    @Column
    private Integer maxPlayers;

    @Column
    @Builder.Default
    private Boolean allowLateJoin = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SessionStatus.CREATED;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "USD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods

    public void addPlayer(SessionPlayer sessionPlayer) {
        sessionPlayers.add(sessionPlayer);
        sessionPlayer.setGameSession(this);
    }

    public void removePlayer(SessionPlayer sessionPlayer) {
        sessionPlayers.remove(sessionPlayer);
        sessionPlayer.setGameSession(null);
    }

    public boolean canAddPlayers() {
        return status == SessionStatus.CREATED || status == SessionStatus.READY;
    }

    public boolean canStart() {
        return (status == SessionStatus.CREATED || status == SessionStatus.READY) 
               && !sessionPlayers.isEmpty();
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED || status == SessionStatus.CANCELLED;
    }

    public int getPlayerCount() {
        return sessionPlayers.size();
    }

    public boolean isFull() {
        return maxPlayers != null && sessionPlayers.size() >= maxPlayers;
    }
}

