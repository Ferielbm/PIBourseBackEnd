// tn/esprit/piboursebackend/Order/Controller/OrderController.java
package tn.esprit.piboursebackend.Order.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.piboursebackend.Order.Entity.*;
import tn.esprit.piboursebackend.Order.Service.MatchingEngineService;
import tn.esprit.piboursebackend.Order.Service.OrderQueryService;
import tn.esprit.piboursebackend.Order.Service.WalletService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players/{playerId}")
@RequiredArgsConstructor
@Tag(name = "Orders (ID user dans le path)")
public class OrderController {

    private final MatchingEngineService engine;
    private final OrderQueryService query;
    private final WalletService walletService; // 🔔 injection du wallet

    /* -------------------- UTIL FUNCS -------------------- */

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : v.toString();
    }

    private static BigDecimal dec(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        if (v instanceof Number num) return new BigDecimal(num.toString());
        return new BigDecimal(v.toString());
    }

    private static <E extends Enum<E>> E en(Map<String, Object> m, String key, Class<E> e) {
        String v = str(m, key);
        if (v == null) return null;

        try {
            return Enum.valueOf(e, v.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            String allowed = String.join(", ",
                    java.util.Arrays.stream(e.getEnumConstants())
                            .map(Enum::name)
                            .toList());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid value for '" + key + "' = '" + v + "' (expected: " + allowed + ")"
            );
        }
    }

    /* -------------------- PLACE ORDER -------------------- */

    @Operation(summary = "Placer un ordre (playerId dans le path)")
    @PostMapping("/orders")
    public ResponseEntity<Order> place(@PathVariable Long playerId,
                                       @RequestBody Map<String, Object> body) {
        try {
            String symbol   = str(body, "symbol");
            OrderSide side  = en(body, "side", OrderSide.class);
            OrderType type  = en(body, "type", OrderType.class);
            TimeInForce tif = en(body, "tif", TimeInForce.class);
            BigDecimal qty  = dec(body, "quantity");
            BigDecimal price = dec(body, "price");

            // 🔎 validations minimales
            if (symbol == null || symbol.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "symbol is required");
            }
            if (side == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "side is required (BUY/SELL)");
            }
            if (type == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required (LIMIT/MARKET)");
            }
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
            }
            if (type == OrderType.LIMIT &&
                    (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be > 0 for LIMIT orders");
            }

            // 💰 Vérifier le solde dispo avant un BUY LIMIT
            if (side == OrderSide.BUY && type == OrderType.LIMIT) {
                BigDecimal amount = price.multiply(qty);
                BigDecimal available = walletService.getAvailable(playerId);
                if (available.compareTo(amount) < 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Solde insuffisant: disponible=" + available + ", requis=" + amount
                    );
                }
            }

            // 🚀 création + matching + réservations gérés dans MatchingEngineService
            Order created = engine.placeOrder(
                    playerId, symbol, side, type, tif, qty, price
            );

            URI location = URI.create(
                    String.format("/api/players/%d/orders/%d", playerId, created.getId())
            );

            return ResponseEntity.created(location).body(created);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, ex.getMessage(), ex
            );
        }
    }

    /* -------------------- LIST ORDERS -------------------- */

    @Operation(summary = "Lister les ordres du joueur (playerId dans le path)")
    @GetMapping("/orders")
    public List<Order> list(@PathVariable Long playerId) {
        try {
            return query.getOrdersForPlayer(playerId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, ex.getMessage(), ex
            );
        }
    }

    /* -------------------- CANCEL ORDER -------------------- */

    @Operation(summary = "Annuler un ordre (playerId dans le path)")
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long playerId,
                                       @PathVariable Long orderId) {
        try {
            engine.cancelOpenOrder(playerId, orderId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, ex.getMessage(), ex
            );
        }
    }

    /* -------------------- ORDER BOOK (depuis la DB) -------------------- */

    @Operation(summary = "Carnet d’ordres par symbole")
    @GetMapping("/orderbook/{symbol}")
    public OrderQueryService.BookSnapshot book(@PathVariable String symbol) {
        try {
            return query.getBook(symbol);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, ex.getMessage(), ex
            );
        }
    }
}
