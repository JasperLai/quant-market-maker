package com.mubin.quant.lifecycle;

import com.mubin.quant.audit.AuditEvent;
import com.mubin.quant.audit.AuditEventType;
import com.mubin.quant.audit.AuditSink;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderEventType;
import com.mubin.quant.domain.OrderStateMachine;
import com.mubin.quant.domain.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RfqLifecycleManager {

    private final long timeoutMs;
    private final OrderStateMachine stateMachine;
    private final AuditSink auditSink;

    private final Map<String, Long> rfqDeadlines = new ConcurrentHashMap<>();

    public RfqLifecycleManager(long timeoutMs, OrderStateMachine stateMachine, AuditSink auditSink) {
        this.timeoutMs = timeoutMs;
        this.stateMachine = stateMachine;
        this.auditSink = auditSink;
    }

    public void registerRfq(String orderId, long sentTs) {
        rfqDeadlines.put(orderId, sentTs + timeoutMs);
    }

    public void markCompleted(String orderId) {
        rfqDeadlines.remove(orderId);
    }

    public List<String> expireDueOrders(long nowTs, Map<String, Order> orderById) {
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Long> entry : rfqDeadlines.entrySet()) {
            String orderId = entry.getKey();
            long deadline = entry.getValue();
            if (nowTs < deadline) {
                continue;
            }

            Order order = orderById.get(orderId);
            if (order != null) {
                OrderStatus prev = order.getStatus();
                OrderStatus next = stateMachine.apply(prev, OrderEventType.TIMEOUT);
                order.setStatus(next);
                auditSink.publish(AuditEvent.of(
                        AuditEventType.RFQ_EXPIRED,
                        orderId,
                        nowTs,
                        "RFQ expired: " + prev + " -> " + next
                ));
                expired.add(orderId);
            }
            rfqDeadlines.remove(orderId);
        }
        return expired;
    }
}
