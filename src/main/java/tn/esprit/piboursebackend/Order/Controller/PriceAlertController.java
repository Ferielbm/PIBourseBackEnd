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

    private static String str(Map<String,Object> m, String k){ Object v=m.get(k); return v==null?null:String.valueOf(v); }
    private static BigDecimal dec(Map<String,Object> m, String k){
        Object v=m.get(k); if (v==null) return null;
        if (v instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(v.toString());
    }

    @Operation(summary="Créer une alerte [min,max] pour un symbole")
    @PostMapping
    public PriceAlert createAlert(@PathVariable Long playerId, @RequestBody Map<String,Object> body){
        try {
            return service.createAlert(playerId, str(body,"symbol"), dec(body,"min"), dec(body,"max"));
        } catch (IllegalArgumentException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @Operation(summary="Lister les tickets du player (option: ?status=PENDING|ACCEPTED|REJECTED)")
    @GetMapping("/tickets")
    public List<DecisionTicket> listTickets(@PathVariable Long playerId,
                                            @RequestParam(name="status", required=false) String status) {
        if (status == null || status.isBlank()) {
            return service.listTicketsForPlayer(playerId);
        }
        try {
            return service.listTicketsForPlayerByStatus(playerId, DecisionStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status must be one of: PENDING, ACCEPTED, REJECTED");
        }
    }

    @Operation(summary="Décider un ticket (accept/reject). Si accept=true, quantity optionnelle.")
    @PostMapping("/tickets/{ticketId}/decide")
    public DecisionTicket decide(@PathVariable Long playerId,
                                 @PathVariable Long ticketId,
                                 @RequestBody Map<String,Object> body){
        try {
            boolean accept = Boolean.parseBoolean(str(body,"accept"));
            BigDecimal qty = dec(body, "quantity");
            return service.decide(ticketId, accept, qty);
        } catch (IllegalArgumentException | IllegalStateException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @Operation(summary="Changer l’état d’une alerte (ACTIVE/PAUSED/CANCELLED)")
    @PostMapping("/{alertId}/status")
    public void setStatus(@PathVariable Long playerId,
                          @PathVariable Long alertId,
                          @RequestBody Map<String,Object> body){
        try {
            service.setStatus(alertId, PriceAlertStatus.valueOf(str(body,"status").toUpperCase()), playerId);
        } catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
