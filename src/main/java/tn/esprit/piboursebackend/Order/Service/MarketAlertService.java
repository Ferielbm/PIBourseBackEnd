package tn.esprit.piboursebackend.Order.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Marche.Entity.Stock;
import tn.esprit.piboursebackend.Order.Entity.MarketAlert;
import tn.esprit.piboursebackend.Order.Entity.MarketAlertDto;
import tn.esprit.piboursebackend.Order.Entity.MarketAlertType;
import tn.esprit.piboursebackend.Order.Repository.MarketAlertRepository;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MarketAlertService {

    private final MarketAlertRepository marketAlertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MarketAlert createAndBroadcastBullishAlert(Stock stock,
                                                      BigDecimal totalQty,
                                                      BigDecimal totalValue) {

        String msg = String.format(
                "Le Meneur de Jeu vient d'ACHETER massivement %s (%s unités, valeur %s).",
                stock.getSymbol(),
                totalQty.toPlainString(),
                totalValue.toPlainString()
        );

        MarketAlert alert = MarketAlert.builder()
                .type(MarketAlertType.BULLISH_MJ_BUY)
                .stockSymbol(stock.getSymbol()) // ✅ pas besoin d’un id numérique
                .totalQuantity(totalQty)
                .totalValue(totalValue)
                .message(msg)
                .build();

        MarketAlert saved = marketAlertRepository.save(alert);

        // ✅ Diffusion temps réel à tous les joueurs abonnés
        messagingTemplate.convertAndSend("/topic/market-alerts", toDto(saved));

        return saved;
    }

    public MarketAlert createAndBroadcastBearishAlert(Stock stock,
                                                      BigDecimal totalQty,
                                                      BigDecimal totalValue) {

        String msg = String.format(
                "Le Meneur de Jeu vient de VENDRE massivement %s (%s unités, valeur %s).",
                stock.getSymbol(),
                totalQty.toPlainString(),
                totalValue.toPlainString()
        );

        MarketAlert alert = MarketAlert.builder()
                .type(MarketAlertType.BEARISH_MJ_SELL)
                .stockSymbol(stock.getSymbol()) // ✅ pareil ici
                .totalQuantity(totalQty)
                .totalValue(totalValue)
                .message(msg)
                .build();

        MarketAlert saved = marketAlertRepository.save(alert);

        messagingTemplate.convertAndSend("/topic/market-alerts", toDto(saved));

        return saved;
    }

    private MarketAlertDto toDto(MarketAlert alert) {
        return new MarketAlertDto(
                alert.getType().name(),
                alert.getStockSymbol(),
                alert.getMessage(),
                alert.getCreatedAt()
        );
    }
}