package tn.esprit.piboursebackend.Portfolio.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Portfolio.Entity.Portfolio;
import tn.esprit.piboursebackend.Portfolio.Repository.PortfolioRepository;

import java.time.Instant;
import java.util.List;

@Service
public class PortfolioService {
    private final PortfolioRepository PortfolioRepository;

    public PortfolioService(PortfolioRepository PortfolioRepository) {
        this.PortfolioRepository = PortfolioRepository;
    }

    public List<Portfolio> getAllPortfolios() {
        return PortfolioRepository.findAll();
    }

    public Portfolio createPortfolio(Portfolio Portfolio) {
        return PortfolioRepository.save(Portfolio);
    }

    public void deletePortfolio(Long id) {
        PortfolioRepository.deleteById(id);
    }
    @Transactional
    public Portfolio archivePortfolio(Long portfolioId) {
        var p = PortfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));
        p.setStatus(Portfolio.Status.ARCHIVED);
        p.setUpdatedAt(Instant.now());
        return PortfolioRepository.save(p);
    }

    @Transactional
    public Portfolio clonePortfolio(Long portfolioId, Instant asOf) {
        var src = PortfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        var clone = Portfolio.builder()
                .baseCurrency(src.getBaseCurrency())
                .status(Portfolio.Status.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return PortfolioRepository.save(clone);
    }
}
