package com.mubin.quant.book;

import com.mubin.quant.domain.QuoteEvent;

public class BookRouter {

    public BookType route(QuoteEvent quoteEvent) {
        if ("DIMPLE".equalsIgnoreCase(quoteEvent.source())) {
            return BookType.DOMESTIC_MM;
        }
        return BookType.OFFSHORE_HEDGE;
    }
}
