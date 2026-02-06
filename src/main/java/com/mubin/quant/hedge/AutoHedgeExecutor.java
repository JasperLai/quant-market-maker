package com.mubin.quant.hedge;

import com.mubin.quant.audit.AuditEvent;
import com.mubin.quant.audit.AuditEventType;
import com.mubin.quant.audit.AuditSink;
import com.mubin.quant.book.BookManager;
import com.mubin.quant.core.SubmitOrderResult;
import com.mubin.quant.core.TradingCoreService;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.position.PositionSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutoHedgeExecutor {

    private final AutoHedgePlanner planner;
    private final TradingCoreService tradingCoreService;
    private final BookManager bookManager;
    private final AuditSink auditSink;

    public AutoHedgeExecutor(AutoHedgePlanner planner,
                             TradingCoreService tradingCoreService,
                             BookManager bookManager,
                             AuditSink auditSink) {
        this.planner = planner;
        this.tradingCoreService = tradingCoreService;
        this.bookManager = bookManager;
        this.auditSink = auditSink;
    }

    public HedgeExecutionResult run(AssetClass assetClass, String symbol, long nowTs) {
        PositionSnapshot position = tradingCoreService.positionService().snapshot(symbol);
        HedgePlan plan = planner.plan(assetClass, symbol, position, bookManager, nowTs);

        if (plan.action() == HedgeAction.NO_ACTION) {
            return new HedgeExecutionResult(plan, List.of());
        }

        if (plan.action() == HedgeAction.WAIT) {
            auditSink.publish(AuditEvent.of(
                    AuditEventType.HEDGE_WAITED,
                    symbol,
                    nowTs,
                    "Auto hedge wait, reason=" + plan.reason() + ", nextCheckTs=" + plan.nextCheckTs()
            ));
            return new HedgeExecutionResult(plan, List.of());
        }

        auditSink.publish(AuditEvent.of(
                AuditEventType.HEDGE_TRIGGERED,
                symbol,
                nowTs,
                "Auto hedge execute, plannedQty=" + plan.plannedQty() + ", vwap=" + plan.estimatedVwap()
        ));

        List<String> submittedOrderIds = new ArrayList<>();
        int index = 0;
        for (HedgeChildOrder child : plan.childOrders()) {
            String orderId = "hedge-" + symbol + "-" + nowTs + "-" + index + "-" + UUID.randomUUID();
            double notional = toNotional(assetClass, child.quantity(), child.price());
            Order order = new Order(orderId, assetClass, child.symbol(), child.side(), child.price(), child.quantity(), notional);
            SubmitOrderResult submit = tradingCoreService.submitOrder(order, plan.estimatedVwap(), false, nowTs);
            if (!submit.accepted()) {
                break;
            }
            submittedOrderIds.add(orderId);
            auditSink.publish(AuditEvent.of(
                    AuditEventType.HEDGE_ORDER_SUBMITTED,
                    orderId,
                    nowTs,
                    "Hedge child order submitted qty=" + child.quantity() + ", price=" + child.price()
            ));
            index++;
        }

        return new HedgeExecutionResult(plan, submittedOrderIds);
    }

    private double toNotional(AssetClass assetClass, double qty, double price) {
        if (assetClass == AssetClass.GOLD) {
            return qty * price;
        }
        return qty;
    }
}
