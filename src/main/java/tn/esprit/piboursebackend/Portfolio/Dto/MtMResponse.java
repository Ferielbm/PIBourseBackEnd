package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;

public record MtMResponse(
        Long positionId,
        Integer quantity,
        BigDecimal averagePrice,
        BigDecimal marketPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedPnl
) {}
