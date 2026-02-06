package com.mubin.quant.adapter.so;

import com.mubin.quant.ingest.DimpleQuoteMessage;

public interface DimpleEventParser {
    DimpleQuoteMessage parse(String rawEvent) throws Exception;
}
