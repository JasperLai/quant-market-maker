package com.mubin.quant.adapter.so;

import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookRouter;
import com.mubin.quant.book.BookType;
import com.mubin.quant.core.MarketDataService;
import com.mubin.quant.domain.Side;
import com.mubin.quant.ingest.QuoteNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MockSoQuoteBridgeTest {

    @Test
    void shouldDispatchSoEventIntoDomesticBook() throws Exception {
        BookManager bookManager = new BookManager(new BookRouter());
        MarketDataService marketDataService = new MarketDataService(bookManager, new QuoteNormalizer());
        MockSoQuoteBridge bridge = new MockSoQuoteBridge(new JacksonDimpleEventParser(), marketDataService);

        bridge.start();
        try {
            bridge.emitRaw("{" +
                    "\"eventId\":\"d-2\"," +
                    "\"assetClass\":\"GOLD\"," +
                    "\"symbol\":\"AU99.99\"," +
                    "\"side\":\"BUY\"," +
                    "\"price\":520.22," +
                    "\"quantity\":2000," +
                    "\"eventTs\":3000," +
                    "\"validForMs\":500" +
                    "}");

            assertEquals(1, bookManager.topLevels(BookType.DOMESTIC_MM, "AU99.99", Side.BUY, 5).size());
        } finally {
            bridge.stop();
        }
    }
}
