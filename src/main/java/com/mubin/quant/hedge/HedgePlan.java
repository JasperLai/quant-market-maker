package com.mubin.quant.hedge;

import java.util.List;

public record HedgePlan(
        HedgeAction action,
        String reason,
        double requestedQty,
        double plannedQty,
        double estimatedVwap,
        double estimatedPnlBps,
        long nextCheckTs,
        List<HedgeChildOrder> childOrders
) {
    public static HedgePlan noAction(String reason) {
        return new HedgePlan(HedgeAction.NO_ACTION, reason, 0.0, 0.0, 0.0, 0.0, 0L, List.of());
    }

    public static HedgePlan waitPlan(String reason, double requestedQty, double plannedQty, double vwap,
                                     double pnlBps, long nextCheckTs) {
        return new HedgePlan(HedgeAction.WAIT, reason, requestedQty, plannedQty, vwap, pnlBps, nextCheckTs, List.of());
    }

    public static HedgePlan executePlan(String reason, double requestedQty, double plannedQty, double vwap,
                                        double pnlBps, List<HedgeChildOrder> childOrders) {
        return new HedgePlan(HedgeAction.EXECUTE, reason, requestedQty, plannedQty, vwap, pnlBps, 0L, childOrders);
    }
}
