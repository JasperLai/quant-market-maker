package com.mubin.quant.adapter.mq;

import com.mubin.quant.ingest.PlatformQuoteMessage;

public interface PlatformQuoteParser {
    PlatformQuoteMessage parse(String raw) throws Exception;
}
