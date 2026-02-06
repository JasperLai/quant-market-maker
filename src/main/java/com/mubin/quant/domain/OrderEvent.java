package com.mubin.quant.domain;

public record OrderEvent(OrderEventType type, long eventTs) {
}
