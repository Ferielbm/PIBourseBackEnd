package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.piboursebackend.Order.Entity.*;
import tn.esprit.piboursebackend.Order.Repository.DecisionTicketRepository;
import tn.esprit.piboursebackend.Order.Repository.PriceAlertRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository alertRepo;
    private final DecisionTicketRepository ticketRepo;
    private final MatchingEngineService engine;   // pour placer un ordre si un ticket est accepté
    private final AuditLogService audit;          // pour historiser

    // -------- ALERTES --------

    @Transactional
    public PriceAlert createAlert(Long playerId, String symbol, BigDecimal min, BigDecimal max) {
        if (playerId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playerId is required");
        if (symbol == null || symbol.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "symbol is required");
        if (min == null && max == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min or max required");
        if (min != null && max != null && min.compareTo(max) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min must be <= max");

        String actor = "user:" + playerId;

        PriceAlert alert = PriceAlert.builder()
                .playerId(playerId)
                .symbol(symbol.trim().toUpperCase())
                .minPrice(min)
                .maxPrice(max)
                .status(PriceAlertStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        alert = alertRepo.save(alert);

        audit.log(actor, "ALERT_CREATED",
                "alertId=" + alert.getId() + ", " + alert.getSymbol() +
                        (min != null ? ", min=" + min : "") +
                        (max != null ? ", max=" + max : ""));

        return alert;
    }

    @Transactional(readOnly = true)
    public List<PriceAlert> listAlertsForPlayer(Long playerId) {
        if (playerId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playerId is required");
        return alertRepo.findByPlayerIdOrderByCreatedAtDesc(playerId);
    }

    @Transactional
    public void setStatus(Long alertId, PriceAlertStatus status, Long actorPlayerId) {
        if (alertId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "alertId is required");
        if (status == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");

        PriceAlert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "alert not found"));

        alert.setStatus(status);
        alert.setUpdatedAt(LocalDateTime.now());
        alertRepo.save(alert);

        String actor = (actorPlayerId != null) ? "user:" + actorPlayerId : "system";
        audit.log(actor, "ALERT_STATUS_CHANGED",
                "alertId=" + alert.getId() + ", status=" + status.name());
    }

    @Transactional
    public void deleteAlert(Long alertId, Long actorPlayerId) {
        if (alertId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "alertId is required");

        PriceAlert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "alert not found"));

        alertRepo.delete(alert);

        String actor = (actorPlayerId != null) ? "user:" + actorPlayerId : "system";
        audit.log(actor, "ALERT_DELETED", "alertId=" + alertId);
    }

    // -------- TICKETS (inchangé, utilisé par ton système) --------

    @Transactional(readOnly = true)
    public List<DecisionTicket> listTicketsForPlayer(Long playerId) {
        return ticketRepo.findByPlayerIdOrderByCreatedAtDesc(playerId);
    }

    @Transactional(readOnly = true)
    public List<DecisionTicket> listTicketsForPlayerByStatus(Long playerId, DecisionStatus status) {
        return ticketRepo.findByPlayerIdAndStatusOrderByCreatedAtDesc(playerId, status);
    }

    @Transactional
    public DecisionTicket decide(Long ticketId, boolean accept, BigDecimal quantity) {
        DecisionTicket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ticket not found"));

        if (t.getStatus() != DecisionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket not pending");
        }

        String actor = "user:" + t.getPlayerId();

        if (!accept) {
            t.setStatus(DecisionStatus.REJECTED);
            t.setDecidedAt(LocalDateTime.now());
            ticketRepo.save(t);
            audit.log(actor, "TICKET_REJECTED", "ticketId=" + t.getId());
            return t;
        }

        BigDecimal q = (quantity != null && quantity.signum() > 0) ? quantity : t.getSuggestedQuantity();
        if (q == null || q.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be > 0");
        }
        if (t.getFoundPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticket has no price to place order");
        }

        OrderSide side = (t.getSide() != null) ? t.getSide() : OrderSide.BUY;

        var order = engine.placeOrder(
                t.getPlayerId(),
                t.getSymbol(),
                side,
                OrderType.LIMIT,
                TimeInForce.DAY,
                q,
                t.getFoundPrice()
        );

        t.setStatus(DecisionStatus.ACCEPTED);
        t.setDecidedAt(LocalDateTime.now());
        ticketRepo.save(t);

        audit.log(actor, "TICKET_ACCEPTED",
                "ticketId=" + t.getId() + ", orderId=" + order.getId() +
                        ", " + t.getSymbol() + "@" + t.getFoundPrice() + ", qty=" + q);
        return t;
    }

    @Transactional
    public DecisionTicket createTicket(Long playerId,
                                       String symbol,
                                       OrderSide side,
                                       BigDecimal foundPrice,
                                       BigDecimal suggestedQty,
                                       TicketReason reason) {
        if (playerId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playerId is required");
        if (symbol == null || symbol.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "symbol is required");
        if (foundPrice == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "foundPrice is required");

        DecisionTicket t = DecisionTicket.builder()
                .playerId(playerId)
                .symbol(symbol.trim().toUpperCase())
                .side(side != null ? side : OrderSide.BUY)
                .foundPrice(foundPrice)
                .suggestedQuantity(suggestedQty)
                .reason(reason != null ? reason : TicketReason.IN_RANGE)
                .status(DecisionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        t = ticketRepo.save(t);

        audit.log("user:" + playerId, "TICKET_CREATED",
                "ticketId=" + t.getId() + ", " + t.getSymbol() + "@" + foundPrice +
                        (suggestedQty != null ? ", qty=" + suggestedQty : "") +
                        (reason != null ? ", reason=" + reason : ""));

        return t;
    }
}
