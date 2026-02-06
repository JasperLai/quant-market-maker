package com.mubin.quant;

import com.mubin.quant.api.TradingHttpServer;
import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookRouter;
import com.mubin.quant.core.MarketDataService;
import com.mubin.quant.core.TradingCoreService;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.Side;
import com.mubin.quant.ingest.DimpleQuoteMessage;
import com.mubin.quant.ingest.QuoteNormalizer;
import com.mubin.quant.risk.DailyExposureTracker;
import com.mubin.quant.risk.PreTradeRiskEngine;
import com.mubin.quant.risk.RiskConfig;
import com.mubin.quant.risk.RiskDecision;
import com.mubin.quant.audit.InMemoryAuditSink;

public class App {

    public static void main(String[] args) throws Exception {
        PreTradeRiskEngine riskEngine = new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker());
        TradingCoreService tradingCoreService = new TradingCoreService(riskEngine, new InMemoryAuditSink(), 1000, 20_000);
        BookManager bookManager = new BookManager(new BookRouter());
        MarketDataService marketDataService = new MarketDataService(bookManager, new QuoteNormalizer());

        Order order = new Order("ord-1", AssetClass.FX, "USDCNY", Side.BUY, 7.2450, 1_000_000, 1_000_000);
        RiskDecision decision = riskEngine.check(order, 7.2448);
        System.out.println("Risk decision: " + decision.reason());

        marketDataService.onDimpleQuote(new DimpleQuoteMessage(
                "dq-1",
                AssetClass.GOLD,
                "AU99.99",
                Side.BUY,
                520.12,
                2000,
                System.currentTimeMillis(),
                2_000
        ));

        TradingHttpServer httpServer = new TradingHttpServer(8080, tradingCoreService, bookManager);
        httpServer.start();
        System.out.println("Trading API started on :8080");
    }
}
