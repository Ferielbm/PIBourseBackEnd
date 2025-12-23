package tn.esprit.piboursebackend.GameMaster.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.GameMaster.Service.GameMasterService;
import tn.esprit.piboursebackend.GameMaster.DTO.*;
import tn.esprit.piboursebackend.Player.Services.PlayerActivityService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/game-master")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameMasterController {

    private final GameMasterService gameMasterService;
    private final PlayerActivityService playerActivityService;

    /**
     * Injecter un ordre fictif massif dans le carnet d'ordres
     */
    @PostMapping("/inject-order")
    public ResponseEntity<?> injectFictiveOrder(@RequestBody FictiveOrderRequest request) {
        log.info("🎮 Injection d'ordre fictif: {} {} {} @ {}",
                request.getSide(), request.getQuantity(), request.getSymbol(),
                request.getPriceType());

        try {
            gameMasterService.injectFictiveOrder(request);
            
            // Déclencher le matching APRÈS que l'ordre soit sauvegardé
            // (dans une transaction séparée pour éviter rollback)
            gameMasterService.triggerMatchingAfterOrder(request.getSymbol());
            
            return ResponseEntity.ok().body(
                    new MessageResponse("Ordre fictif créé avec succès")
            );
        } catch (Exception e) {
            log.error("❌ Erreur injection ordre fictif:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }

    /**
     * Déclencher un événement de marché
     */
    @PostMapping("/trigger-event")
    public ResponseEntity<?> triggerMarketEvent(@RequestBody MarketEventRequest request) {
        log.info("🎮 Déclenchement événement: {} (intensité: {}%)",
                request.getEventType(), request.getIntensity());

        try {
            gameMasterService.triggerMarketEvent(request);
            return ResponseEntity.ok().body(
                    new MessageResponse("Événement déclenché avec succès")
            );
        } catch (Exception e) {
            log.error("❌ Erreur déclenchement événement:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }

    /**
     * Annuler tous les ordres d'un symbole
     */
    @DeleteMapping("/orders/{symbol}")
    public ResponseEntity<?> cancelAllOrders(@PathVariable String symbol) {
        log.info("🎮 Annulation de tous les ordres pour: {}", symbol);

        try {
            int count = gameMasterService.cancelAllOrdersForSymbol(symbol);
            return ResponseEntity.ok().body(
                    new MessageResponse("Annulé " + count + " ordre(s)")
            );
        } catch (Exception e) {
            log.error("❌ Erreur annulation ordres:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }

    /**
     * Obtenir les statistiques du marché
     */
    @GetMapping("/stats")
    public ResponseEntity<MarketStatsResponse> getMarketStats() {
        try {
            MarketStatsResponse stats = gameMasterService.getMarketStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Erreur récupération stats:", e);
            return ResponseEntity.ok(new MarketStatsResponse(0, 0L, 0, ""));
        }
    }

    /**
     * Forcer l'exécution du matching engine
     */
    @PostMapping("/force-matching/{symbol}")
    public ResponseEntity<?> forceMatching(@PathVariable String symbol) {
        log.info("🎮 Force matching pour: {}", symbol);

        try {
            gameMasterService.forceMatchingForSymbol(symbol);
            return ResponseEntity.ok().body(
                    new MessageResponse("Matching forcé pour " + symbol)
            );
        } catch (Exception e) {
            log.error("❌ Erreur force matching:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }

    /**
     * Réinitialiser le marché (supprimer tous les ordres)
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetMarket() {
        log.warn("🎮 ⚠️ RÉINITIALISATION DU MARCHÉ");

        try {
            gameMasterService.resetMarket();
            return ResponseEntity.ok().body(
                    new MessageResponse("Marché réinitialisé")
            );
        } catch (Exception e) {
            log.error("❌ Erreur réinitialisation:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }

    /**
     * Obtenir l'historique des prix pour un symbole
     */
    @GetMapping("/price-history/{stockSymbol}")
    public ResponseEntity<?> getPriceHistory(@PathVariable String stockSymbol) {
        log.info("📊 Récupération historique prix pour: {}", stockSymbol);

        try {
            var history = gameMasterService.getPriceHistory(stockSymbol);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("❌ Erreur récupération historique:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }

    /**
     * Mettre à jour le prix d'un stock
     */
    @PostMapping("/update-price")
    public ResponseEntity<?> updatePrice(@RequestBody Map<String, Object> request) {
        log.info("💰 Mise à jour prix: {} -> {} ({})",
                request.get("stockSymbol"), request.get("newPrice"), request.get("reason"));

        try {
            gameMasterService.updateStockPrice(request);
            return ResponseEntity.ok().body(
                    new MessageResponse("Prix mis à jour avec succès")
            );
        } catch (Exception e) {
            log.error("❌ Erreur mise à jour prix:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }

    /**
     * Obtenir la liste des joueurs actifs (connectés récemment)
     * NOTE: Fallback - retourne tous les joueurs pour le moment car l'activité tracking n'est pas implémenté
     */
    @GetMapping("/active-players")
    public ResponseEntity<?> getActivePlayers() {
        try {
            log.info("🔍 Tentative de récupération des joueurs actifs via PlayerActivityService...");
            List<PlayerActivityService.PlayerActivityDTO> activePlayers = playerActivityService.getActivePlayers();
            
            if (activePlayers.isEmpty()) {
                log.warn("⚠️  Aucun joueur trouvé via PlayerActivity, tentative fallback via getAllPlayers()...");
                // FALLBACK: Récupérer tous les joueurs 
                activePlayers = gameMasterService.getAllPlayersAsActivityDTOs();
                log.info("✅ Fallback réussi: {} joueurs retournés", activePlayers.size());
            }
            
            return ResponseEntity.ok(Map.of(
                    "count", activePlayers.size(),
                    "players", activePlayers
            ));
        } catch (Exception e) {
            log.error("❌ Erreur récupération joueurs actifs:", e);
            return ResponseEntity.ok(Map.of(
                    "count", 0,
                    "players", List.of()
            ));
        }
    }

    /**
     * 🔔 Envoyer une notification personnalisée à tous les joueurs
     */
    @PostMapping("/broadcast-notification")
    public ResponseEntity<?> broadcastNotification(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            
            if (type == null || title == null || message == null) {
                return ResponseEntity.badRequest().body(
                        new MessageResponse("Type, title et message sont requis")
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> additionalData = (Map<String, Object>) request.getOrDefault("data", Map.of());
            
            gameMasterService.broadcastNotificationToAllPlayers(type, title, message, additionalData);
            
            return ResponseEntity.ok().body(
                    new MessageResponse("Notification envoyée à tous les joueurs")
            );
        } catch (Exception e) {
            log.error("❌ Erreur envoi notification:", e);
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Erreur: " + e.getMessage())
            );
        }
    }
}
