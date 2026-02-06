package com.mubin.quant.position;

import com.mubin.quant.audit.InMemoryAuditSink;
import com.mubin.quant.domain.AssetClass;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionServiceTest {

    @Test
    void shouldUpdateAndOffsetPosition() {
        PositionService service = new PositionService(new InMemoryAuditSink());
        long now = System.currentTimeMillis();

        Order buy = new Order("o1", AssetClass.GOLD, "AU99.99", Side.BUY, 520.0, 1000, 520_000);
        service.onTrade(buy, 1000, 520.0, now);

        PositionSnapshot first = service.snapshot("AU99.99");
        assertEquals(1000.0, first.netQuantity());
        assertEquals(520.0, first.avgPrice());

        Order sell = new Order("o2", AssetClass.GOLD, "AU99.99", Side.SELL, 521.0, 400, 208_400);
        service.onTrade(sell, 400, 521.0, now + 1);

        PositionSnapshot second = service.snapshot("AU99.99");
        assertEquals(600.0, second.netQuantity());
        assertEquals(520.0, second.avgPrice());
    }
}
