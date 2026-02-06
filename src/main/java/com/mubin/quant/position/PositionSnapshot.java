package com.mubin.quant.position;

public record PositionSnapshot(
        String symbol,
        double netQuantity,
        double avgPrice
) {
}
