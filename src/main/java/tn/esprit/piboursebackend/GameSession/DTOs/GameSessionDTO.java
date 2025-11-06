package tn.esprit.piboursebackend.GameSession.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.piboursebackend.GameSession.Entities.SessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long gameMasterId;
    private String gameMasterUsername;
    private String gameMasterEmail;
    private SessionStatus status;
    private BigDecimal initialBalance;
    private String currency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer playerCount;
    private Integer maxPlayers;
    private Boolean allowLateJoin;
    private Boolean isFull;
}

