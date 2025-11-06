package tn.esprit.piboursebackend.GameSession.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionLeaderboardDTO {
    
    private Long sessionId;
    private String sessionName;
    private List<SessionPlayerDTO> players;
    private Integer totalPlayers;
}

