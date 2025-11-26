package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeProposal(
        Long stockId,
        String symbol,
        String currency,
        String side,
        BigDecimal qty,
        BigDecimal deltaValueBase,
        BigDecimal deltaWeight
) {}