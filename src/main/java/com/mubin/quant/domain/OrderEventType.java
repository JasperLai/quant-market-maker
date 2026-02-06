package com.mubin.quant.domain;

public enum OrderEventType {
    ACK,
    PARTIAL_FILL,
    FILL,
    CANCEL_REQUESTED,
    CANCELED,
    REJECT,
    TIMEOUT
}
