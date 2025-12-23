package tn.esprit.piboursebackend.Order.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Order.Entity.DecisionTicket;
import tn.esprit.piboursebackend.Order.Entity.ScheduledOrder;
import tn.esprit.piboursebackend.Order.Service.ScheduledOrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players/{playerId}/scheduled-orders")
@RequiredArgsConstructor
public class ScheduledOrderController {

    private final ScheduledOrderService service;

    // Créer une planification (playerId dans le path)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ScheduledOrder create(@PathVariable Long playerId, @RequestBody Map<String,Object> body) {
        body.put("playerId", playerId);
        return service.create(body);
    }

    // Lancer le process pour 1 symbole (manuel/cron)
    @PostMapping("/process")
    public void process(@RequestParam String symbol) {
        service.processForSymbol(symbol);
    }

    // Annuler une planification PENDING
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long playerId,
                       @PathVariable Long id,
                       @RequestHeader(name="X-Actor", required=false) String actor) {
        service.cancel(id, actor != null ? actor : ("user:" + playerId));
    }

    // Supporter DELETE depuis l'UI qui envoie un DELETE sur /api/players/{playerId}/scheduled-orders/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long playerId,
                       @PathVariable Long id,
                       @RequestHeader(name="X-Actor", required=false) String actor) {
        service.cancel(id, actor != null ? actor : ("user:" + playerId));
    }

    // Réactiver un ordre TRIGGERED pour recevoir un nouveau ticket
    @PutMapping("/{id}/reactivate")
    public ScheduledOrder reactivate(@PathVariable Long playerId,
                                      @PathVariable Long id,
                                      @RequestHeader(name="X-Actor", required=false) String actor) {
        return service.reactivate(id, actor != null ? actor : ("user:" + playerId));
    }

    // Récupérer les tickets de décision en attente
    @GetMapping("/tickets/pending")
    public List<DecisionTicket> getPendingTickets(@PathVariable Long playerId) {
        return service.listPendingTickets(playerId);
    }

    // Accepter ou rejeter un ticket de décision
    @PostMapping("/tickets/{ticketId}/decide")
    public DecisionTicket decideTicket(
            @PathVariable Long playerId,
            @PathVariable Long ticketId,
            @RequestBody Map<String, Object> body) {
        
        boolean accept = body.get("accept") != null && (Boolean) body.get("accept");
        BigDecimal quantity = null;
        
        if (body.containsKey("quantity")) {
            Object qtyValue = body.get("quantity");
            if (qtyValue != null) {
                quantity = new BigDecimal(qtyValue.toString());
            }
        }
        
        return service.decide(ticketId, accept, quantity);
    }

    // ⚠️ IMPORTANT : PAS de /alerts/tickets ici !
}
