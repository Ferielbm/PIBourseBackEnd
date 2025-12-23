package tn.esprit.piboursebackend.Order.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.piboursebackend.Order.Entity.*;
import tn.esprit.piboursebackend.Order.Service.PriceAlertService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players/{playerId}/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final PriceAlertService service;

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : String.valueOf(v);
    }

    private static BigDecimal dec(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        if (v instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(v.toString());
    }

    // -------- ALERTES --------

    @Operation(summary = "Créer une alerte [min,max] pour un symbole")
    @PostMapping
    public PriceAlert createAlert(@PathVariable Long playerId,
                                  @RequestBody Map<String, Object> body) {
        try {
            String symbol = str(body, "symbol");
            // ⚠️ adapter aux noms utilisés par le front : minPrice / maxPrice
            BigDecimal min = dec(body, "minPrice");
            BigDecimal max = dec(body, "maxPrice");
            return service.createAlert(playerId, symbol, min, max);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @Operation(summary = "Lister les alertes d’un player")
    @GetMapping
    public List<PriceAlert> listAlerts(@PathVariable Long playerId) {
        return service.listAlertsForPlayer(playerId);
    }

    @Operation(summary = "Changer l’état d’une alerte (ACTIVE/PAUSED/CANCELLED)")
    @PutMapping("/{alertId}/status")
    public void setStatus(@PathVariable Long playerId,
                          @PathVariable Long alertId,
                          @RequestBody Map<String, Object> body) {
        String statusStr = str(body, "status");
        if (statusStr == null || statusStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        try {
            PriceAlertStatus status = PriceAlertStatus.valueOf(statusStr.trim().toUpperCase());
            service.setStatus(alertId, status, playerId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status must be one of: ACTIVE, PAUSED, CANCELLED");
        }
    }

    @Operation(summary = "Supprimer une alerte")
    @DeleteMapping("/{alertId}")
    public void deleteAlert(@PathVariable Long playerId,
                            @PathVariable Long alertId) {
        service.deleteAlert(alertId, playerId);
    }

    // -------- TICKETS (si tu veux les exposer pour ton UI) --------

    @Operation(summary = "Lister les tickets du player (option: ?status=PENDING|ACCEPTED|REJECTED)")
    @GetMapping("/tickets")
    public List<DecisionTicket> listTickets(@PathVariable Long playerId,
                                            @RequestParam(name = "status", required = false) String status) {
        if (status == null || status.isBlank()) {
            return service.listTicketsForPlayer(playerId);
        }
        try {
            return service.listTicketsForPlayerByStatus(playerId,
                    DecisionStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status must be one of: PENDING, ACCEPTED, REJECTED");
        }
    }

    @Operation(summary = "Décider un ticket (accept/reject). Si accept=true, quantity optionnelle.")
    @PostMapping("/tickets/{ticketId}/decide")
    public DecisionTicket decide(@PathVariable Long playerId,
                                 @PathVariable Long ticketId,
                                 @RequestBody Map<String, Object> body) {
        try {
            boolean accept = Boolean.parseBoolean(str(body, "accept"));
            BigDecimal qty = dec(body, "quantity");
            return service.decide(ticketId, accept, qty);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
