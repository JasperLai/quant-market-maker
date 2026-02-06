package com.mubin.quant.risk;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreTradeRiskEngineTest {

    @Test
    void shouldRejectOnSingleOrderLimit() {
        PreTradeRiskEngine engine = new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker());
        Order order = new Order("o1", AssetClass.FX, "USDCNY", Side.BUY, 7.2, 10_000_000, 6_000_000);

        RiskDecision decision = engine.check(order, 7.2);

        assertFalse(decision.passed());
    }

    @Test
    void shouldRejectOnFxPriceDeviation() {
        PreTradeRiskEngine engine = new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker());
        Order order = new Order("o2", AssetClass.FX, "USDCNY", Side.BUY, 7.30, 1_000_000, 1_000_000);

        RiskDecision decision = engine.check(order, 7.20);

        assertFalse(decision.passed());
    }

    @Test
    void shouldPassForReasonableGoldOrder() {
        PreTradeRiskEngine engine = new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker());
        Order order = new Order("o3", AssetClass.GOLD, "AU99.99", Side.SELL, 520.20, 2_000, 1_040_400);

        RiskDecision decision = engine.check(order, 520.00);

        assertTrue(decision.passed());
    }
}
