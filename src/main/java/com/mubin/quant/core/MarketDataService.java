package com.mubin.quant.core;

import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookType;
import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.ingest.DimpleQuoteMessage;
import com.mubin.quant.ingest.PlatformQuoteMessage;
import com.mubin.quant.ingest.QuoteNormalizer;

public class MarketDataService {

    private final BookManager bookManager;
    private final QuoteNormalizer quoteNormalizer;

    public MarketDataService(BookManager bookManager, QuoteNormalizer quoteNormalizer) {
        this.bookManager = bookManager;
        this.quoteNormalizer = quoteNormalizer;
    }

    public BookType onPlatformQuote(PlatformQuoteMessage message) {
        QuoteEvent quoteEvent = quoteNormalizer.fromPlatform(message);
        return bookManager.onQuote(quoteEvent);
    }

    public BookType onDimpleQuote(DimpleQuoteMessage message) {
        QuoteEvent quoteEvent = quoteNormalizer.fromDimple(message);
        return bookManager.onQuote(quoteEvent);
    }

    public void expireQuotes(long nowTs) {
        bookManager.expireAll(nowTs);
    }

    public BookManager bookManager() {
        return bookManager;
    }
}
