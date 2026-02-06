package com.mubin.quant.execution;

import com.mubin.quant.audit.AuditEventType;
import com.mubin.quant.audit.InMemoryAuditSink;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderEventType;
import com.mubin.quant.domain.OrderStateMachine;
import com.mubin.quant.domain.OrderStatus;
import com.mubin.quant.domain.Side;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutionReportProcessorTest {

    @Test
    void shouldDeduplicateSameReport() {
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        ExecutionReportProcessor processor = new ExecutionReportProcessor(1000, new OrderStateMachine(), auditSink);

        Order order = new Order("o1", AssetClass.FX, "USDCNY", Side.BUY, 7.2, 1_000_000, 1_000_000);
        Map<String, Order> orders = new HashMap<>();
        orders.put(order.getOrderId(), order);

        long base = 1_000_000L;
        ExecutionReport ack = new ExecutionReport("CFETS", "o1", "e1", "t1", OrderEventType.ACK, 0, base, base + 1);

        processor.onReport(ack, orders);
        processor.onReport(ack, orders);
        processor.flushAll(orders);

        assertEquals(OrderStatus.ACKED, order.getStatus());
        LocalDate baseDate = Instant.ofEpochMilli(base).atZone(ZoneId.systemDefault()).toLocalDate();
        long dupCount = auditSink.findByDate(baseDate, ZoneId.systemDefault())
                .stream().filter(e -> e.type() == AuditEventType.DUPLICATE_REPORT).count();
        assertEquals(1, dupCount);
    }

    @Test
    void shouldReorderWithinWindowByBusinessTs() {
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        ExecutionReportProcessor processor = new ExecutionReportProcessor(1000, new OrderStateMachine(), auditSink);

        Order order = new Order("o2", AssetClass.FX, "USDCNY", Side.BUY, 7.2, 1_000_000, 1_000_000);
        Map<String, Order> orders = new HashMap<>();
        orders.put(order.getOrderId(), order);

        long now = System.currentTimeMillis();
        ExecutionReport fill = new ExecutionReport("CFETS", "o2", "e2", "t2", OrderEventType.FILL, 1_000_000, now + 900, now + 900);
        ExecutionReport ack = new ExecutionReport("CFETS", "o2", "e3", "t3", OrderEventType.ACK, 0, now + 100, now + 950);

        processor.onReport(fill, orders);
        processor.onReport(ack, orders);
        processor.flushAll(orders);

        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertEquals(1_000_000, order.getFilledQuantity());
    }
}
