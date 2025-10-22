package tn.esprit.piboursebackend.Portfolio.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tn.esprit.piboursebackend.Portfolio.Entity.*;
import tn.esprit.piboursebackend.Portfolio.Repository.CashBalanceRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.CashFlowRepository;
import tn.esprit.piboursebackend.Portfolio.Repository.PortfolioRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class CashService {

    private final PortfolioRepository portfolioRepo;
    private final CashFlowRepository cashFlowRepo;
    private final CashBalanceRepository cashBalanceRepo;

    public CashService(PortfolioRepository portfolioRepo, CashFlowRepository cashFlowRepo, CashBalanceRepository cashBalanceRepo) {
        this.portfolioRepo = portfolioRepo;
        this.cashFlowRepo = cashFlowRepo;
        this.cashBalanceRepo = cashBalanceRepo;
    }

    @Transactional
    public CashFlow recordCashFlow(Long portfolioId,
                                   BigDecimal amount,
                                   String currency,
                                   CashFlowType type,
                                   Instant asOf) {

        if (portfolioId == null) throw new IllegalArgumentException("portfolioId is required");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (currency == null || currency.length() != 3) throw new IllegalArgumentException("currency must be ISO3");
        if (type == null) throw new IllegalArgumentException("type is required");
        if (asOf == null) throw new IllegalArgumentException("asOf is required");

        var portfolio = portfolioRepo.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));
        BigDecimal signed = switch (type) {
            case WITHDRAWAL, FEE, TAX, TRANSFER_OUT -> amount.negate();
            default -> amount;
        };
        int updated = cashBalanceRepo.addToBalance(portfolioId, currency, signed);
        if (updated == 0) {
            var cb = CashBalance.builder()
                    .portfolio(portfolio)
                    .currency(currency)
                    .balance(signed)
                    .build();
            cashBalanceRepo.save(cb);
        }
        var cf = CashFlow.builder()
                .portfolio(portfolio)
                .asOf(asOf)
                .amount(amount)
                .currency(currency)
                .type(type)
                .build();

        return cashFlowRepo.save(cf);
    }

    @Transactional
    public void transferCashBetween(Long fromPortfolioId,
                                    Long toPortfolioId,
                                    BigDecimal amount,
                                    String currency,
                                    Instant asOf) {
        if (fromPortfolioId == null || toPortfolioId == null) {
            throw new IllegalArgumentException("Both portfolio IDs are required");
        }
        if (fromPortfolioId.equals(toPortfolioId)) {
            throw new IllegalArgumentException("fromPortfolioId and toPortfolioId must differ");
        }

        recordCashFlow(fromPortfolioId, amount, currency, CashFlowType.TRANSFER_OUT, asOf);
        recordCashFlow(toPortfolioId, amount, currency, CashFlowType.TRANSFER_IN, asOf);
    }
}
