package com.mubin.quant.lifecycle;

import com.mubin.quant.audit.InMemoryAuditSink;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderStateMachine;
import com.mubin.quant.domain.OrderStatus;
import com.mubin.quant.domain.Side;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RfqLifecycleManagerTest {

    @Test
    void shouldExpireOrderAfterTimeout() {
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        RfqLifecycleManager manager = new RfqLifecycleManager(20_000, new OrderStateMachine(), auditSink);

        Order order = new Order("rfq-1", AssetClass.FX, "USDCNY", Side.BUY, 7.2, 1_000_000, 1_000_000);
        Map<String, Order> orders = new HashMap<>();
        orders.put(order.getOrderId(), order);

        long sentTs = 100_000L;
        manager.registerRfq(order.getOrderId(), sentTs);

        manager.expireDueOrders(sentTs + 20_001, orders);

        assertEquals(OrderStatus.EXPIRED, order.getStatus());
    }
}
