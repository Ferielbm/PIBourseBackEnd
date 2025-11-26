package tn.esprit.piboursebackend.Portfolio.Dto;

import java.math.BigDecimal;
import java.time.Instant;

public class FillRequest {
    private Long portfolioId;
    private Long stockId;
    private Side side;
    private Integer quantity;
    private BigDecimal price;
    private Instant asOf;
    private String externalTradeId;
    private String externalOrderId;
    private String currency;

    public FillRequest(Long portfolioId, Long stockId, Side side, Integer quantity,
                       BigDecimal price, Instant asOf, String externalTradeId,
                       String externalOrderId, String currency) {
        this.portfolioId = portfolioId;
        this.stockId = stockId;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.asOf = asOf;
        this.externalTradeId = externalTradeId;
        this.externalOrderId = externalOrderId;
        this.currency = currency;
    }

    // getters
    public Long getPortfolioId() { return portfolioId; }
    public Long getStockId() { return stockId; }
    public Side getSide() { return side; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public Instant getAsOf() { return asOf; }
    public String getExternalTradeId() { return externalTradeId; }
    public String getExternalOrderId() { return externalOrderId; }
    public String getCurrency() { return currency; }
}
