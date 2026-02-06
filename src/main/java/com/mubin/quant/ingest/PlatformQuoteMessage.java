package com.mubin.quant.ingest;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Side;

public record PlatformQuoteMessage(
        String messageId,
        String source,
        AssetClass assetClass,
        String symbol,
        Side side,
        double price,
        double quantity,
        long businessTs,
        int ttlMs
) {
}
