package com.mubin.quant.risk;

import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;

public class PreTradeRiskEngine {

    private final RiskConfig config;
    private final DailyExposureTracker dailyExposureTracker;

    public PreTradeRiskEngine(RiskConfig config, DailyExposureTracker dailyExposureTracker) {
        this.config = config;
        this.dailyExposureTracker = dailyExposureTracker;
    }

    public RiskDecision check(Order order, double referenceMidPrice) {
        // Use notional/amount as the first hard stop to short-circuit expensive checks.
        if (order.getNotional() > config.singleOrderNotionalLimit()) {
            return RiskDecision.fail("SINGLE_ORDER_LIMIT_EXCEEDED");
        }

        RiskDecision priceDecision = checkPriceDeviation(order, referenceMidPrice);
        if (!priceDecision.passed()) {
            return priceDecision;
        }

        RiskDecision dailyDecision = checkDailyLimit(order);
        if (!dailyDecision.passed()) {
            return dailyDecision;
        }

        return RiskDecision.pass();
    }

    public void onTradeBooked(Order order, double executedQuantity) {
        // Exposure accounting follows product-specific units: FX by notional, Gold by grams.
        if (order.getAssetClass() == AssetClass.FX) {
            dailyExposureTracker.addFxNotional(order.getNotional());
        } else {
            dailyExposureTracker.addGoldQtyGrams(executedQuantity);
        }
    }

    private RiskDecision checkPriceDeviation(Order order, double referenceMidPrice) {
        double relativeDiffPct = Math.abs(order.getPrice() - referenceMidPrice) / referenceMidPrice * 100.0;
        if (order.getAssetClass() == AssetClass.FX) {
            double diffBps = relativeDiffPct * 100.0;
            if (diffBps > config.fxDeviationBps()) {
                return RiskDecision.fail("FX_PRICE_DEVIATION_EXCEEDED");
            }
            return RiskDecision.pass();
        }

        if (relativeDiffPct > config.goldDeviationPct()) {
            return RiskDecision.fail("GOLD_PRICE_DEVIATION_EXCEEDED");
        }
        return RiskDecision.pass();
    }

    private RiskDecision checkDailyLimit(Order order) {
        double current = dailyExposureTracker.current(order.getAssetClass());
        if (order.getAssetClass() == AssetClass.FX && current + order.getNotional() > config.fxDailyNotionalLimit()) {
            return RiskDecision.fail("FX_DAILY_LIMIT_EXCEEDED");
        }
        if (order.getAssetClass() == AssetClass.GOLD && current + order.getQuantity() > config.goldDailyQtyLimitGrams()) {
            return RiskDecision.fail("GOLD_DAILY_LIMIT_EXCEEDED");
        }
        return RiskDecision.pass();
    }
}
