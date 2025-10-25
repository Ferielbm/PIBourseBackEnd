package tn.esprit.piboursebackend.Order.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.OrderSide;
import tn.esprit.piboursebackend.Order.Entity.OrderType;
import tn.esprit.piboursebackend.Order.Entity.TimeInForce;
import tn.esprit.piboursebackend.Order.Service.MatchingEngineService;
import tn.esprit.piboursebackend.Order.Service.OrderQueryService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/players/{playerId}")
@RequiredArgsConstructor
@Tag(name = "Orders (ID user dans le path)")
public class OrderController {

    private final MatchingEngineService engine;
    private final OrderQueryService query;

    private static String str(Map<String,Object> m, String key) {
        Object v = m.get(key); return v == null ? null : v.toString();
    }
    private static BigDecimal dec(Map<String,Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(v.toString());
    }
    private static <E extends Enum<E>> E en(Map<String,Object> m, String key, Class<E> e) {
        String v = str(m, key);
        if (v == null) return null;
        try { return Enum.valueOf(e, v.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) {
            String allowed = String.join(", ",
                    java.util.Arrays.stream(e.getEnumConstants()).map(Enum::name).toList());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid value for "+key+"="+v+" (expected one of: " + allowed + ")");
        }
    }

    @Operation(summary = "Placer un ordre (playerId dans le path)")
    @PostMapping("/orders")
    public Order place(@PathVariable Long playerId, @RequestBody Map<String,Object> body) {
        try {
            String actor = "user:" + playerId;
            String symbol = str(body, "symbol");
            OrderSide side = en(body, "side", OrderSide.class);
            OrderType type = en(body, "type", OrderType.class);
            TimeInForce tif = en(body, "tif", TimeInForce.class);
            BigDecimal qty = dec(body, "quantity");
            BigDecimal price = dec(body, "price");

            return engine.placeOrder(actor, symbol, side, type, tif, qty, price);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @Operation(summary = "Annuler un ordre (playerId dans le path)")
    @PostMapping("/orders/{orderId}/cancel")
    public void cancel(@PathVariable Long playerId, @PathVariable Long orderId) {
        try {
            engine.cancelOpenOrder("user:" + playerId, orderId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @Operation(summary = "Carnet d’ordres par symbole")
    @GetMapping("/orderbook/{symbol}")
    public OrderQueryService.BookSnapshot book(@PathVariable String symbol) {
        try {
            return query.getBook(symbol);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
