package com.mubin.quant.recon;

import com.mubin.quant.audit.AuditEvent;
import com.mubin.quant.audit.AuditEventType;
import com.mubin.quant.audit.AuditSink;
import com.mubin.quant.domain.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReconciliationJob {

    private final AuditSink auditSink;

    public ReconciliationJob(AuditSink auditSink) {
        this.auditSink = auditSink;
    }

    public List<String> reconcile(Map<String, Order> localOrders, List<VenueOrderSnapshot> venueSnapshots, long eventTs) {
        List<String> corrected = new ArrayList<>();

        for (VenueOrderSnapshot venue : venueSnapshots) {
            Order local = localOrders.get(venue.orderId());
            if (local == null) {
                auditSink.publish(AuditEvent.of(
                        AuditEventType.RECON_DIFF_DETECTED,
                        venue.orderId(),
                        eventTs,
                        "Venue order not found locally"
                ));
                continue;
            }

            boolean diffStatus = local.getStatus() != venue.status();
            boolean diffFillQty = Double.compare(local.getFilledQuantity(), venue.filledQuantity()) != 0;
            if (!diffStatus && !diffFillQty) {
                continue;
            }

            auditSink.publish(AuditEvent.of(
                    AuditEventType.RECON_DIFF_DETECTED,
                    venue.orderId(),
                    eventTs,
                    "Diff found local(status=" + local.getStatus() + ",filled=" + local.getFilledQuantity()
                            + ") venue(status=" + venue.status() + ",filled=" + venue.filledQuantity() + ")"
            ));

            // Venue snapshot is treated as the source of truth during correction.
            local.setStatus(venue.status());
            resetFilled(local, venue.filledQuantity());
            corrected.add(venue.orderId());

            auditSink.publish(AuditEvent.of(
                    AuditEventType.RECON_STATE_CORRECTED,
                    venue.orderId(),
                    eventTs,
                    "Local state corrected to venue snapshot"
            ));
        }

        return corrected;
    }

    private void resetFilled(Order order, double expectedFilled) {
        double delta = expectedFilled - order.getFilledQuantity();
        order.addFilledQuantity(delta);
    }
}
