package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;

public record PnlResponse(
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl
) {}
