package tn.esprit.piboursebackend.Player.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponseDTO {
    private Long id;
    private Long playerId;
    private BigDecimal balance;        // Solde total de la BDD
    private BigDecimal reserved;       // Montant réservé (réservations actives)
    private BigDecimal available;      // Disponible = balance - reserved
    private String currency;           // Ex: "EUR"
    private String lastUpdated;
}
