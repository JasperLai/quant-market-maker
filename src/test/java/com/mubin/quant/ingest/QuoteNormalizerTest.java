package com.mubin.quant.ingest;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.domain.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuoteNormalizerTest {

    @Test
    void shouldNormalizePlatformMessage() {
        QuoteNormalizer normalizer = new QuoteNormalizer();
        PlatformQuoteMessage message = new PlatformQuoteMessage(
                "m1", "PLATFORM", AssetClass.FX, "USDCNY", Side.BUY,
                7.20, 1_000_000, 1_000L, 500
        );

        QuoteEvent quote = normalizer.fromPlatform(message);

        assertEquals("m1", quote.quoteId());
        assertEquals("PLATFORM", quote.source());
        assertEquals(AssetClass.FX, quote.assetClass());
        assertEquals(1_500L, quote.validUntilTs());
    }

    @Test
    void shouldNormalizeDimpleMessage() {
        QuoteNormalizer normalizer = new QuoteNormalizer();
        DimpleQuoteMessage message = new DimpleQuoteMessage(
                "d1", AssetClass.GOLD, "AU99.99", Side.SELL,
                520.1, 2000, 2_000L, 300
        );

        QuoteEvent quote = normalizer.fromDimple(message);

        assertEquals("DIMPLE", quote.source());
        assertEquals(2_300L, quote.validUntilTs());
    }
}
