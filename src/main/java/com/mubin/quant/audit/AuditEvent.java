package com.mubin.quant.audit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public record AuditEvent(
        String auditId,
        AuditEventType type,
        String entityId,
        long eventTs,
        String message
) {
    public static AuditEvent of(AuditEventType type, String entityId, long eventTs, String message) {
        return new AuditEvent(UUID.randomUUID().toString(), type, entityId, eventTs, message);
    }

    public LocalDate bizDate(ZoneId zoneId) {
        return Instant.ofEpochMilli(eventTs).atZone(zoneId).toLocalDate();
    }
}
