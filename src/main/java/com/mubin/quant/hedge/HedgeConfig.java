package com.mubin.quant.hedge;

public record HedgeConfig(
        double triggerPositionQty,
        double minPnlBps,
        long waitMs,
        double maxParticipationRate,
        double minChildQty,
        double maxChildQty,
        int bookDepth
) {
    public static HedgeConfig defaultConfig() {
        return new HedgeConfig(
                100_000.0,
                -5.0,
                5_000,
                0.30,
                10_000.0,
                200_000.0,
                10
        );
    }
}
