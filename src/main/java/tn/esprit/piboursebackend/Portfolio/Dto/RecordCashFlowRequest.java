package tn.esprit.piboursebackend.Portfolio.Dto;

import tn.esprit.piboursebackend.Portfolio.Entity.CashFlowType;

import java.math.BigDecimal;
import java.time.Instant;

public record RecordCashFlowRequest(
        Long portfolioId,
        BigDecimal amount,
        String currency,
        CashFlowType type,
        Instant asOf
) {}

