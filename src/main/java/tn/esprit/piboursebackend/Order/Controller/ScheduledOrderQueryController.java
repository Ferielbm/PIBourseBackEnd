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

    // GET /api/players/1/scheduled-orders?status=PENDING
    // GET /api/players/1/scheduled-orders?status=PENDING&symbol=TSLA
    @GetMapping("/scheduled-orders")
    public List<ScheduledOrder> list(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "PENDING") ScheduledOrderStatus status,
            @RequestParam(required = false) String symbol
    ) {
        if (symbol == null || symbol.isBlank()) {
            return repo.findByPlayerIdAndStatusOrderByCreatedAtAsc(playerId, status);
        } else {
            return repo.findByPlayerIdAndStatusAndDesiredSymbolOrderByCreatedAtAsc(playerId, status, symbol);
        }
    }
}
