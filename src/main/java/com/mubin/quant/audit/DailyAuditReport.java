package com.mubin.quant.audit;

import java.time.LocalDate;
import java.util.Map;

public record DailyAuditReport(
        LocalDate date,
        int totalEvents,
        Map<AuditEventType, Long> byType
) {
}
