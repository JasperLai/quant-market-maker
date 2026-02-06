package com.mubin.quant.adapter.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mubin.quant.ingest.PlatformQuoteMessage;

public class JacksonPlatformQuoteParser implements PlatformQuoteParser {

    private final ObjectMapper objectMapper;

    public JacksonPlatformQuoteParser() {
        this(new ObjectMapper());
    }

    public JacksonPlatformQuoteParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PlatformQuoteMessage parse(String raw) throws Exception {
        return objectMapper.readValue(raw, PlatformQuoteMessage.class);
    }
}
