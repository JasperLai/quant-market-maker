package com.mubin.quant.adapter.mq;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Side;
import com.mubin.quant.ingest.PlatformQuoteMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonPlatformQuoteParserTest {

    @Test
    void shouldParsePlatformQuoteJson() throws Exception {
        String raw = "{" +
                "\"messageId\":\"m-1\"," +
                "\"source\":\"PLATFORM\"," +
                "\"assetClass\":\"FX\"," +
                "\"symbol\":\"USDCNY\"," +
                "\"side\":\"BUY\"," +
                "\"price\":7.2," +
                "\"quantity\":1000000," +
                "\"businessTs\":1000," +
                "\"ttlMs\":500" +
                "}";

        PlatformQuoteMessage parsed = new JacksonPlatformQuoteParser().parse(raw);

        assertEquals("m-1", parsed.messageId());
        assertEquals("PLATFORM", parsed.source());
        assertEquals(AssetClass.FX, parsed.assetClass());
        assertEquals(Side.BUY, parsed.side());
        assertEquals(500, parsed.ttlMs());
    }
}
