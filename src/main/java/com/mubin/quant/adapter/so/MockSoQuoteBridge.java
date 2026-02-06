package com.mubin.quant.adapter.so;

import com.mubin.quant.core.MarketDataService;
import com.mubin.quant.ingest.DimpleQuoteMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MockSoQuoteBridge implements SoQuoteBridge {

    private final DimpleEventParser parser;
    private final MarketDataService marketDataService;
    private final BlockingQueue<String> rawEvents = new LinkedBlockingQueue<>();
    private volatile boolean started;

    public MockSoQuoteBridge(DimpleEventParser parser, MarketDataService marketDataService) {
        this.parser = parser;
        this.marketDataService = marketDataService;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
        rawEvents.clear();
    }

    public void emitRaw(String rawEvent) throws Exception {
        if (!started) {
            throw new IllegalStateException("SO bridge not started");
        }
        rawEvents.add(rawEvent);
        DimpleQuoteMessage parsed = parser.parse(rawEvent);
        marketDataService.onDimpleQuote(parsed);
    }
}
