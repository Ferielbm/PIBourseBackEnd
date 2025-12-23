package tn.esprit.piboursebackend.Order.dto;

import java.math.BigDecimal;

public class OrderBookStats {
    private String symbol;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal lastPrice;

    public OrderBookStats() {}

    public OrderBookStats(String symbol, BigDecimal bestBid, BigDecimal bestAsk, BigDecimal lastPrice) {
        this.symbol = symbol;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.lastPrice = lastPrice;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getBestBid() { return bestBid; }
    public void setBestBid(BigDecimal bestBid) { this.bestBid = bestBid; }

    public BigDecimal getBestAsk() { return bestAsk; }
    public void setBestAsk(BigDecimal bestAsk) { this.bestAsk = bestAsk; }

    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
}
