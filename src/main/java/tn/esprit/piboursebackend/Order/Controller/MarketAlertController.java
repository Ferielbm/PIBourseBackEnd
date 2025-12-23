package tn.esprit.piboursebackend.Order.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Order.Entity.MarketAlert;
import tn.esprit.piboursebackend.Order.Entity.MarketAlertDto;
import tn.esprit.piboursebackend.Order.Repository.MarketAlertRepository;


import java.util.List;

@RestController
@RequestMapping("/api/market-alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // si tu veux appeler depuis Angular
public class MarketAlertController {

    private final MarketAlertRepository marketAlertRepository;

    // Récupérer toutes les alertes (le plus récent d'abord)
    @GetMapping
    public List<MarketAlertDto> getAllAlerts() {
        List<MarketAlert> alerts = marketAlertRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return alerts.stream()
                .map(a -> new MarketAlertDto(
                        a.getType().name(),
                        a.getStockSymbol(),
                        a.getMessage(),
                        a.getCreatedAt()
                ))
                .toList();
    }

    // Optionnel : récupérer les alertes d'un titre spécifique
    @GetMapping("/stock/{symbol}")
    public List<MarketAlertDto> getAlertsByStock(@PathVariable String symbol) {
        List<MarketAlert> alerts = marketAlertRepository
                .findByStockSymbolOrderByCreatedAtDesc(symbol);

        return alerts.stream()
                .map(a -> new MarketAlertDto(
                        a.getType().name(),
                        a.getStockSymbol(),
                        a.getMessage(),
                        a.getCreatedAt()
                ))
                .toList();
    }
}