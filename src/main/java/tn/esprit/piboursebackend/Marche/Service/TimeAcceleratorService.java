package tn.esprit.piboursebackend.Marche.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.Market;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Marche.Entity.PriceHistory;
import tn.esprit.piboursebackend.Marche.Repository.MarketRepository;
import tn.esprit.piboursebackend.Marche.Repository.StockRepository;
import tn.esprit.piboursebackend.Marche.Repository.PriceHistoryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeAcceleratorService {

    private final MarketRepository marketRepository;
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    private LocalDateTime gameStartTime;
    private LocalDateTime currentGameTime;
    private boolean timeAccelerationActive = false;
    private final BigDecimal TIME_COMPRESSION_RATIO = new BigDecimal("2016.0"); // 12 mois = 1 heure

    // Configuration du temps
    public void startTimeAcceleration() {
        this.gameStartTime = LocalDateTime.of(2023, 1, 1, 9, 30);
        this.currentGameTime = gameStartTime;
        this.timeAccelerationActive = true;

        System.out.println("⏰ Time acceleration started: 12 months = 1 hour");
        System.out.println("Game time: " + currentGameTime);
        System.out.println("Real time: " + LocalDateTime.now());
    }

    public void stopTimeAcceleration() {
        this.timeAccelerationActive = false;
        System.out.println("⏹️ Time acceleration stopped");
    }

    @Scheduled(fixedRate = 1000) // Exécuté toutes les secondes
    public void advanceGameTime() {
        if (!timeAccelerationActive) return;

        // Calcul du temps écoulé en temps réel
        long realTimeElapsedSeconds = 1; // 1 seconde réelle

        // Conversion en temps de jeu (12 mois = 1 heure réelle)
        BigDecimal gameTimeElapsedSeconds = new BigDecimal(realTimeElapsedSeconds)
                .multiply(TIME_COMPRESSION_RATIO);

        // Avancer le temps de jeu
        currentGameTime = currentGameTime.plusSeconds(gameTimeElapsedSeconds.longValue());

        // Mettre à jour les marchés
        updateMarketsWithCurrentTime();

        // Mettre à jour les prix des actions
        updateStockPrices();

        // Log toutes les 10 secondes réelles
        if (System.currentTimeMillis() % 10000 < 1000) {
            System.out.println("🕐 Game Time: " + currentGameTime +
                    " | Real Time: " + LocalDateTime.now().toLocalTime());
        }

        // Vérifier si l'année 2023 est terminée
        if (currentGameTime.getYear() > 2023) {
            stopTimeAcceleration();
            System.out.println("🎯 Game completed! 2023 simulation finished in ~1 hour");
        }
    }

    private void updateMarketsWithCurrentTime() {
        List<Market> markets = marketRepository.findAll();
        for (Market market : markets) {
            market.setCurrentDate(currentGameTime);

            // Gérer l'ouverture/fermeture du marché selon l'heure de jeu
            boolean isMarketOpen = isMarketOpen(currentGameTime);
            market.setIsOpen(isMarketOpen);

            marketRepository.save(market);
        }
    }

    private void updateStockPrices() {
        if (!isMarketOpen(currentGameTime)) return;

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            // Trouver le prix historique correspondant à la date actuelle du jeu
            Optional<PriceHistory> currentPriceData = priceHistoryRepository
                    .findBySymbolAndDate(stock.getSymbol(), currentGameTime);

            if (currentPriceData.isPresent()) {
                PriceHistory priceData = currentPriceData.get();

                // Mettre à jour le prix courant de l'action
                stock.setCurrentPrice(priceData.getClosePrice());
                stockRepository.save(stock);

                // Mettre à jour l'OrderBook si nécessaire
                updateOrderBookPrice(stock, priceData.getClosePrice());
            }
        }
    }

    private void updateOrderBookPrice(Stock stock, BigDecimal newPrice) {
        // Ici vous pouvez mettre à jour l'OrderBook avec le nouveau prix
        // Cette méthode sera implémentée quand on ajoutera le trading
    }

    private boolean isMarketOpen(LocalDateTime gameTime) {
        int hour = gameTime.getHour();
        int dayOfWeek = gameTime.getDayOfWeek().getValue();

        // Marché ouvert du lundi au vendredi, 9h30 à 16h00
        return dayOfWeek >= 1 && dayOfWeek <= 5 &&
                hour >= 9 && (hour < 16 || (hour == 16 && gameTime.getMinute() == 0));
    }

    // Méthodes pour le contrôle manuel
    public LocalDateTime getCurrentGameTime() {
        return currentGameTime;
    }

    public boolean isTimeAccelerationActive() {
        return timeAccelerationActive;
    }

    public String getTimeCompressionInfo() {
        long realSeconds = 1;
        long gameSeconds = TIME_COMPRESSION_RATIO.longValue();

        return String.format("⏱️ Time Compression: 1 real second = %d game seconds (%d real minutes = 1 game year)",
                gameSeconds, gameSeconds / 60);
    }

    public void setGameTime(LocalDateTime newGameTime) {
        this.currentGameTime = newGameTime;
        updateMarketsWithCurrentTime();
        System.out.println("🕐 Game time manually set to: " + currentGameTime);
    }
}