package tn.esprit.piboursebackend.Portfolio.Dto;

import java.time.Instant;

public record ApplyModelPortfolioRequest(
        Long portfolioId,
        Long modelId,
        Instant asOf,
        Integer toleranceBps
) {}