package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record ProposeRebalanceRequest(
        Long portfolioId,
        Instant asOf,
        Integer toleranceBps  // e.g. 50 = 0.50%
) {}
