package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record SetTargetWeightsRequest(
        Long portfolioId,
        Map<Long, BigDecimal> weights
) {}