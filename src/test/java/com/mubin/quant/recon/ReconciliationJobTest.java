package com.mubin.quant.recon;

import com.mubin.quant.audit.InMemoryAuditSink;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderStatus;
import com.mubin.quant.domain.Side;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconciliationJobTest {

    @Test
    void shouldCorrectLocalStateFromVenueSnapshot() {
        ReconciliationJob job = new ReconciliationJob(new InMemoryAuditSink());

        Order order = new Order("o1", AssetClass.FX, "USDCNY", Side.BUY, 7.2, 1_000_000, 1_000_000);
        order.setStatus(OrderStatus.ACKED);

        Map<String, Order> local = Map.of(order.getOrderId(), order);
        List<VenueOrderSnapshot> venue = List.of(new VenueOrderSnapshot("o1", OrderStatus.FILLED, 1_000_000));

        List<String> corrected = job.reconcile(local, venue, System.currentTimeMillis());

        assertEquals(1, corrected.size());
        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertEquals(1_000_000, order.getFilledQuantity());
    }
}
