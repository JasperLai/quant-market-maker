package com.mubin.quant.audit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryAuditSink implements AuditSink {

    private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void publish(AuditEvent event) {
        events.add(event);
    }

    @Override
    public List<AuditEvent> findByDate(LocalDate date, ZoneId zoneId) {
        List<AuditEvent> result = new ArrayList<>();
        for (AuditEvent event : events) {
            if (event.bizDate(zoneId).equals(date)) {
                result.add(event);
            }
        }
        return result;
    }
}
