package com.mubin.quant.domain;

public class Order {
    private final String orderId;
    private final AssetClass assetClass;
    private final String symbol;
    private final Side side;
    private final double price;
    private final double quantity;
    private final double notional;

    private OrderStatus status;
    private double filledQuantity;

    public Order(String orderId, AssetClass assetClass, String symbol, Side side, double price, double quantity, double notional) {
        this.orderId = orderId;
        this.assetClass = assetClass;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.notional = notional;
        this.status = OrderStatus.NEW;
        this.filledQuantity = 0.0;
    }

    public String getOrderId() {
        return orderId;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public String getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getNotional() {
        return notional;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public double getFilledQuantity() {
        return filledQuantity;
    }

    public void addFilledQuantity(double delta) {
        this.filledQuantity += delta;
    }
}
