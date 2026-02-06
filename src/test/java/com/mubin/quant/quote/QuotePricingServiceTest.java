package com.mubin.quant.quote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuotePricingServiceTest {

    @Test
    void shouldAdjustFxByBps() {
        QuotePricingService service = new QuotePricingService();
        double px = service.adjustFxByBps(7.2000, 10.0);
        assertEquals(7.2072, px, 0.000001);
    }

    @Test
    void shouldAdjustGoldByTicks() {
        QuotePricingService service = new QuotePricingService();
        double px = service.adjustGoldByTicks(520.00, 3, 0.02);
        assertEquals(520.06, px, 0.000001);
    }
}
