package com.mubin.quant.core;

import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookRouter;
import com.mubin.quant.book.BookType;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Side;
import com.mubin.quant.ingest.DimpleQuoteMessage;
import com.mubin.quant.ingest.PlatformQuoteMessage;
import com.mubin.quant.ingest.QuoteNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketDataServiceTest {

    @Test
    void shouldIngestIntoSeparatedBooks() {
        BookManager bookManager = new BookManager(new BookRouter());
        MarketDataService service = new MarketDataService(bookManager, new QuoteNormalizer());
        long now = System.currentTimeMillis();

        BookType dimpleType = service.onDimpleQuote(new DimpleQuoteMessage(
                "d1", AssetClass.GOLD, "AU99.99", Side.BUY, 520.0, 1000, now, 1000
        ));

        BookType platformType = service.onPlatformQuote(new PlatformQuoteMessage(
                "p1", "PLATFORM", AssetClass.FX, "USDCNY", Side.SELL, 7.2, 1_000_000, now, 1000
        ));

        assertEquals(BookType.DOMESTIC_MM, dimpleType);
        assertEquals(BookType.OFFSHORE_HEDGE, platformType);
        assertEquals(1, bookManager.size(BookType.DOMESTIC_MM));
        assertEquals(1, bookManager.size(BookType.OFFSHORE_HEDGE));
    }
}
