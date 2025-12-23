package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.Set;
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
    private final NotificationService notificationService;

    // =====================================================================
    //  SCHEDULER - Vérifie les ordres planifiés toutes les 2 secondes
    // =====================================================================

    @Scheduled(fixedRate = 2000)
    @Transactional
    public void checkAndExecuteScheduledOrders() {
        try {
            // Récupérer tous les symboles avec des ordres PENDING
            Set<String> symbolsWithPending = schedRepo.findAll().stream()
                    .filter(p -> p.getStatus() == ScheduledOrderStatus.PENDING)
                    .map(ScheduledOrder::getDesiredSymbol)
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());

            if (!symbolsWithPending.isEmpty()) {
                System.out.println("🔄 [SCHEDULER] Vérification des ordres planifiés pour: " + symbolsWithPending);
            }

            for (String symbol : symbolsWithPending) {
                try {
                    processForSymbol(symbol);
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors du traitement du symbole " + symbol + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur dans checkAndExecuteScheduledOrders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    //  Création / annulation d'un ordre planifié
    // =====================================================================

    @Transactional
    public ScheduledOrder create(Map<String, Object> body) {
        Long playerId = lng(body, "playerId");
        // Accepte à la fois "symbol" et "desiredSymbol"
        String symbol = body.containsKey("symbol") ? str(body, "symbol") : str(body, "desiredSymbol");
        OrderSide side = OrderSide.valueOf(str(body, "side").toUpperCase());
        BigDecimal qty = dec(body, "quantity");
        BigDecimal min = dec(body, "minPrice");
        BigDecimal max = dec(body, "maxPrice");

        // ⚠️ Par défaut : exécution automatique (notifyOnly = false)
        boolean notifyOnly = bool(body, "notifyOnly", false);
        boolean notifyApproach = bool(body, "notifyWhenApproachMin", false);
        BigDecimal threshold = dec(body, "approachThresholdPct");
        Integer cooldownMin = intval(body, "approachCooldownMinutes");

        if (qty == null || qty.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be <= maxPrice");
        }

        String actor = "user:" + playerId;

        var po = ScheduledOrder.builder()
                .actor(actor)
                .playerId(playerId)
                .desiredSymbol(symbol)
                .side(side)
                .quantity(qty)
                .minPrice(min)
                .maxPrice(max)
                .notifyOnly(notifyOnly)
                .notifyWhenApproachMin(notifyApproach)
                .approachThresholdPct(threshold)
                .approachCooldownMinutes(cooldownMin != null ? cooldownMin : 60)
                .status(ScheduledOrderStatus.PENDING)
                .build();

        po = schedRepo.save(po);

        audit.log(actor, "SCHEDULE_CREATED",
                "planId=" + po.getId() + ", " + symbol + " " + side + " qty=" + qty
                        + (min != null ? ", min=" + min : "")
                        + (max != null ? ", max=" + max : "")
                        + (notifyApproach ? ", approach=" + (threshold != null ? threshold : "0.05") : "")
                        + ", notifyOnly=" + notifyOnly);

        // On regarde tout de suite si l'ordre book permet déjà de déclencher
        processForSymbol(symbol);

        return po;
    }

    @Transactional
    public void cancel(Long planId, String actor) {
        var po = schedRepo.findById(planId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "scheduled order not found"));

        if (po.getStatus() != ScheduledOrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING can be cancelled");
        }

        po.setStatus(ScheduledOrderStatus.CANCELLED);
        schedRepo.save(po);

        audit.log(actor != null ? actor : "system",
                "SCHEDULE_CANCELLED",
                "planId=" + planId);
    }

    @Transactional
    public ScheduledOrder reactivate(Long planId, String actor) {
        var po = schedRepo.findById(planId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "scheduled order not found"));

        if (po.getStatus() != ScheduledOrderStatus.TRIGGERED && po.getStatus() != ScheduledOrderStatus.REFUSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only TRIGGERED or REFUSED orders can be reactivated");
        }

        po.setStatus(ScheduledOrderStatus.PENDING);
        schedRepo.save(po);

        audit.log(actor != null ? actor : "system",
                "SCHEDULE_REACTIVATED",
                "planId=" + planId + " - ordre remis en PENDING pour recevoir un nouveau ticket");

        return po;
    }

    // =====================================================================
    //  Déclenchement en fonction de l'ordre book
    // =====================================================================

    @Transactional
    public void processForSymbol(String symbol) {
        System.out.println("\n📊 [PROCESS] Traitement du symbole: " + symbol);
        
        var stockOpt = stockRepo.findBySymbol(symbol);
        if (stockOpt.isEmpty()) {
            System.out.println("⚠️ [PROCESS] Stock " + symbol + " introuvable");
            return;
        }
        var stock = stockOpt.get();
        // Charger listes une seule fois (on filtrera par player ensuite)
        var allAsks = orderRepo.findAsksForMatching(stock).stream()
            .filter(o -> o.getPrice() != null && isOpen(o))
            .collect(Collectors.toList());
        var allBids = orderRepo.findBidsForMatching(stock).stream()
            .filter(o -> o.getPrice() != null && isOpen(o))
            .collect(Collectors.toList());

        System.out.println("📈 [PROCESS] Carnet: " + allAsks.size() + " ASKs, " + allBids.size() + " BIDs");

        if (allAsks.isEmpty() && allBids.isEmpty()) {
            System.out.println("⚠️ [PROCESS] Carnet vide pour " + symbol);
            return; // carnet vide
        }

        // On ne fait pas findByStatus en DB : on filtre en mémoire
        List<ScheduledOrder> plans = schedRepo.findAll().stream()
                .filter(p -> p.getStatus() == ScheduledOrderStatus.PENDING)
                .filter(p -> p.getDesiredSymbol() != null
                        && p.getDesiredSymbol().equalsIgnoreCase(symbol))
                .collect(Collectors.toList());
        
        System.out.println("📋 [PROCESS] " + plans.size() + " ordre(s) planifié(s) PENDING pour " + symbol);

        for (var p : plans) {
            System.out.println("\n🔍 [ORDRE #" + p.getId() + "] Analyse de l'ordre planifié:");
            System.out.println("   - Joueur: " + p.getPlayerId());
            System.out.println("   - Côté: " + p.getSide());
            System.out.println("   - Quantité: " + p.getQuantity());
            System.out.println("   - Fourchette: [" + p.getMinPrice() + ", " + p.getMaxPrice() + "]");
            System.out.println("   - NotifyOnly: " + p.isNotifyOnly());

            boolean isBuy = p.getSide() == OrderSide.BUY;

            // Exclure les ordres du même joueur (éviter auto-match / self-trade)
            BigDecimal px = null;
            if (isBuy) {
                px = allAsks.stream()
                        .filter(o -> !o.getPlayerId().equals(p.getPlayerId()))
                        .map(Order::getPrice)
                        .min(Comparator.naturalOrder())
                        .orElse(null);
            } else { // SELL
                px = allBids.stream()
                        .filter(o -> !o.getPlayerId().equals(p.getPlayerId()))
                        .map(Order::getPrice)
                        .max(Comparator.naturalOrder())
                        .orElse(null);
            }

            System.out.println("   - Prix trouvé dans le carnet: " + px);

            // Si aucun prix disponible hors ordres du joueur, on ignore ce plan
            if (px == null) {
                System.out.println("   ⚠️ Aucun prix disponible (uniquement ordres du même joueur)");
                continue;
            }

            // ----------------------------
            // 1) Notification "approche du min"
            // ----------------------------
            if (p.isNotifyWhenApproachMin()
                    && p.getMinPrice() != null
                    && isApproachingMin(px, p.getMinPrice(), p.getApproachThresholdPct())
                    && cooldownOk(p.getLastApproachNotifiedAt(), p.getApproachCooldownMinutes())) {

                // Vérifier qu'aucun ticket PENDING n'existe déjà pour cet ordre
                boolean hasExistingTicket = ticketRepo.findByPlayerIdAndStatus(p.getPlayerId(), DecisionStatus.PENDING)
                        .stream()
                        .anyMatch(t -> t.getScheduledOrderId() != null && t.getScheduledOrderId().equals(p.getId()));

                if (!hasExistingTicket) {
                    var t = DecisionTicket.builder()
                            .playerId(p.getPlayerId())
                            .symbol(symbol)
                            .side(p.getSide())
                            .foundPrice(px)
                            .suggestedQuantity(p.getQuantity())
                            .reason(TicketReason.APPROACHING_MIN)
                            .status(DecisionStatus.PENDING)
                            .scheduledOrderId(p.getId())
                            .build();

                    t = ticketRepo.save(t);

                    p.setLastApproachNotifiedAt(LocalDateTime.now());
                    schedRepo.save(p);

                    audit.log("user:" + p.getPlayerId(), "SCHEDULE_APPROACH_TICKET_CREATED",
                            "planId=" + p.getId() + ", ticketId=" + t.getId() + ", " + symbol + "@" + px);

                    // notif WS (facultatif)
                    notificationService.sendDecisionTicketCreated(t);
                }
            }            // ----------------------------
            // 2) Prix du carnet DANS la fourchette [minPrice, maxPrice]
            // ----------------------------
            boolean priceInRange = inRange(px, p.getMinPrice(), p.getMaxPrice());
            System.out.println("   - Prix " + px + " dans fourchette [" + p.getMinPrice() + ", " + p.getMaxPrice() + "]: " + priceInRange);
            
            if (!priceInRange) {
                System.out.println("   ⏭️ Prix hors fourchette, ordre ignoré");
                continue;
            }

            if (p.isNotifyOnly()) {
                System.out.println("   🔔 MODE MANUEL: Création d'un Decision Ticket");
                // MODE MANUEL : on crée UN SEUL ticket
                // Si le plan est déjà TRIGGERED, cela signifie qu'un ticket a déjà été créé
                if (p.getStatus() == ScheduledOrderStatus.PENDING) {
                    // Vérifier qu'aucun ticket PENDING n'existe déjà pour cet ordre
                    boolean hasExistingTicket = ticketRepo.findByPlayerIdAndStatus(p.getPlayerId(), DecisionStatus.PENDING)
                            .stream()
                            .anyMatch(t -> t.getScheduledOrderId() != null && t.getScheduledOrderId().equals(p.getId()));

                    if (!hasExistingTicket) {
                        System.out.println("   ✅ Création ticket pour plan " + p.getId() + " (" + symbol + " " + p.getSide() + ")");
                        
                        var t = DecisionTicket.builder()
                            .playerId(p.getPlayerId())
                            .symbol(symbol)
                            .side(p.getSide())
                            .foundPrice(px)
                            .suggestedQuantity(p.getQuantity())
                            .reason(TicketReason.IN_RANGE)
                            .status(DecisionStatus.PENDING)
                            .scheduledOrderId(p.getId())
                            .build();

                        t = ticketRepo.save(t);

                        p.setStatus(ScheduledOrderStatus.TRIGGERED); // ✅ évite de recréer le ticket
                        schedRepo.save(p);

                        audit.log("user:" + p.getPlayerId(), "SCHEDULE_TICKET_CREATED",
                                "planId=" + p.getId() + ", ticketId=" + t.getId() + ", " + symbol + "@" + px);

                        notificationService.sendDecisionTicketCreated(t);
                    } else {
                        System.out.println("⚠️ Ticket PENDING existe déjà pour plan " + p.getId());
                    }
                } else {
                    System.out.println("⚠️ Plan " + p.getId() + " déjà " + p.getStatus() + ", pas de nouveau ticket");
                }

            } else {
                System.out.println("   🚀 MODE AUTO: Exécution automatique de l'ordre");
                // MODE AUTO : on passe un ordre direct dans le MatchingEngine
                try {
                    var order = engine.placeOrder(
                            p.getPlayerId(),
                            symbol,
                            p.getSide(),
                            OrderType.LIMIT,
                            TimeInForce.DAY,
                            p.getQuantity(),
                            px
                    );

                    p.setStatus(ScheduledOrderStatus.EXECUTED);
                    p.setExecutedOrderId(order.getId());
                    schedRepo.save(p);
                    
                    System.out.println("   ✅ Ordre #" + order.getId() + " exécuté avec succès!");

                    // Recharger l'ordre pour avoir le statut après matching
                    if (order.getId() != null) {
                        order = orderRepo.findById(order.getId()).orElse(order);
                    }

                    audit.log("user:" + p.getPlayerId(), "SCHEDULED_ORDER_PLACED",
                            "planId=" + p.getId() + ", orderId=" + order.getId()
                                    + ", " + symbol + "@" + px + ", status=" + order.getStatus());

                    notificationService.sendScheduledOrderExecuted(p, order);
                } catch (Exception e) {
                    System.err.println("   ❌ Erreur lors de l'exécution: " + e.getMessage());
                    e.printStackTrace();
                    throw e; // Re-lancer pour garder le comportement existant
                }
            }
        }
    }

    // =====================================================================
    //  Tickets (consultation / décision)
    // =====================================================================

    @Transactional(readOnly = true)
    public List<DecisionTicket> listPendingTickets(Long playerId) {
        return ticketRepo.findByPlayerIdAndStatus(playerId, DecisionStatus.PENDING);
    }

    @Transactional
    public DecisionTicket decide(Long ticketId, boolean accept, BigDecimal quantity) {
        var t = ticketRepo.findById(ticketId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "ticket not found"));

        if (t.getStatus() != DecisionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket not pending");
        }

        if (!accept) {
            t.setStatus(DecisionStatus.REJECTED);
            ticketRepo.save(t);
            
            // Mettre le plan en REFUSED après un rejet de ticket
            if (t.getScheduledOrderId() != null) {
                schedRepo.findById(t.getScheduledOrderId()).ifPresent(plan -> {
                    if (plan.getStatus() == ScheduledOrderStatus.TRIGGERED) {
                        System.out.println("🚫 Mise en REFUSED du plan " + plan.getId() + " après rejet du ticket " + t.getId());
                        plan.setStatus(ScheduledOrderStatus.REFUSED);
                        schedRepo.save(plan);
                    }
                });
            }
            
            audit.log("user:" + t.getPlayerId(), "TICKET_REJECTED", 
                    "ticketId=" + t.getId() + (t.getScheduledOrderId() != null ? ", scheduledOrderId=" + t.getScheduledOrderId() + " (plan mis en REFUSED)" : ""));
            return t;
        }

        BigDecimal q = (quantity != null && quantity.signum() > 0)
                ? quantity
                : t.getSuggestedQuantity();

        var order = engine.placeOrder(
                t.getPlayerId(),
                t.getSymbol(),
                t.getSide(),
                OrderType.LIMIT,
                TimeInForce.DAY,
                q,
                t.getFoundPrice()
        );

        t.setStatus(DecisionStatus.ACCEPTED);
        t.setPlacedOrderId(order.getId());
        ticketRepo.save(t);

        // Si lié à un ScheduledOrder, on marque ce dernier EXECUTED et on stocke l'id de l'ordre
        if (t.getScheduledOrderId() != null) {
            schedRepo.findById(t.getScheduledOrderId()).ifPresent(plan -> {
                if (plan.getStatus() == ScheduledOrderStatus.TRIGGERED || plan.getStatus() == ScheduledOrderStatus.PENDING) {
                    plan.setStatus(ScheduledOrderStatus.EXECUTED);
                    plan.setExecutedOrderId(order.getId());
                    schedRepo.save(plan);
                    notificationService.sendScheduledOrderExecuted(plan, order);
                }
            });
        }

        audit.log("user:" + t.getPlayerId(), "TICKET_ACCEPTED",
                "ticketId=" + t.getId() + ", orderId=" + order.getId() + (t.getScheduledOrderId() != null ? ", scheduledOrderId=" + t.getScheduledOrderId() : ""));

        return t;
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    private boolean isOpen(Order o) {
        return o.getStatus() == OrderStatus.PENDING
                || o.getStatus() == OrderStatus.PARTIALLY_FILLED;
    }

    private boolean inRange(BigDecimal p, BigDecimal min, BigDecimal max) {
        if (p == null) return false;
        if (min != null && p.compareTo(min) < 0) return false;
        if (max != null && p.compareTo(max) > 0) return false;
        return true;
    }

    /** Approche : p ∈ (min, min * (1 + threshold)] */
    private boolean isApproachingMin(BigDecimal price, BigDecimal minPrice, BigDecimal thresholdPct) {
        if (price == null || minPrice == null) return false;
        if (thresholdPct == null) thresholdPct = new BigDecimal("0.05");

        BigDecimal upper = minPrice.multiply(BigDecimal.ONE.add(thresholdPct));
        return price.compareTo(minPrice) > 0 && price.compareTo(upper) <= 0;
    }

    private boolean cooldownOk(LocalDateTime last, Integer minutes) {
        int mm = (minutes == null ? 0 : minutes);
        if (mm <= 0) return true;
        if (last == null) return true;
        return last.plusMinutes(mm).isBefore(LocalDateTime.now());
    }

    // ---- extracteurs depuis le body (pas de DTO) ----

    private static String str(Map<String, Object> m, String k) {
        var v = m.get(k);
        if (v == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " is required");
        return String.valueOf(v);
    }

    private static Long lng(Map<String, Object> m, String k) {
        var v = m.get(k);
        if (v == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " is required");
        try {
            return (v instanceof Number) ? ((Number) v).longValue() : Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " must be a long");
        }
    }

    private static Integer intval(Map<String, Object> m, String k) {
        var v = m.get(k);
        if (v == null) return null;
        try {
            return (v instanceof Number) ? ((Number) v).intValue() : Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " must be an integer");
        }
    }

    private static BigDecimal dec(Map<String, Object> m, String k) {
        var v = m.get(k);
        if (v == null) return null;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, k + " must be a decimal");
        }
    }

    private static boolean bool(Map<String, Object> m, String k, boolean def) {
        var v = m.get(k);
        if (v == null) return def;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
