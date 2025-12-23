// src/main/java/tn/esprit/piboursebackend/Order/Controller/ScheduledOrderQueryController.java
package tn.esprit.piboursebackend.Order.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Order.Entity.ScheduledOrder;
import tn.esprit.piboursebackend.Order.Entity.ScheduledOrderStatus;
import tn.esprit.piboursebackend.Order.Repository.ScheduledOrderRepository;

import java.util.List;

@RestController
@RequestMapping("/api/players/{playerId}")
@RequiredArgsConstructor
public class ScheduledOrderQueryController {

    private final ScheduledOrderRepository repo;


    @GetMapping("/scheduled-orders")
    public List<ScheduledOrder> list(
            @PathVariable Long playerId,
            @RequestParam(required = false) String status, // accepte PENDING / TRIGGERED / CANCELLED / FAILED / ALL / (null)
            @RequestParam(required = false) String symbol
    ) {
        ScheduledOrderStatus effectiveStatus = null;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            try {
                effectiveStatus = ScheduledOrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // statut inconnu -> on ignore et on retourne tout
            }
        }

        boolean hasSymbol = symbol != null && !symbol.isBlank();

        if (effectiveStatus == null) {
            // Pas de filtre status -> on renvoie tout
            if (!hasSymbol) {
                return repo.findByPlayerIdOrderByCreatedAtAsc(playerId);
            } else {
                return repo.findByPlayerIdAndDesiredSymbolOrderByCreatedAtAsc(playerId, symbol);
            }
        } else {
            // Filtre sur un statut précis
            if (!hasSymbol) {
                return repo.findByPlayerIdAndStatusOrderByCreatedAtAsc(playerId, effectiveStatus);
            } else {
                return repo.findByPlayerIdAndStatusAndDesiredSymbolOrderByCreatedAtAsc(playerId, effectiveStatus, symbol);
            }
        }
    }
}
