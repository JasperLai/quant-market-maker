package com.mubin.quant.ingest;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Side;

public record DimpleQuoteMessage(
        String eventId,
        AssetClass assetClass,
        String symbol,
        Side side,
        double price,
        double quantity,
        long eventTs,
        int validForMs
) {
}
