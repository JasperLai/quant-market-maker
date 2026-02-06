package com.mubin.quant.recon;

import com.mubin.quant.domain.OrderStatus;

public record VenueOrderSnapshot(
        String orderId,
        OrderStatus status,
        double filledQuantity
) {
}
