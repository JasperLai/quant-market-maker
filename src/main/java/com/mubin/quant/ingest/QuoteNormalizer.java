package com.mubin.quant.ingest;

import com.mubin.quant.domain.QuoteEvent;

public class QuoteNormalizer {

    public QuoteEvent fromPlatform(PlatformQuoteMessage message) {
        return new QuoteEvent(
                message.messageId(),
                message.source(),
                message.assetClass(),
                message.symbol(),
                message.side(),
                message.price(),
                message.quantity(),
                message.businessTs(),
                message.businessTs() + message.ttlMs()
        );
    }

    public QuoteEvent fromDimple(DimpleQuoteMessage message) {
        return new QuoteEvent(
                message.eventId(),
                "DIMPLE",
                message.assetClass(),
                message.symbol(),
                message.side(),
                message.price(),
                message.quantity(),
                message.eventTs(),
                message.eventTs() + message.validForMs()
        );
    }
}
