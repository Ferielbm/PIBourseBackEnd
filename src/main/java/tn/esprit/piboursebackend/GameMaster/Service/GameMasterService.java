package tn.esprit.piboursebackend.GameMaster.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.piboursebackend.GameMaster.DTO.*;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;
import tn.esprit.piboursebackend.Order.Entity.*;
import tn.esprit.piboursebackend.Order.Repository.OrderRepository;
import tn.esprit.piboursebackend.Order.Service.MatchingEngineService;
import tn.esprit.piboursebackend.Player.Entities.Player;
import tn.esprit.piboursebackend.Player.Repositories.PlayerRepository;
import tn.esprit.piboursebackend.Player.Services.PlayerActivityService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameMasterService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final PlayerRepository playerRepository;
    private final MatchingEngineService matchingEngineService;
    private final tn.esprit.piboursebackend.Order.Service.NotificationService notificationService;
    private final PlayerActivityService playerActivityService;

    // ID fictif du joueur "Game Master" (créé au démarrage par DataInitializer)
    private static final Long GAME_MASTER_PLAYER_ID = 999L;

    /**
     * Injecter un ordre fictif massif
     */
    @Transactional
    public void injectFictiveOrder(FictiveOrderRequest request) {
        log.info("💉 Injection ordre fictif: {} {} {}",
                request.getSide(), request.getQuantity(), request.getSymbol());

        // Récupérer le stock
        Stock stock = stockRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbole introuvable: " + request.getSymbol()));

        // Déterminer le prix
        BigDecimal orderPrice = null;
        OrderType orderType;

        if ("MARKET".equals(request.getPriceType())) {
            orderType = OrderType.MARKET;
            orderPrice = null; // Prix au marché
        } else {
            orderType = OrderType.LIMIT;
            if (request.getLimitPrice() == null || request.getLimitPrice() <= 0) {
                throw new RuntimeException("Prix limite invalide");
            }
            orderPrice = BigDecimal.valueOf(request.getLimitPrice());
        }

        // Créer l'ordre
        Order order = Order.builder()
                .playerId(GAME_MASTER_PLAYER_ID)
                .stock(stock)
                .type(orderType)
                .side(OrderSide.valueOf(request.getSide().toUpperCase()))
                .tif(TimeInForce.GTC) // Good Till Cancelled
                .status(OrderStatus.PENDING)
                .quantity(BigDecimal.valueOf(request.getQuantity()))
                .remainingQuantity(BigDecimal.valueOf(request.getQuantity()))
                .price(orderPrice)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        orderRepository.flush(); // Forcer la sauvegarde immédiate
        log.info("✅ Ordre fictif créé: ID={}", order.getId());

        // 🔔 Notifier tous les joueurs
        String sideText = request.getSide().equals("BUY") ? "ACHAT" : "VENTE";
        String actionMessage = String.format(
            "🚨 Alerte Marché! Un investisseur institutionnel vient de placer un ordre massif de %s actions %s à %s! %s",
            request.getQuantity(),
            request.getSymbol(),
            sideText,
            request.getSide().equals("BUY") ? "Prix en hausse attendue 📈" : "Attention à la baisse 📉"
        );
        
        try {
            notificationService.broadcastGameMasterAction(
                "MASSIVE_ORDER",
                request.getSymbol(),
                request.getSide(),
                (long) request.getQuantity(),
                actionMessage
            );
        } catch (Exception e) {
            log.error("⚠️ Erreur notification:", e);
            // Ne pas faire échouer la transaction si notification échoue
        }
    }
    
    /**
     * Déclencher le matching après création d'ordre (séparé pour éviter rollback)
     */
    public void triggerMatchingAfterOrder(String symbol) {
        try {
            matchingEngineService.matchSymbol(symbol);
            log.info("✅ Matching déclenché pour {}", symbol);
        } catch (Exception e) {
            log.error("⚠️ Erreur lors du matching pour {}: {}", symbol, e.getMessage(), e);
            // Le matching peut échouer sans problème, l'ordre reste PENDING
        }
    }

    /**
     * Déclencher un événement de marché
     */
    @Transactional
    public void triggerMarketEvent(MarketEventRequest request) {
        log.info("🔥 Événement: {} (intensité: {}%)",
                request.getEventType(), request.getIntensity());

        List<Stock> targetStocks;
        if (request.getSymbol() != null && !request.getSymbol().isEmpty()) {
            // Cibler un symbole spécifique
            Stock stock = stockRepository.findBySymbol(request.getSymbol().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Symbole introuvable"));
            targetStocks = List.of(stock);
        } else {
            // Tous les symboles
            targetStocks = stockRepository.findAll();
        }

        // Calculer le nombre d'ordres basé sur l'intensité
        int baseOrdersPerSymbol = Math.max(1, request.getIntensity() / 10);

        String eventMessage = "";
        String eventName = "";
        String description = "";
        List<String> affectedSymbols = targetStocks.stream()
                .map(Stock::getSymbol)
                .collect(java.util.stream.Collectors.toList());

        switch (request.getEventType()) {
            case "CRASH":
                triggerCrash(targetStocks, baseOrdersPerSymbol);
                eventName = "CRASH BOURSIER";
                eventMessage = "📉 ALERTE CRASH BOURSIER! Ventes massives en cours. Opportunité d'achat à bas prix!";
                description = "Ventes massives détectées. Les prix chutent rapidement. C'est peut-être le moment d'acheter à bas prix!";
                break;
            case "BULL_RUN":
                triggerBullRun(targetStocks, baseOrdersPerSymbol);
                eventName = "BULL RUN";
                eventMessage = "📈 BULL RUN DÉCLENCHÉ! Achats massifs détectés. Les prix explosent!";
                description = "Achats massifs en cours. Les prix montent rapidement. Attention à la survalorisation!";
                break;
            case "VOLATILITY":
                triggerVolatility(targetStocks, baseOrdersPerSymbol);
                eventName = "VOLATILITÉ EXTRÊME";
                eventMessage = "⚡ VOLATILITÉ EXTRÊME! Marché très agité. Risques et opportunités!";
                description = "Marché très instable avec des variations de prix importantes. Opportunités mais aussi risques élevés!";
                break;
            case "MANIPULATION":
                triggerManipulation(targetStocks, baseOrdersPerSymbol);
                eventName = "ACTIVITÉ SUSPECTE";
                eventMessage = "🎯 Activité suspecte détectée sur le marché. Soyez vigilants!";
                description = "Mouvements de prix inhabituels détectés. Possibilité de manipulation de marché.";
                break;
            default:
                throw new RuntimeException("Type d'événement inconnu: " + request.getEventType());
        }

        // 🔔 Notifier tous les joueurs de l'événement de marché
        notificationService.broadcastMarketEvent(
            request.getEventType(),
            eventName,
            description,
            request.getIntensity(),
            affectedSymbols
        );

        // 🔔 Notifier aussi via l'ancien système
        notificationService.broadcastGameMasterAction(
            request.getEventType(),
            request.getSymbol(),
            null,
            null,
            eventMessage
        );
    }

    /**
     * Crash boursier: ventes massives
     */
    private void triggerCrash(List<Stock> stocks, int ordersPerSymbol) {
        log.warn("📉 CRASH BOURSIER DÉCLENCHÉ");

        for (Stock stock : stocks) {
            for (int i = 0; i < ordersPerSymbol; i++) {
                BigDecimal quantity = BigDecimal.valueOf(500L + (long)(Math.random() * 2000));
                BigDecimal price = stock.getCurrentPrice().multiply(BigDecimal.valueOf(0.90 - Math.random() * 0.15)); // -10% à -25%

                Order order = Order.builder()
                        .playerId(GAME_MASTER_PLAYER_ID)
                        .stock(stock)
                        .type(OrderType.LIMIT)
                        .side(OrderSide.SELL)
                        .tif(TimeInForce.GTC)
                        .status(OrderStatus.PENDING)
                        .quantity(quantity)
                        .remainingQuantity(quantity)
                        .price(price)
                        .createdAt(LocalDateTime.now())
                        .build();

                orderRepository.save(order);
            }
            // Matching
            try {
                matchingEngineService.matchSymbol(stock.getSymbol());
            } catch (Exception e) {
                log.error("Erreur matching:", e);
            }
        }
    }

    /**
     * Bull Run: achats massifs
     */
    private void triggerBullRun(List<Stock> stocks, int ordersPerSymbol) {
        log.info("📈 BULL RUN DÉCLENCHÉ");

        for (Stock stock : stocks) {
            for (int i = 0; i < ordersPerSymbol; i++) {
                BigDecimal quantity = BigDecimal.valueOf(500L + (long)(Math.random() * 2000));
                BigDecimal price = stock.getCurrentPrice().multiply(BigDecimal.valueOf(1.10 + Math.random() * 0.15)); // +10% à +25%

                Order order = Order.builder()
                        .playerId(GAME_MASTER_PLAYER_ID)
                        .stock(stock)
                        .type(OrderType.LIMIT)
                        .side(OrderSide.BUY)
                        .tif(TimeInForce.GTC)
                        .status(OrderStatus.PENDING)
                        .quantity(quantity)
                        .remainingQuantity(quantity)
                        .price(price)
                        .createdAt(LocalDateTime.now())
                        .build();

                orderRepository.save(order);
            }
            // Matching
            try {
                matchingEngineService.matchSymbol(stock.getSymbol());
            } catch (Exception e) {
                log.error("Erreur matching:", e);
            }
        }
    }

    /**
     * Volatilité: ordres aléatoires
     */
    private void triggerVolatility(List<Stock> stocks, int ordersPerSymbol) {
        log.info("⚡ VOLATILITÉ EXTRÊME DÉCLENCHÉE");

        for (Stock stock : stocks) {
            for (int i = 0; i < ordersPerSymbol * 2; i++) {
                BigDecimal quantity = BigDecimal.valueOf(200L + (long)(Math.random() * 1000));
                OrderSide side = Math.random() > 0.5 ? OrderSide.BUY : OrderSide.SELL;
                Double priceVariation = 0.95 + Math.random() * 0.10; // -5% à +5%
                BigDecimal price = stock.getCurrentPrice().multiply(BigDecimal.valueOf(priceVariation));

                Order order = Order.builder()
                        .playerId(GAME_MASTER_PLAYER_ID)
                        .stock(stock)
                        .type(OrderType.LIMIT)
                        .side(side)
                        .tif(TimeInForce.GTC)
                        .status(OrderStatus.PENDING)
                        .quantity(quantity)
                        .remainingQuantity(quantity)
                        .price(price)
                        .createdAt(LocalDateTime.now())
                        .build();

                orderRepository.save(order);
            }
            // Matching
            try {
                matchingEngineService.matchSymbol(stock.getSymbol());
            } catch (Exception e) {
                log.error("Erreur matching:", e);
            }
        }
    }

    /**
     * Manipulation: cibler un symbole avec beaucoup d'ordres
     */
    private void triggerManipulation(List<Stock> stocks, int ordersPerSymbol) {
        log.warn("🎯 MANIPULATION DE MARCHÉ DÉCLENCHÉE");

        for (Stock stock : stocks) {
            // Créer beaucoup d'ordres à des prix variés pour manipuler
            for (int i = 0; i < ordersPerSymbol * 3; i++) {
                BigDecimal quantity = BigDecimal.valueOf(100L + (long)(Math.random() * 500));
                OrderSide side = i % 2 == 0 ? OrderSide.BUY : OrderSide.SELL;
                Double priceVariation = 0.98 + Math.random() * 0.04; // -2% à +2%
                BigDecimal price = stock.getCurrentPrice().multiply(BigDecimal.valueOf(priceVariation));

                Order order = Order.builder()
                        .playerId(GAME_MASTER_PLAYER_ID)
                        .stock(stock)
                        .type(OrderType.LIMIT)
                        .side(side)
                        .tif(TimeInForce.GTC)
                        .status(OrderStatus.PENDING)
                        .quantity(quantity)
                        .remainingQuantity(quantity)
                        .price(price)
                        .createdAt(LocalDateTime.now())
                        .build();

                orderRepository.save(order);
            }
            // Matching
            try {
                matchingEngineService.matchSymbol(stock.getSymbol());
            } catch (Exception e) {
                log.error("Erreur matching:", e);
            }
        }
    }

    /**
     * Annuler tous les ordres d'un symbole
     */
    @Transactional
    public int cancelAllOrdersForSymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbole introuvable"));

        List<Order> pendingOrders = orderRepository.findByStockAndStatus(stock, OrderStatus.PENDING);
        
        for (Order order : pendingOrders) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        
        orderRepository.saveAll(pendingOrders);
        log.info("🗑️ Annulé {} ordre(s) pour {}", pendingOrders.size(), symbol);
        
        return pendingOrders.size();
    }

    /**
     * Obtenir les statistiques du marché
     */
    public MarketStatsResponse getMarketStats() {
        // Total des ordres actifs
        int totalOrders = (int) orderRepository.countByStatus(OrderStatus.PENDING);

        // Volume total (somme des quantités)
        Long totalVolume = orderRepository.sumQuantityByStatus(OrderStatus.PENDING);
        if (totalVolume == null) totalVolume = 0L;

        // Joueurs actifs (depuis PlayerActivity - connectés dans les 15 dernières minutes)
        int activePlayers = playerActivityService.countActivePlayers().intValue();

        // Symbole le plus échangé
        String mostTraded = orderRepository.findMostTradedSymbol();

        return MarketStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalVolume(totalVolume)
                .activePlayers(activePlayers)
                .mostTradedSymbol(mostTraded != null ? mostTraded : "")
                .build();
    }

    /**
     * Forcer le matching pour un symbole
     */
    @Transactional
    public void forceMatchingForSymbol(String symbol) {
        matchingEngineService.matchSymbol(symbol);
        log.info("✅ Matching forcé pour {}", symbol);
    }

    /**
     * Obtenir l'historique des prix pour un symbole
     */
    public List<Map<String, Object>> getPriceHistory(String stockSymbol) {
        Stock stock = stockRepository.findBySymbol(stockSymbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbole introuvable: " + stockSymbol));

        return stock.getPriceHistoryList().stream()
                .sorted((p1, p2) -> p2.getDateTime().compareTo(p1.getDateTime())) // Plus récent d'abord
                .map(ph -> {
                    BigDecimal changePercent = BigDecimal.ZERO;
                    if (ph.getOpenPrice() != null && ph.getOpenPrice().compareTo(BigDecimal.ZERO) > 0) {
                        changePercent = ph.getClosePrice()
                                .subtract(ph.getOpenPrice())
                                .divide(ph.getOpenPrice(), 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                    }

                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", ph.getId());
                    dto.put("stockSymbol", stock.getSymbol());
                    dto.put("price", ph.getClosePrice());
                    dto.put("timestamp", ph.getDateTime());
                    dto.put("changePercent", changePercent);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le prix d'un stock et créer une entrée dans l'historique
     */
    @Transactional
    public void updateStockPrice(Map<String, Object> request) {
        String stockSymbol = (String) request.get("stockSymbol");
        BigDecimal newPrice = new BigDecimal(request.get("newPrice").toString());
        String reason = (String) request.get("reason");

        Stock stock = stockRepository.findBySymbol(stockSymbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbole introuvable: " + stockSymbol));

        BigDecimal oldPrice = stock.getCurrentPrice();

        log.info("💰 Mise à jour prix {} : {} -> {}", stock.getSymbol(), oldPrice, newPrice);

        // Mettre à jour le prix actuel
        stock.setCurrentPrice(newPrice);

        // Créer une entrée dans l'historique
        tn.esprit.piboursebackend.Marche.Entity.PriceHistory priceHistory = 
            tn.esprit.piboursebackend.Marche.Entity.PriceHistory.builder()
                .stock(stock)
                .dateTime(LocalDateTime.now())
                .openPrice(oldPrice)
                .closePrice(newPrice)
                .highPrice(oldPrice.max(newPrice))
                .lowPrice(oldPrice.min(newPrice))
                .volume(0L) // Pas de volume pour une modification manuelle
                .createdAt(LocalDateTime.now())
                .build();

        stock.getPriceHistoryList().add(priceHistory);
        stockRepository.save(stock);

        // Notifier tous les joueurs du changement de prix
        BigDecimal changePercent = newPrice.subtract(oldPrice)
                .divide(oldPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        String message = String.format(
            "💰 Le prix de %s a été ajusté par le meneur de jeu : %s € → %s € (%+.2f%%). Raison: %s",
            stock.getSymbol(),
            oldPrice,
            newPrice,
            changePercent,
            reason
        );

        try {
            notificationService.broadcastGameMasterAction(
                "PRICE_UPDATE",
                stock.getSymbol(),
                changePercent.compareTo(BigDecimal.ZERO) > 0 ? "UP" : "DOWN",
                newPrice.longValue(),
                message
            );
        } catch (Exception e) {
            log.error("⚠️ Erreur notification changement prix:", e);
        }

        log.info("✅ Prix mis à jour et historique créé");
    }

    /**
     * 🔔 Diffuser une notification personnalisée à tous les joueurs
     */
    public void broadcastNotificationToAllPlayers(String type, String title, String message, 
                                                   Map<String, Object> additionalData) {
        log.info("📢 Envoi notification à tous les joueurs: {} - {}", type, title);
        notificationService.broadcastToAllPlayers(type, title, message, additionalData);
    }

    /**
     * Réinitialiser le marché
     */
    @Transactional
    public void resetMarket() {
        log.warn("⚠️ RÉINITIALISATION DU MARCHÉ");
        
        // Annuler tous les ordres PENDING
        List<Order> allPending = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : allPending) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.saveAll(allPending);
        
        log.info("✅ {} ordres annulés", allPending.size());
    }

    /**
     * Fallback: Récupérer tous les joueurs en tant que PlayerActivityDTOs
     * Utilisé quand PlayerActivity n'a pas de données
     */
    public List<PlayerActivityService.PlayerActivityDTO> getAllPlayersAsActivityDTOs() {
        log.info("🔍 Fallback: Récupération de TOUS les joueurs pour getActivePlayers()");
        
        List<Player> allPlayers = playerRepository.findAll();
        log.info("📊 Total de joueurs trouvés: {}", allPlayers.size());
        
        return allPlayers.stream()
                .map(player -> PlayerActivityService.PlayerActivityDTO.builder()
                        .playerId(player.getId())
                        .username(player.getUsername())
                        .email(player.getEmail())
                        .role(player.getRole())
                        .lastLogin(null)
                        .lastActivity(null)
                        .isActive(true) // Assumon qu'ils sont actifs pour le fallback
                        .build())
                .collect(Collectors.toList());
    }
}
