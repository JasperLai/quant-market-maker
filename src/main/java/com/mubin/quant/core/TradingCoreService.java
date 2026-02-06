package com.mubin.quant.core;

import com.mubin.quant.audit.AuditSink;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderEventType;
import com.mubin.quant.domain.OrderStateMachine;
import com.mubin.quant.execution.ExecutionReport;
import com.mubin.quant.execution.ExecutionReportProcessor;
import com.mubin.quant.lifecycle.RfqLifecycleManager;
import com.mubin.quant.position.PositionService;
import com.mubin.quant.recon.ReconciliationJob;
import com.mubin.quant.recon.VenueOrderSnapshot;
import com.mubin.quant.risk.PreTradeRiskEngine;
import com.mubin.quant.risk.RiskDecision;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TradingCoreService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    private final PreTradeRiskEngine preTradeRiskEngine;
    private final ExecutionReportProcessor executionReportProcessor;
    private final RfqLifecycleManager rfqLifecycleManager;
    private final PositionService positionService;
    private final ReconciliationJob reconciliationJob;

    public TradingCoreService(PreTradeRiskEngine preTradeRiskEngine,
                              AuditSink auditSink,
                              long reorderWindowMs,
                              long rfqTimeoutMs) {
        OrderStateMachine stateMachine = new OrderStateMachine();
        this.preTradeRiskEngine = preTradeRiskEngine;
        this.executionReportProcessor = new ExecutionReportProcessor(reorderWindowMs, stateMachine, auditSink);
        this.rfqLifecycleManager = new RfqLifecycleManager(rfqTimeoutMs, stateMachine, auditSink);
        this.positionService = new PositionService(auditSink);
        this.reconciliationJob = new ReconciliationJob(auditSink);
    }

    public SubmitOrderResult submitOrder(Order order, double referenceMidPrice, boolean rfqMode, long nowTs) {
        RiskDecision decision = preTradeRiskEngine.check(order, referenceMidPrice);
        if (!decision.passed()) {
            return SubmitOrderResult.reject(decision.reason());
        }

        orders.put(order.getOrderId(), order);
        if (rfqMode) {
            rfqLifecycleManager.registerRfq(order.getOrderId(), nowTs);
        }
        return SubmitOrderResult.accept();
    }

    public void onExecutionReport(ExecutionReport report) {
        executionReportProcessor.onReport(report, orders);

        if (report.eventType() == OrderEventType.FILL || report.eventType() == OrderEventType.PARTIAL_FILL) {
            Order order = orders.get(report.orderId());
            if (order != null) {
                rfqLifecycleManager.markCompleted(order.getOrderId());
                preTradeRiskEngine.onTradeBooked(order, report.lastFilledQty());
                positionService.onTrade(order, report.lastFilledQty(), order.getPrice(), report.receiveTs());
            }
        }
    }

    public void flushExecutionBuffer() {
        executionReportProcessor.flushAll(orders);
    }

    public List<String> runRfqTimeoutCheck(long nowTs) {
        return rfqLifecycleManager.expireDueOrders(nowTs, orders);
    }

    public List<String> reconcile(List<VenueOrderSnapshot> venueSnapshots, long eventTs) {
        return reconciliationJob.reconcile(orders, venueSnapshots, eventTs);
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public PositionService positionService() {
        return positionService;
    }
}
