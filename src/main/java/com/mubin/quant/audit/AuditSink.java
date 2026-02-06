package com.mubin.quant.audit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface AuditSink {
    void publish(AuditEvent event);

    List<AuditEvent> findByDate(LocalDate date, ZoneId zoneId);
}
