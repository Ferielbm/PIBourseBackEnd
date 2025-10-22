package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferCashRequest(
        Long fromPortfolioId,
        Long toPortfolioId,
        BigDecimal amount,
        String currency,
        Instant asOf
) {}

