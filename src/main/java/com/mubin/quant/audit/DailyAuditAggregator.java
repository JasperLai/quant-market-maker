package com.mubin.quant.audit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DailyAuditAggregator {

    private final AuditSink auditSink;
    private final ZoneId zoneId;

    public DailyAuditAggregator(AuditSink auditSink, ZoneId zoneId) {
        this.auditSink = auditSink;
        this.zoneId = zoneId;
    }

    public DailyAuditReport generate(LocalDate date) {
        List<AuditEvent> events = auditSink.findByDate(date, zoneId);
        Map<AuditEventType, Long> byType = new EnumMap<>(AuditEventType.class);
        for (AuditEvent event : events) {
            byType.merge(event.type(), 1L, Long::sum);
        }
        return new DailyAuditReport(date, events.size(), byType);
    }
}
