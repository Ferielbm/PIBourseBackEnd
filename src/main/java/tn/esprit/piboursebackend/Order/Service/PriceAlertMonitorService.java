package tn.esprit.piboursebackend.Order.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.Order.Entity.PriceAlert;
import tn.esprit.piboursebackend.Order.Entity.PriceAlertStatus;
import tn.esprit.piboursebackend.Order.Repository.PriceAlertRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour surveiller les alertes de prix et déclencher des notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAlertMonitorService {

    private final PriceAlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditLogService auditLogService;

    /**
     * Vérifie et déclenche les alertes pour un symbole donné
     * Cette méthode est appelée lorsqu'une transaction est exécutée
     */
    @Transactional
    public void checkAlertsForSymbol(String symbol, BigDecimal currentPrice) {
        if (symbol == null || currentPrice == null) {
            log.warn("⚠️ checkAlertsForSymbol appelé avec symbol={} ou currentPrice={} null", symbol, currentPrice);
            return;
        }

        String normalizedSymbol = symbol.toUpperCase().trim();
        log.info("🔍 Recherche des alertes actives pour symbole: {} à prix: {}", normalizedSymbol, currentPrice);
        
        // Récupérer toutes les alertes actives pour ce symbole
        List<PriceAlert> activeAlerts = alertRepository.findBySymbolAndStatus(
            normalizedSymbol, 
            PriceAlertStatus.ACTIVE
        );

        log.info("📊 Trouvé {} alerte(s) active(s) pour {} @ {}", 
            activeAlerts.size(), normalizedSymbol, currentPrice);

        if (activeAlerts.isEmpty()) {
            log.info("ℹ️ Aucune alerte active trouvée pour {}", normalizedSymbol);
        }

        for (PriceAlert alert : activeAlerts) {
            log.debug("🔎 Vérification alerte ID={}, joueur={}, range=[{}, {}]", 
                alert.getId(), alert.getPlayerId(), alert.getMinPrice(), alert.getMaxPrice());
            checkAndTriggerAlert(alert, currentPrice);
        }
    }

    /**
     * Vérifie si une alerte doit être déclenchée
     */
    private void checkAndTriggerAlert(PriceAlert alert, BigDecimal currentPrice) {
        boolean triggered = false;
        String triggeredType = null;

        log.debug("🧪 Test alerte ID={}: min={}, max={}, currentPrice={}", 
            alert.getId(), alert.getMinPrice(), alert.getMaxPrice(), currentPrice);

        // Cas 1: Les deux limites sont définies (min ET max)
        if (alert.getMinPrice() != null && alert.getMaxPrice() != null) {
            // Alerte déclenchée si le prix est DANS l'intervalle [min, max]
            if (currentPrice.compareTo(alert.getMinPrice()) >= 0 && 
                currentPrice.compareTo(alert.getMaxPrice()) <= 0) {
                triggered = true;
                triggeredType = "IN_RANGE";
                log.info("✅ Prix {} est DANS l'intervalle [{}, {}] - ALERTE DÉCLENCHÉE!", 
                    currentPrice, alert.getMinPrice(), alert.getMaxPrice());
            } else {
                log.debug("❌ Prix {} est HORS de l'intervalle [{}, {}]", 
                    currentPrice, alert.getMinPrice(), alert.getMaxPrice());
            }
        }
        // Cas 2: Seulement MIN est défini
        else if (alert.getMinPrice() != null && alert.getMaxPrice() == null) {
            // Alerte déclenchée si le prix atteint ou dépasse le minimum
            if (currentPrice.compareTo(alert.getMinPrice()) >= 0) {
                triggered = true;
                triggeredType = "MIN_REACHED";
                log.info("✅ Prix {    } >= {} - ALERTE MIN_REACHED DÉCLENCHÉE!",
                    currentPrice, alert.getMinPrice());
            }
        }
        // Cas 3: Seulement MAX est défini
        else if (alert.getMaxPrice() != null && alert.getMinPrice() == null) {
            // Alerte déclenchée si le prix atteint ou descend sous le maximum
            if (currentPrice.compareTo(alert.getMaxPrice()) <= 0) {
                triggered = true;
                triggeredType = "MAX_REACHED";
                log.info("✅ Prix {} <= {} - ALERTE MAX_REACHED DÉCLENCHÉE!", 
                    currentPrice, alert.getMaxPrice());
            }
        }

        if (triggered) {
            triggerAlert(alert, currentPrice, triggeredType);
        }
    }

    /**
     * Déclenche une alerte et envoie une notification WebSocket
     */
    private void triggerAlert(PriceAlert alert, BigDecimal currentPrice, String triggeredType) {
        log.info("🚨 Alerte déclenchée: {} pour joueur {} - Prix actuel: {} (type: {})", 
            alert.getSymbol(), alert.getPlayerId(), currentPrice, triggeredType);

        // Créer le payload de notification
        Map<String, Object> payload = new HashMap<>();
        payload.put("alertId", alert.getId());
        payload.put("symbol", alert.getSymbol());
        payload.put("currentPrice", currentPrice);
        payload.put("minPrice", alert.getMinPrice());
        payload.put("maxPrice", alert.getMaxPrice());
        payload.put("triggeredType", triggeredType);
        payload.put("timestamp", LocalDateTime.now());

        // Envoyer la notification WebSocket au joueur
        String destination = "/topic/player/" + alert.getPlayerId() + "/alerts";
        messagingTemplate.convertAndSend(destination, payload);

        log.debug("✅ Notification envoyée sur: {}", destination);

        // Mettre à jour le statut de l'alerte (optionnel: désactiver après déclenchement)
        // Vous pouvez choisir de garder l'alerte active ou de la mettre en pause
        // alert.setStatus(PriceAlertStatus.PAUSED);
        // alert.setUpdatedAt(LocalDateTime.now());
        // alertRepository.save(alert);

        // Enregistrer dans l'audit log
        auditLogService.log(
            "system",
            "ALERT_TRIGGERED",
            String.format("alertId=%d, %s @ %s, type=%s", 
                alert.getId(), alert.getSymbol(), currentPrice, triggeredType)
        );
    }
}
