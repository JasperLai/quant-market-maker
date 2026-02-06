package com.mubin.quant.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStateMachineTest {

    private final OrderStateMachine stateMachine = new OrderStateMachine();

    @Test
    void shouldTransitionFromNewToAcked() {
        OrderStatus next = stateMachine.apply(OrderStatus.NEW, OrderEventType.ACK);
        assertEquals(OrderStatus.ACKED, next);
    }

    @Test
    void shouldNotRollbackOnInvalidEvent() {
        OrderStatus next = stateMachine.apply(OrderStatus.FILLED, OrderEventType.ACK);
        assertEquals(OrderStatus.FILLED, next);
    }

    @Test
    void shouldExpireRfqOrderOnTimeout() {
        OrderStatus next = stateMachine.apply(OrderStatus.ACKED, OrderEventType.TIMEOUT);
        assertEquals(OrderStatus.EXPIRED, next);
    }
}
