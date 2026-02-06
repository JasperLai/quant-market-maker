package com.mubin.quant.audit;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailyAuditAggregatorTest {

    @Test
    void shouldAggregateEventCountsByType() {
        InMemoryAuditSink sink = new InMemoryAuditSink();
        ZoneId zoneId = ZoneId.systemDefault();
        long now = System.currentTimeMillis();

        sink.publish(AuditEvent.of(AuditEventType.ORDER_EVENT_APPLIED, "o1", now, "applied"));
        sink.publish(AuditEvent.of(AuditEventType.ORDER_EVENT_APPLIED, "o2", now, "applied"));
        sink.publish(AuditEvent.of(AuditEventType.DUPLICATE_REPORT, "o2", now, "dup"));

        DailyAuditAggregator aggregator = new DailyAuditAggregator(sink, zoneId);
        DailyAuditReport report = aggregator.generate(LocalDate.now(zoneId));

        assertEquals(3, report.totalEvents());
        assertEquals(2L, report.byType().get(AuditEventType.ORDER_EVENT_APPLIED));
        assertEquals(1L, report.byType().get(AuditEventType.DUPLICATE_REPORT));
    }
}
