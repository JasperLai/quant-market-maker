package com.mubin.quant.book;

import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.domain.Side;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderBook {

    private final Map<String, QuoteEvent> quotesById = new ConcurrentHashMap<>();

    public void upsert(QuoteEvent quoteEvent) {
        quotesById.put(quoteEvent.quoteId(), quoteEvent);
    }

    public void expire(long nowTs) {
        quotesById.values().removeIf(quote -> quote.isExpired(nowTs));
    }

    public List<QuoteEvent> topLevels(String symbol, Side side, int depth) {
        Comparator<QuoteEvent> comparator = side == Side.BUY
                ? Comparator.comparingDouble(QuoteEvent::price).reversed()
                : Comparator.comparingDouble(QuoteEvent::price);

        List<QuoteEvent> rows = new ArrayList<>();
        for (QuoteEvent quoteEvent : quotesById.values()) {
            if (quoteEvent.symbol().equals(symbol) && quoteEvent.side() == side) {
                rows.add(quoteEvent);
            }
        }
        rows.sort(comparator.thenComparingLong(QuoteEvent::businessTs));

        if (rows.size() <= depth) {
            return rows;
        }
        return rows.subList(0, depth);
    }

    public int size() {
        return quotesById.size();
    }
}
