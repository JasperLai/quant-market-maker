package com.mubin.quant.execution;

import com.mubin.quant.audit.AuditEvent;
import com.mubin.quant.audit.AuditEventType;
import com.mubin.quant.audit.AuditSink;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderEventType;
import com.mubin.quant.domain.OrderStateMachine;
import com.mubin.quant.domain.OrderStatus;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionReportProcessor {

    private final long reorderWindowMs;
    private final OrderStateMachine stateMachine;
    private final AuditSink auditSink;

    private final Set<String> seenReportKeys = ConcurrentHashMap.newKeySet();
    private final PriorityQueue<ExecutionReport> buffer = new PriorityQueue<>(
            Comparator.comparingLong(ExecutionReport::businessTs).thenComparingLong(ExecutionReport::receiveTs)
    );

    private long maxBusinessTsSeen = Long.MIN_VALUE;

    public ExecutionReportProcessor(long reorderWindowMs, OrderStateMachine stateMachine, AuditSink auditSink) {
        this.reorderWindowMs = reorderWindowMs;
        this.stateMachine = stateMachine;
        this.auditSink = auditSink;
    }

    public void onReport(ExecutionReport report, Map<String, Order> orderById) {
        String dedupKey = report.dedupKey();
        if (!seenReportKeys.add(dedupKey)) {
            auditSink.publish(AuditEvent.of(
                    AuditEventType.DUPLICATE_REPORT,
                    report.orderId(),
                    report.receiveTs(),
                    "Duplicate report ignored: " + dedupKey
            ));
            return;
        }

        buffer.add(report);
        maxBusinessTsSeen = Math.max(maxBusinessTsSeen, report.businessTs());

        long flushWatermark = maxBusinessTsSeen - reorderWindowMs;
        flushEligible(flushWatermark, orderById);
    }

    public void flushAll(Map<String, Order> orderById) {
        while (!buffer.isEmpty()) {
            apply(buffer.poll(), orderById);
        }
    }

    private void flushEligible(long watermarkTs, Map<String, Order> orderById) {
        while (!buffer.isEmpty() && buffer.peek().businessTs() <= watermarkTs) {
            apply(buffer.poll(), orderById);
        }
    }

    private void apply(ExecutionReport report, Map<String, Order> orderById) {
        Order order = orderById.get(report.orderId());
        if (order == null) {
            auditSink.publish(AuditEvent.of(
                    AuditEventType.ORPHAN_REPORT,
                    report.orderId(),
                    report.receiveTs(),
                    "No order found for report"
            ));
            return;
        }

        OrderStatus prev = order.getStatus();
        OrderStatus next = stateMachine.apply(prev, report.eventType());
        if (next == prev && isStateChangeEvent(report.eventType())) {
            auditSink.publish(AuditEvent.of(
                    AuditEventType.STATE_CONFLICT,
                    order.getOrderId(),
                    report.receiveTs(),
                    "State unchanged for event: " + report.eventType()
            ));
            return;
        }

        order.setStatus(next);
        if (report.eventType() == OrderEventType.PARTIAL_FILL || report.eventType() == OrderEventType.FILL) {
            order.addFilledQuantity(report.lastFilledQty());
        }

        auditSink.publish(AuditEvent.of(
                AuditEventType.ORDER_EVENT_APPLIED,
                order.getOrderId(),
                report.receiveTs(),
                "Applied event " + report.eventType() + ", state: " + prev + " -> " + next
        ));
    }

    private boolean isStateChangeEvent(OrderEventType eventType) {
        return eventType == OrderEventType.ACK
                || eventType == OrderEventType.PARTIAL_FILL
                || eventType == OrderEventType.FILL
                || eventType == OrderEventType.CANCEL_REQUESTED
                || eventType == OrderEventType.CANCELED
                || eventType == OrderEventType.REJECT
                || eventType == OrderEventType.TIMEOUT;
    }
}
