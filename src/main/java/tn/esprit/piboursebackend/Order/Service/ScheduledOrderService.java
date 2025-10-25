package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;
import tn.esprit.piboursebackend.Order.Entity.*;
import tn.esprit.piboursebackend.Order.Repository.DecisionTicketRepository;
import tn.esprit.piboursebackend.Order.Repository.OrderRepository;
import tn.esprit.piboursebackend.Order.Repository.ScheduledOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledOrderService {

    private final ScheduledOrderRepository schedRepo;
    private final DecisionTicketRepository ticketRepo;
    private final OrderRepository orderRepo;
    private final StockRepository stockRepo;
    private final MatchingEngineService engine;
    private final AuditLogService audit;

    // ---- Création / annulation ----

    @Transactional
    public ScheduledOrder create(Map<String,Object> body){
        Long playerId = lng(body,"playerId");
        String symbol = str(body,"symbol");
        OrderSide side = OrderSide.valueOf(str(body,"side").toUpperCase());
        BigDecimal qty = dec(body,"quantity");
        BigDecimal min = dec(body,"minPrice");
        BigDecimal max = dec(body,"maxPrice");
        boolean notifyOnly = bool(body,"notifyOnly", true);
        boolean notifyApproach = bool(body,"notifyWhenApproachMin", false);
        BigDecimal threshold = dec(body,"approachThresholdPct");
        Integer cooldownMin = intval(body,"approachCooldownMinutes");

        if (qty == null || qty.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
        if (min != null && max != null && min.compareTo(max) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be <= maxPrice");

        String actor = "user:" + playerId;

        var po = ScheduledOrder.builder()
                .actor("user:" + playerId)
                .playerId(playerId)
                .desiredSymbol(symbol)     // ⬅️ ICI (et pas symbol)
                .side(side)
                .quantity(qty)
                .minPrice(min)
                .maxPrice(max)
                .notifyOnly(notifyOnly)
                .notifyWhenApproachMin(notifyApproach)
                .approachThresholdPct(threshold)
                .approachCooldownMinutes(cooldownMin!=null? cooldownMin : 60)
                .status(ScheduledOrderStatus.PENDING)
                .build();


        po = schedRepo.save(po);

        audit.log(actor, "SCHEDULE_CREATED",
                "planId="+po.getId()+", "+symbol+" "+side+" qty="+qty
                        + (min!=null? ", min="+min:"") + (max!=null? ", max="+max:"")
                        + (notifyApproach? ", approach="+(threshold!=null?threshold:"0.05"):""));

        processForSymbol(symbol);
        return po;
    }

    @Transactional
    public void cancel(Long planId, String actor){
        var po = schedRepo.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "scheduled order not found"));
        if (po.getStatus() != ScheduledOrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING can be cancelled");
        }
        po.setStatus(ScheduledOrderStatus.CANCELLED);
        schedRepo.save(po);
        audit.log(actor != null ? actor : "system", "SCHEDULE_CANCELLED", "planId="+planId);
    }

    // ---- Déclenchement (à appeler après place/trade/stock create) ----
    @Transactional
    public void processForSymbol(String symbol) {
        var stockOpt = stockRepo.findBySymbol(symbol);
        if (stockOpt.isEmpty()) return;
        var stock = stockOpt.get();

        BigDecimal bestAsk = orderRepo.findAsksForMatching(stock).stream()
                .filter(o -> o.getPrice()!=null && isOpen(o))
                .map(Order::getPrice).min(Comparator.naturalOrder()).orElse(null);

        BigDecimal bestBid = orderRepo.findBidsForMatching(stock).stream()
                .filter(o -> o.getPrice()!=null && isOpen(o))
                .map(Order::getPrice).max(Comparator.naturalOrder()).orElse(null);

        if (bestAsk == null && bestBid == null) return;

        // ⬇️ Pas de findByStatus(...). On filtre en mémoire.
        List<ScheduledOrder> plans = schedRepo.findAll().stream()
                .filter(p -> p.getStatus() == ScheduledOrderStatus.PENDING)
                .filter(p -> p.getDesiredSymbol() != null && p.getDesiredSymbol().equalsIgnoreCase(symbol))
                .collect(Collectors.toList());

        for (var p : plans) {
            boolean isBuy = p.getSide() == OrderSide.BUY;
            BigDecimal px = isBuy ? bestAsk : bestBid;
            if (px == null) continue;

            // 1) APPROCHE du min
            if (p.isNotifyWhenApproachMin()
                    && p.getMinPrice() != null
                    && isApproachingMin(px, p.getMinPrice(), p.getApproachThresholdPct())
                    && cooldownOk(p.getLastApproachNotifiedAt(), p.getApproachCooldownMinutes())) {

                var t = DecisionTicket.builder()
                        .playerId(p.getPlayerId())
                        .symbol(symbol)
                        .side(p.getSide())
                        .foundPrice(px)
                        .suggestedQuantity(p.getQuantity())
                        .reason(TicketReason.APPROACHING_MIN)
                        .status(DecisionStatus.PENDING)
                        .build();
                ticketRepo.save(t);

                p.setLastApproachNotifiedAt(LocalDateTime.now());
                schedRepo.save(p);

                audit.log("user:"+p.getPlayerId(), "SCHEDULE_APPROACH_TICKET_CREATED",
                        "planId="+p.getId()+", ticketId="+t.getId()+", "+symbol+"@"+px);
            }

            // 2) DANS LA FOURCHETTE
            if (!inRange(px, p.getMinPrice(), p.getMaxPrice())) continue;

            if (p.isNotifyOnly()) {
                var t = DecisionTicket.builder()
                        .playerId(p.getPlayerId())
                        .symbol(symbol)
                        .side(p.getSide())
                        .foundPrice(px)
                        .suggestedQuantity(p.getQuantity())
                        .reason(TicketReason.IN_RANGE)
                        .status(DecisionStatus.PENDING)
                        .build();
                ticketRepo.save(t);

                audit.log("user:"+p.getPlayerId(), "SCHEDULE_TICKET_CREATED",
                        "planId="+p.getId()+", ticketId="+t.getId()+", "+symbol+"@"+px);
            } else {
                var order = engine.placeOrder("user:"+p.getPlayerId(), symbol, p.getSide(),
                        OrderType.LIMIT, TimeInForce.DAY, p.getQuantity(), px);
                p.setStatus(ScheduledOrderStatus.TRIGGERED);
                schedRepo.save(p);

                audit.log("user:"+p.getPlayerId(), "SCHEDULED_ORDER_PLACED",
                        "planId="+p.getId()+", orderId="+order.getId()+", "+symbol+"@"+px);
            }
        }
    }

    // ---- Tickets (liste/decide) ----

    @Transactional(readOnly = true)
    public List<DecisionTicket> listPendingTickets(Long playerId){
        return ticketRepo.findByPlayerIdAndStatus(playerId, DecisionStatus.PENDING);
    }

    @Transactional
    public DecisionTicket decide(Long ticketId, boolean accept, BigDecimal quantity){
        var t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ticket not found"));
        if (t.getStatus() != DecisionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket not pending");
        }
        if (!accept) {
            t.setStatus(DecisionStatus.REJECTED);
            ticketRepo.save(t);
            audit.log("user:"+t.getPlayerId(), "TICKET_REJECTED", "ticketId="+t.getId());
            return t;
        }
        BigDecimal q = (quantity!=null && quantity.signum()>0) ? quantity : t.getSuggestedQuantity();
        var order = engine.placeOrder("user:"+t.getPlayerId(), t.getSymbol(), t.getSide(),
                OrderType.LIMIT, TimeInForce.DAY, q, t.getFoundPrice());
        t.setStatus(DecisionStatus.ACCEPTED);
        ticketRepo.save(t);
        audit.log("user:"+t.getPlayerId(), "TICKET_ACCEPTED",
                "ticketId="+t.getId()+", orderId="+order.getId());
        return t;
    }

    // ---- Helpers ----

    private boolean isOpen(Order o){
        return o.getStatus()==OrderStatus.PENDING || o.getStatus()==OrderStatus.PARTIALLY_FILLED;
    }

    private boolean inRange(BigDecimal p, BigDecimal min, BigDecimal max){
        if (p == null) return false;
        if (min != null && p.compareTo(min) < 0) return false;
        if (max != null && p.compareTo(max) > 0) return false;
        return true;
    }

    /** Approche : p ∈ (min, min*(1+threshold)] */
    private boolean isApproachingMin(BigDecimal price, BigDecimal minPrice, BigDecimal thresholdPct) {
        if (price == null || minPrice == null) return false;
        if (thresholdPct == null) thresholdPct = new BigDecimal("0.05");
        if (price.compareTo(minPrice) <= 0) return false; // déjà au/ sous min => inRange
        BigDecimal maxApproach = minPrice.multiply(BigDecimal.ONE.add(thresholdPct));
        return price.compareTo(maxApproach) <= 0;
    }

    private boolean cooldownOk(LocalDateTime last, Integer minutes) {
        int mm = (minutes == null ? 0 : minutes);
        if (mm <= 0) return true;
        if (last == null) return true;
        return last.plusMinutes(mm).isBefore(LocalDateTime.now());
    }

    // extractors (pas de DTO)
    private static String str(Map<String,Object> m, String k){
        var v = m.get(k); if (v==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k+" is required");
        return String.valueOf(v);
    }
    private static Long lng(Map<String,Object> m, String k){
        var v = m.get(k); if (v==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k+" is required");
        try { return (v instanceof Number)? ((Number)v).longValue() : Long.parseLong(String.valueOf(v)); }
        catch (Exception e){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k+" must be a number"); }
    }
    private static Integer intval(Map<String,Object> m, String k){
        var v = m.get(k); if (v==null) return null;
        try { return (v instanceof Number)? ((Number)v).intValue() : Integer.parseInt(String.valueOf(v)); }
        catch (Exception e){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k+" must be an integer"); }
    }
    private static BigDecimal dec(Map<String,Object> m, String k){
        var v = m.get(k); if (v==null) return null;
        try { return new BigDecimal(String.valueOf(v)); }
        catch (Exception e){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k+" must be a decimal"); }
    }
    private static boolean bool(Map<String,Object> m, String k, boolean def){
        var v = m.get(k); if (v==null) return def;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
