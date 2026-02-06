package com.mubin.quant.book;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.domain.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookManagerTest {

    @Test
    void shouldRouteDimpleAndPlatformToDifferentBooks() {
        BookManager manager = new BookManager(new BookRouter());
        long now = System.currentTimeMillis();

        BookType dimpleBook = manager.onQuote(new QuoteEvent("q1", "DIMPLE", AssetClass.GOLD,
                "AU99.99", Side.BUY, 520.0, 1000, now, now + 1000));

        BookType platformBook = manager.onQuote(new QuoteEvent("q2", "PLATFORM", AssetClass.FX,
                "USDCNY", Side.BUY, 7.20, 1_000_000, now, now + 1000));

        assertEquals(BookType.DOMESTIC_MM, dimpleBook);
        assertEquals(BookType.OFFSHORE_HEDGE, platformBook);
        assertEquals(1, manager.size(BookType.DOMESTIC_MM));
        assertEquals(1, manager.size(BookType.OFFSHORE_HEDGE));
    }
}
