package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.util.List;

public record NavBreakdown(
        BigDecimal nav, BigDecimal cashBase, BigDecimal marketValueTotal,
        List<Line> positions
) {
    public record Line(Long positionId, String symbol, String ccy,
                       BigDecimal qty, BigDecimal priceCcy, BigDecimal fxToBase, BigDecimal mvBase) {}
}
