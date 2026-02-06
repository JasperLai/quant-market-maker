package com.mubin.quant.hedge;

import com.mubin.quant.domain.Side;

public record HedgeChildOrder(
        String symbol,
        Side side,
        double quantity,
        double price
) {
}
