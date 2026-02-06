package com.mubin.quant.domain;

public record QuoteEvent(
        String quoteId,
        String source,
        AssetClass assetClass,
        String symbol,
        Side side,
        double price,
        double quantity,
        long businessTs,
        long validUntilTs
) {
    public boolean isExpired(long nowTs) {
        return nowTs >= validUntilTs;
    }
}
