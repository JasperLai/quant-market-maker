package com.mubin.quant.hedge;

import com.mubin.quant.audit.InMemoryAuditSink;
import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookRouter;
import com.mubin.quant.core.TradingCoreService;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.Side;
import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.risk.DailyExposureTracker;
import com.mubin.quant.risk.PreTradeRiskEngine;
import com.mubin.quant.risk.RiskConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoHedgeExecutorTest {

    @Test
    void shouldSubmitChildOrdersWhenPlanIsExecutable() {
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        TradingCoreService core = new TradingCoreService(
                new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker()),
                auditSink,
                1000,
                20_000
        );

        BookManager bookManager = new BookManager(new BookRouter());
        long now = System.currentTimeMillis();
        bookManager.onQuote(new QuoteEvent("q1", "PLATFORM", AssetClass.FX, "USDCNY", Side.SELL,
                7.20, 500_000, now, now + 10_000));

        Order posSeed = new Order("seed", AssetClass.FX, "USDCNY", Side.BUY, 7.00, 300_000, 300_000);
        core.positionService().onTrade(posSeed, 300_000, 7.00, now);

        HedgeConfig config = new HedgeConfig(100_000, -100, 5000, 0.5, 50_000, 150_000, 10);
        AutoHedgeExecutor executor = new AutoHedgeExecutor(new AutoHedgePlanner(config), core, bookManager, auditSink);

        HedgeExecutionResult result = executor.run(AssetClass.FX, "USDCNY", now);

        assertEquals(HedgeAction.EXECUTE, result.plan().action());
        assertTrue(result.submittedOrderIds().size() > 0);
        assertTrue(core.getOrder(result.submittedOrderIds().get(0)) != null);
    }
}
