package com.mubin.quant.hedge;

import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookRouter;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.domain.Side;
import com.mubin.quant.position.PositionSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoHedgePlannerTest {

    @Test
    void shouldReturnNoActionWhenPositionBelowTrigger() {
        HedgeConfig config = new HedgeConfig(100_000, -5, 5000, 0.3, 10_000, 200_000, 10);
        AutoHedgePlanner planner = new AutoHedgePlanner(config);

        HedgePlan plan = planner.plan(AssetClass.FX, "USDCNY",
                new PositionSnapshot("USDCNY", 90_000, 7.2),
                new BookManager(new BookRouter()),
                1_000L);

        assertEquals(HedgeAction.NO_ACTION, plan.action());
    }

    @Test
    void shouldWaitWhenPnlGateNotMet() {
        HedgeConfig config = new HedgeConfig(100_000, 5, 5000, 0.5, 10_000, 200_000, 10);
        AutoHedgePlanner planner = new AutoHedgePlanner(config);
        BookManager bookManager = new BookManager(new BookRouter());
        long now = System.currentTimeMillis();

        // Long position, hedge side should be SELL. Current sell quotes are below avg position price.
        bookManager.onQuote(new QuoteEvent("q1", "PLATFORM", AssetClass.FX, "USDCNY", Side.SELL,
                7.10, 500_000, now, now + 10_000));

        HedgePlan plan = planner.plan(AssetClass.FX, "USDCNY",
                new PositionSnapshot("USDCNY", 300_000, 7.20),
                bookManager,
                now);

        assertEquals(HedgeAction.WAIT, plan.action());
        assertEquals("PNL_GATE_NOT_MET", plan.reason());
        assertTrue(plan.nextCheckTs() > now);
    }

    @Test
    void shouldSplitByVolumeAndParticipation() {
        HedgeConfig config = new HedgeConfig(0, -100, 5000, 0.5, 50_000, 120_000, 10);
        AutoHedgePlanner planner = new AutoHedgePlanner(config);
        BookManager bookManager = new BookManager(new BookRouter());
        long now = System.currentTimeMillis();

        bookManager.onQuote(new QuoteEvent("q1", "PLATFORM", AssetClass.FX, "USDCNY", Side.SELL,
                7.20, 400_000, now, now + 10_000));
        bookManager.onQuote(new QuoteEvent("q2", "PLATFORM", AssetClass.FX, "USDCNY", Side.SELL,
                7.21, 200_000, now, now + 10_000));

        HedgePlan plan = planner.plan(AssetClass.FX, "USDCNY",
                new PositionSnapshot("USDCNY", 500_000, 7.10),
                bookManager,
                now);

        assertEquals(HedgeAction.EXECUTE, plan.action());
        // max tradable = 0.5 * (400k + 200k) = 300k
        assertEquals(300_000, plan.plannedQty(), 0.0001);
        assertTrue(plan.childOrders().size() >= 3);
    }
}
