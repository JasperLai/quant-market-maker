package com.mubin.quant.book;

import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.domain.Side;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BookManager {

    private final BookRouter router;
    private final Map<BookType, InMemoryOrderBook> books = new EnumMap<>(BookType.class);

    public BookManager(BookRouter router) {
        this.router = router;
        books.put(BookType.DOMESTIC_MM, new InMemoryOrderBook());
        books.put(BookType.OFFSHORE_HEDGE, new InMemoryOrderBook());
    }

    public BookType onQuote(QuoteEvent quoteEvent) {
        BookType bookType = router.route(quoteEvent);
        books.get(bookType).upsert(quoteEvent);
        return bookType;
    }

    public void expireAll(long nowTs) {
        for (InMemoryOrderBook book : books.values()) {
            book.expire(nowTs);
        }
    }

    public List<QuoteEvent> topLevels(BookType bookType, String symbol, Side side, int depth) {
        return books.get(bookType).topLevels(symbol, side, depth);
    }

    public int size(BookType bookType) {
        return books.get(bookType).size();
    }
}
