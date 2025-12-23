package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Order.Entity.DecisionTicket;
import tn.esprit.piboursebackend.Order.Entity.Order;
import tn.esprit.piboursebackend.Order.Entity.ScheduledOrder;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Entities.Role;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerRepository playerRepository;

    public void sendScheduledOrderExecuted(ScheduledOrder plan, Order order) {
        if (plan == null || order == null) return;

        var payload = Map.of(
                "planId", plan.getId(),
                "playerId", plan.getPlayerId(),
                "symbol", plan.getDesiredSymbol(),
                "side", plan.getSide().toString(),
                "quantity", plan.getQuantity(),
                "status", plan.getStatus().toString(),
                "orderId", order.getId(),
                "orderStatus", order.getStatus().toString(),
                "executedPrice", order.getPrice(),
                "executedOrderId", plan.getExecutedOrderId()
        );

        messagingTemplate.convertAndSend(
                "/topic/scheduled-orders/" + plan.getPlayerId(),
                payload
        );

        System.out.println("📡 WS: scheduled order executed, planId=" + plan.getId()
                + ", orderId=" + order.getId());
    }

    public void sendDecisionTicketCreated(DecisionTicket ticket) {
        if (ticket == null) return;

        var payload = Map.of(
                "ticketId", ticket.getId(),
                "playerId", ticket.getPlayerId(),
                "symbol", ticket.getSymbol(),
                "side", ticket.getSide().toString(),
                "reason", ticket.getReason().toString(),
                "status", ticket.getStatus().toString(),
                "foundPrice", ticket.getFoundPrice(),
                "suggestedQuantity", ticket.getSuggestedQuantity()
        );

        messagingTemplate.convertAndSend(
                "/topic/decision-tickets/" + ticket.getPlayerId(),
                payload
        );

        System.out.println("📡 WS: decision ticket created, ticketId=" + ticket.getId());
    }

    /**
     * 🎮 Notifier tous les JOUEURS (pas les meneurs de jeu) d'une action du Game Master
     * Chaque joueur reçoit la notification sur son topic personnel
     */
    public void broadcastGameMasterAction(String actionType, String symbol, String side, 
                                          Long quantity, String message) {
        var payload = Map.of(
                "type", "GAME_MASTER_ACTION",
                "actionType", actionType,
                "symbol", symbol != null ? symbol : "",
                "side", side != null ? side : "",
                "quantity", quantity != null ? quantity : 0,
                "message", message,
                "timestamp", System.currentTimeMillis()
        );

        // Récupérer tous les joueurs (ROLE_PLAYER uniquement), fallback si vide
        List<Player> players = playerRepository.findByRole(Role.ROLE_PLAYER);
        if (players == null || players.isEmpty()) {
            // ⚠️ Fallback dev: envoyer à tous les comptes si aucun joueur ROLE_PLAYER trouvé
            players = playerRepository.findAll();
        }
        
        System.out.println("🎮 DEBUG: Envoi notification Game Master à " + players.size() + " joueur(s)");
        
        // Envoyer la notification à chaque joueur individuellement
        int notificationsSent = 0;
        for (Player player : players) {
            String topic = "/topic/game-master-events/" + player.getId();
            System.out.println("🎮 DEBUG: → Joueur ID=" + player.getId() + " (" + player.getUsername() + ") sur " + topic);
            messagingTemplate.convertAndSend(topic, payload);
            notificationsSent++;
        }

        System.out.println("🎮 WS: Game Master action sent to " + notificationsSent + " player(s) - " + actionType);
    }

    /**
     * 📊 Notifier tous les joueurs d'un changement de prix
     */
    public void broadcastPriceChange(String symbol, Double oldPrice, Double newPrice, 
                                     Double changePercent, String reason) {
        var payload = Map.of(
                "type", "PRICE_CHANGE",
                "symbol", symbol,
                "oldPrice", oldPrice != null ? oldPrice : 0.0,
                "newPrice", newPrice != null ? newPrice : 0.0,
                "changePercent", changePercent != null ? changePercent : 0.0,
                "reason", reason != null ? reason : "Changement de prix",
                "timestamp", System.currentTimeMillis()
        );

        // Envoyer à tous les joueurs
        List<Player> players = playerRepository.findByRole(Role.ROLE_PLAYER);
        if (players == null || players.isEmpty()) {
            players = playerRepository.findAll();
        }
        
        int notificationsSent = 0;
        for (Player player : players) {
            messagingTemplate.convertAndSend(
                "/topic/price-changes/" + player.getId(), 
                payload
            );
            notificationsSent++;
        }

        System.out.println("📊 WS: Price change notification sent to " + notificationsSent + 
                          " player(s) - " + symbol + ": " + oldPrice + " → " + newPrice);
    }

    /**
     * 🌪️ Notifier tous les joueurs d'un événement de marché
     */
    public void broadcastMarketEvent(String eventType, String eventName, String description, 
                                     Integer intensity, List<String> affectedSymbols) {
        var payload = Map.of(
                "type", "MARKET_EVENT",
                "eventType", eventType,
                "eventName", eventName,
                "description", description != null ? description : "",
                "intensity", intensity != null ? intensity : 0,
                "affectedSymbols", affectedSymbols != null ? affectedSymbols : List.of(),
                "timestamp", System.currentTimeMillis()
        );

        // Envoyer à tous les joueurs
        List<Player> players = playerRepository.findByRole(Role.ROLE_PLAYER);
        if (players == null || players.isEmpty()) {
            players = playerRepository.findAll();
        }
        
        int notificationsSent = 0;
        for (Player player : players) {
            messagingTemplate.convertAndSend(
                "/topic/market-events/" + player.getId(), 
                payload
            );
            notificationsSent++;
        }

        System.out.println("🌪️ WS: Market event notification sent to " + notificationsSent + 
                          " player(s) - " + eventType + " (" + eventName + ")");
    }

    /**
     * 🔔 Notifier tous les joueurs d'une alerte générale (système unifié)
     */
    public void broadcastToAllPlayers(String notificationType, String title, String message, 
                                      Map<String, Object> additionalData) {
        var payload = Map.of(
                "type", notificationType,
                "title", title,
                "message", message,
                "data", additionalData != null ? additionalData : Map.of(),
                "timestamp", System.currentTimeMillis()
        );

        // Envoyer à tous les joueurs
        List<Player> players = playerRepository.findByRole(Role.ROLE_PLAYER);
        if (players == null || players.isEmpty()) {
            players = playerRepository.findAll();
        }
        
        int notificationsSent = 0;
        for (Player player : players) {
            messagingTemplate.convertAndSend(
                "/topic/notifications/" + player.getId(), 
                payload
            );
            notificationsSent++;
        }

        System.out.println("🔔 WS: General notification sent to " + notificationsSent + 
                          " player(s) - " + notificationType);
    }
}
