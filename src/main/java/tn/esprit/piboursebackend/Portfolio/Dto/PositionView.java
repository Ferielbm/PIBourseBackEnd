package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;

public record PositionView(
        Long positionId,
        Long stockId,
        String symbol,
        Integer quantity,
        BigDecimal averagePrice
) {}