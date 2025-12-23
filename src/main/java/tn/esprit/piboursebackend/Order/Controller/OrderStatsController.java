package tn.esprit.piboursebackend.Order.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.piboursebackend.Order.Service.MatchingEngineService;
import tn.esprit.piboursebackend.Order.dto.OrderBookStats;

@RestController
@RequestMapping("/api/orderbook")
public class OrderStatsController {

    private final MatchingEngineService matchingEngineService;

    public OrderStatsController(MatchingEngineService matchingEngineService) {
        this.matchingEngineService = matchingEngineService;
    }

    @GetMapping("/{symbol}/stats")
    public ResponseEntity<OrderBookStats> getStats(@PathVariable("symbol") String symbol) {
        OrderBookStats stats = matchingEngineService.getStatsForSymbol(symbol);
        return ResponseEntity.ok(stats);
    }
}
