package com.mubin.quant.adapter.so;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mubin.quant.ingest.DimpleQuoteMessage;

public class JacksonDimpleEventParser implements DimpleEventParser {

    private final ObjectMapper objectMapper;

    public JacksonDimpleEventParser() {
        this(new ObjectMapper());
    }

    public JacksonDimpleEventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public DimpleQuoteMessage parse(String rawEvent) throws Exception {
        return objectMapper.readValue(rawEvent, DimpleQuoteMessage.class);
    }
}
