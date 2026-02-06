package com.mubin.quant.core;

import com.mubin.quant.audit.InMemoryAuditSink;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderEventType;
import com.mubin.quant.domain.OrderStatus;
import com.mubin.quant.domain.Side;
import com.mubin.quant.execution.ExecutionReport;
import com.mubin.quant.recon.VenueOrderSnapshot;
import com.mubin.quant.risk.DailyExposureTracker;
import com.mubin.quant.risk.PreTradeRiskEngine;
import com.mubin.quant.risk.RiskConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradingCoreServiceTest {

    @Test
    void shouldRunSubmitFillAndReconcileFlow() {
        PreTradeRiskEngine risk = new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker());
        TradingCoreService service = new TradingCoreService(risk, new InMemoryAuditSink(), 1000, 20_000);

        Order order = new Order("o1", AssetClass.FX, "USDCNY", Side.BUY, 7.20, 1_000_000, 1_000_000);
        SubmitOrderResult submit = service.submitOrder(order, 7.20, true, 10_000);
        assertTrue(submit.accepted());

        service.onExecutionReport(new ExecutionReport("CFETS", "o1", "e1", "t1", OrderEventType.ACK, 0, 10_010, 10_010));
        service.onExecutionReport(new ExecutionReport("CFETS", "o1", "e2", "t2", OrderEventType.FILL, 1_000_000, 10_020, 10_020));
        service.flushExecutionBuffer();

        assertEquals(OrderStatus.FILLED, service.getOrder("o1").getStatus());

        List<String> corrected = service.reconcile(List.of(new VenueOrderSnapshot("o1", OrderStatus.FILLED, 1_000_000)), 11_000);
        assertEquals(0, corrected.size());
    }

    @Test
    void shouldExpireRfqWhenTimeout() {
        PreTradeRiskEngine risk = new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker());
        TradingCoreService service = new TradingCoreService(risk, new InMemoryAuditSink(), 1000, 20_000);

        Order order = new Order("rfq-o1", AssetClass.FX, "USDCNY", Side.BUY, 7.20, 100_000, 100_000);
        SubmitOrderResult submit = service.submitOrder(order, 7.20, true, 1000);
        assertTrue(submit.accepted());

        service.runRfqTimeoutCheck(21_001);

        assertEquals(OrderStatus.EXPIRED, service.getOrder("rfq-o1").getStatus());
    }
}
