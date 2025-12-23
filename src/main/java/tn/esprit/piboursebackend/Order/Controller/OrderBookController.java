package tn.esprit.piboursebackend.Order.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Order.Entity.BookSnapshot;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.OrderDTO;
import tn.esprit.piboursebackend.Order.Repository.OrderRepository;
import tn.esprit.piboursebackend.Order.Service.OrderBookService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // adapte selon ton front
public class OrderBookController {

    private final OrderBookService orderBookService;
    private final OrderRepository orderRepository;

    /**
     * 🔹 Tous les ordres de tous les players (vue admin)
     * GET /api/allorders
     */
    @GetMapping("/allorders")
    public List<OrderDTO> getAllOrders() {
        return orderBookService.getAllOrdersWithRelations();
    }
    @GetMapping("/players/{playerId}/orderbooks/{symbol}")
    public BookSnapshot getSnapshot(@PathVariable Long playerId, @PathVariable String symbol) {
        // playerId est dans le path pour cohérence mais non-utilisé ici
        return orderBookService.getSnapshot(symbol);
    }
}