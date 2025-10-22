package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LotView(
        Long lotId,
        Instant asOf,
        Integer qtyOriginal,
        Integer qtyRemaining,
        BigDecimal price
) {}
