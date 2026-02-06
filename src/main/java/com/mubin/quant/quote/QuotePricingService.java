package com.mubin.quant.quote;

public class QuotePricingService {

    public double adjustFxByBps(double midPrice, double bpsDelta) {
        return midPrice * (1.0 + bpsDelta / 10_000.0);
    }

    public double adjustGoldByTicks(double midPrice, int ticks, double tickSize) {
        return midPrice + ticks * tickSize;
    }
}
