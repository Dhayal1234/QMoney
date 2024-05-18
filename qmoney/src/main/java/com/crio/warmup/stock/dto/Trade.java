package com.crio.warmup.stock.dto;

public class Trade {
  private String symbol;
  private int quantity;
  private String tradeType;
  private String purchaseDate;

  // Getters and setters

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public String getTradeType() {
    return tradeType;
  }

  public void setTradeType(String tradeType) {
    this.tradeType = tradeType;
  }

  public String getPurchaseDate() {
    return purchaseDate;
  }

  public void setPurchaseDate(String purchaseDate) {
    this.purchaseDate = purchaseDate;
  }
}
