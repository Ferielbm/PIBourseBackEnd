package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeProposal(
        Long stockId,
        String symbol,
        String currency,
        String side,          // BUY / SELL / NONE
        BigDecimal qty,       // integer qty (rounded)
        BigDecimal deltaValueBase,
        BigDecimal deltaWeight // target - current
) {}