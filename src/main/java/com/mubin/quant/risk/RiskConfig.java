package com.mubin.quant.risk;

public record RiskConfig(
        double fxDeviationBps,
        double goldDeviationPct,
        double singleOrderNotionalLimit,
        double fxDailyNotionalLimit,
        double goldDailyQtyLimitGrams
) {
    public static RiskConfig defaultConfig() {
        return new RiskConfig(
                15.0,
                0.30,
                5_000_000.0,
                100_000_000.0,
                1_000_000.0
        );
    }
}
