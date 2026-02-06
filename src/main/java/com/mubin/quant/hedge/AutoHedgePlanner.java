package com.mubin.quant.hedge;

import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookType;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.QuoteEvent;
import com.mubin.quant.domain.Side;
import com.mubin.quant.position.PositionSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AutoHedgePlanner {

    private final HedgeConfig config;

    public AutoHedgePlanner(HedgeConfig config) {
        this.config = config;
    }

    public HedgePlan plan(AssetClass assetClass, String symbol, PositionSnapshot position, BookManager bookManager, long nowTs) {
        double netQty = position.netQuantity();
        double absNetQty = Math.abs(netQty);
        if (absNetQty <= config.triggerPositionQty()) {
            return HedgePlan.noAction("POSITION_BELOW_TRIGGER");
        }

        Side hedgeSide = netQty > 0 ? Side.SELL : Side.BUY;
        double requestedQty = absNetQty - config.triggerPositionQty();

        List<QuoteEvent> levels = loadLevels(assetClass, symbol, hedgeSide, bookManager);
        if (levels.isEmpty()) {
            return HedgePlan.waitPlan("NO_LIQUIDITY", requestedQty, 0.0, 0.0, 0.0, nowTs + config.waitMs());
        }

        double maxTradableQty = maxTradableByParticipation(levels);
        double plannedQty = Math.min(requestedQty, maxTradableQty);
        if (plannedQty < config.minChildQty()) {
            return HedgePlan.waitPlan("INSUFFICIENT_TRADABLE_QTY", requestedQty, plannedQty, 0.0, 0.0,
                    nowTs + config.waitMs());
        }

        List<HedgeChildOrder> childOrders = splitIntoChildOrders(symbol, hedgeSide, levels, plannedQty);
        if (childOrders.isEmpty()) {
            return HedgePlan.waitPlan("NO_VALID_CHILD_ORDERS", requestedQty, plannedQty, 0.0, 0.0,
                    nowTs + config.waitMs());
        }

        double actualPlannedQty = childOrders.stream().mapToDouble(HedgeChildOrder::quantity).sum();
        double vwap = childOrders.stream().mapToDouble(order -> order.quantity() * order.price()).sum() / actualPlannedQty;
        double pnlBps = estimatePnlBps(position.avgPrice(), vwap, hedgeSide);
        if (pnlBps < config.minPnlBps()) {
            return HedgePlan.waitPlan("PNL_GATE_NOT_MET", requestedQty, actualPlannedQty, vwap, pnlBps,
                    nowTs + config.waitMs());
        }

        return HedgePlan.executePlan("EXECUTE", requestedQty, actualPlannedQty, vwap, pnlBps, childOrders);
    }

    private List<QuoteEvent> loadLevels(AssetClass assetClass, String symbol, Side side, BookManager bookManager) {
        List<QuoteEvent> levels = new ArrayList<>();

        if (assetClass == AssetClass.GOLD) {
            levels.addAll(bookManager.topLevels(BookType.DOMESTIC_MM, symbol, side, config.bookDepth()));
            if (levels.isEmpty()) {
                levels.addAll(bookManager.topLevels(BookType.OFFSHORE_HEDGE, symbol, side, config.bookDepth()));
            }
            return levels;
        }

        levels.addAll(bookManager.topLevels(BookType.OFFSHORE_HEDGE, symbol, side, config.bookDepth()));
        if (levels.isEmpty()) {
            levels.addAll(bookManager.topLevels(BookType.DOMESTIC_MM, symbol, side, config.bookDepth()));
        }
        return levels;
    }

    private double maxTradableByParticipation(List<QuoteEvent> levels) {
        double total = 0.0;
        for (QuoteEvent level : levels) {
            total += level.quantity() * config.maxParticipationRate();
        }
        return total;
    }

    private double estimatePnlBps(double avgPosPrice, double hedgePrice, Side hedgeSide) {
        if (avgPosPrice <= 0) {
            return 0.0;
        }

        double pnlPerUnit = hedgeSide == Side.SELL
                ? hedgePrice - avgPosPrice
                : avgPosPrice - hedgePrice;

        return pnlPerUnit / avgPosPrice * 10_000.0;
    }

    private List<HedgeChildOrder> splitIntoChildOrders(String symbol, Side side, List<QuoteEvent> levels, double targetQty) {
        List<HedgeChildOrder> childOrders = new ArrayList<>();
        double remaining = targetQty;

        for (QuoteEvent level : levels) {
            if (remaining <= 0) {
                break;
            }

            double levelLimit = level.quantity() * config.maxParticipationRate();
            double allowed = Math.min(levelLimit, remaining);
            while (allowed >= config.minChildQty() && remaining > 0) {
                double childQty = Math.min(Math.min(allowed, config.maxChildQty()), remaining);
                if (childQty < config.minChildQty()) {
                    break;
                }

                childOrders.add(new HedgeChildOrder(symbol, side, childQty, level.price()));
                remaining -= childQty;
                allowed -= childQty;
            }
        }

        return childOrders;
    }
}
