package com.mubin.quant.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class OrderStateMachine {

    private final Map<OrderStatus, Map<OrderEventType, OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);

    public OrderStateMachine() {
        putTransition(OrderStatus.NEW, OrderEventType.ACK, OrderStatus.ACKED);
        putTransition(OrderStatus.NEW, OrderEventType.REJECT, OrderStatus.REJECTED);

        putTransition(OrderStatus.ACKED, OrderEventType.PARTIAL_FILL, OrderStatus.PARTIAL_FILLED);
        putTransition(OrderStatus.ACKED, OrderEventType.FILL, OrderStatus.FILLED);
        putTransition(OrderStatus.ACKED, OrderEventType.CANCEL_REQUESTED, OrderStatus.CANCEL_PENDING);

        putTransition(OrderStatus.PARTIAL_FILLED, OrderEventType.PARTIAL_FILL, OrderStatus.PARTIAL_FILLED);
        putTransition(OrderStatus.PARTIAL_FILLED, OrderEventType.FILL, OrderStatus.FILLED);
        putTransition(OrderStatus.PARTIAL_FILLED, OrderEventType.CANCEL_REQUESTED, OrderStatus.CANCEL_PENDING);

        putTransition(OrderStatus.CANCEL_PENDING, OrderEventType.CANCELED, OrderStatus.CANCELED);

        Set<OrderStatus> rfqStates = EnumSet.of(OrderStatus.NEW, OrderStatus.ACKED);
        for (OrderStatus status : rfqStates) {
            putTransition(status, OrderEventType.TIMEOUT, OrderStatus.EXPIRED);
        }
    }

    public OrderStatus apply(OrderStatus current, OrderEventType eventType) {
        Map<OrderEventType, OrderStatus> eventToNext = transitions.get(current);
        if (eventToNext == null) {
            return current;
        }
        // Unknown transitions are intentionally no-op to enforce monotonic state progression.
        return eventToNext.getOrDefault(eventType, current);
    }

    private void putTransition(OrderStatus from, OrderEventType event, OrderStatus to) {
        transitions.computeIfAbsent(from, ignored -> new EnumMap<>(OrderEventType.class))
                .put(event, to);
    }
}
