package tn.esprit.piboursebackend.Order.Controller;

import lombok.RequiredArgsConstructor;
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
        // On impose le playerId du path au service (pas besoin dans le body Postman)
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
    public void cancel(@PathVariable Long playerId,
                       @PathVariable Long id,
                       @RequestHeader(name="X-Actor", required=false) String actor) {
        service.cancel(id, actor != null ? actor : ("user:" + playerId));
    }

    // ⚠️ IMPORTANT : PAS de /alerts/tickets ici !
}
