package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FillRequest(
        Long portfolioId,
        Long stockId,
        Side side,
        Integer quantity,
        BigDecimal price,
        Instant asOf
) {}

