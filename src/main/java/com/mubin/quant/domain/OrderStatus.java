package com.mubin.quant.domain;

public enum OrderStatus {
    NEW,
    ACKED,
    PARTIAL_FILLED,
    FILLED,
    CANCEL_PENDING,
    CANCELED,
    REJECTED,
    EXPIRED
}
