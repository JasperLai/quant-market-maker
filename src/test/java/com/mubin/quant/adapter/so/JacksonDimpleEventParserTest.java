package com.mubin.quant.adapter.so;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Side;
import com.mubin.quant.ingest.DimpleQuoteMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonDimpleEventParserTest {

    @Test
    void shouldParseDimpleEventJson() throws Exception {
        String raw = "{" +
                "\"eventId\":\"d-1\"," +
                "\"assetClass\":\"GOLD\"," +
                "\"symbol\":\"AU99.99\"," +
                "\"side\":\"SELL\"," +
                "\"price\":520.12," +
                "\"quantity\":3000," +
                "\"eventTs\":2000," +
                "\"validForMs\":1000" +
                "}";

        DimpleQuoteMessage parsed = new JacksonDimpleEventParser().parse(raw);

        assertEquals("d-1", parsed.eventId());
        assertEquals(AssetClass.GOLD, parsed.assetClass());
        assertEquals(Side.SELL, parsed.side());
        assertEquals(1000, parsed.validForMs());
    }
}
