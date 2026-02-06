package com.mubin.quant.domain;

import com.mubin.quant.book.InMemoryOrderBook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryOrderBookTest {

    @Test
    void shouldExpireQuotesByValidity() {
        InMemoryOrderBook book = new InMemoryOrderBook();
        long now = System.currentTimeMillis();

        book.upsert(new QuoteEvent("q1", "DIMPLE", AssetClass.GOLD, "AU99.99", Side.BUY, 520.00, 1000, now - 5, now - 1));
        book.upsert(new QuoteEvent("q2", "DIMPLE", AssetClass.GOLD, "AU99.99", Side.BUY, 519.90, 1000, now - 5, now + 10_000));

        book.expire(now);

        assertEquals(1, book.size());
    }
}
