package com.mubin.quant.api;

import com.mubin.quant.book.BookManager;
import com.mubin.quant.book.BookRouter;
import com.mubin.quant.core.TradingCoreService;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.OrderEventType;
import com.mubin.quant.domain.Side;
import com.mubin.quant.execution.ExecutionReport;
import com.mubin.quant.risk.DailyExposureTracker;
import com.mubin.quant.risk.PreTradeRiskEngine;
import com.mubin.quant.risk.RiskConfig;
import com.mubin.quant.audit.InMemoryAuditSink;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TradingHttpServerTest {

    @Test
    void shouldServeHealthAndOrderQuery() throws Exception {
        TradingCoreService core = new TradingCoreService(
                new PreTradeRiskEngine(RiskConfig.defaultConfig(), new DailyExposureTracker()),
                new InMemoryAuditSink(),
                1000,
                20_000
        );

        Order order = new Order("http-o1", AssetClass.FX, "USDCNY", Side.BUY, 7.2, 1_000_000, 1_000_000);
        core.submitOrder(order, 7.2, false, System.currentTimeMillis());
        core.onExecutionReport(new ExecutionReport("CFETS", "http-o1", "e1", "t1", OrderEventType.ACK, 0,
                System.currentTimeMillis(), System.currentTimeMillis()));
        core.flushExecutionBuffer();

        TradingHttpServer server = new TradingHttpServer(18080, core, new BookManager(new BookRouter()));
        server.start();
        try {
            String health = get("http://127.0.0.1:18080/health");
            String orderResp = get("http://127.0.0.1:18080/orders?id=http-o1");

            assertTrue(health.contains("UP"));
            assertTrue(orderResp.contains("http-o1"));
            assertTrue(orderResp.contains("ACKED"));
        } finally {
            server.stop(0);
        }
    }

    private String get(String rawUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(rawUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
