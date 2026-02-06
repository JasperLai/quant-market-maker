package com.mubin.quant.position;

import com.mubin.quant.audit.AuditEvent;
import com.mubin.quant.audit.AuditEventType;
import com.mubin.quant.audit.AuditSink;
import com.mubin.quant.domain.Order;
import com.mubin.quant.domain.Side;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PositionService {

    private final AuditSink auditSink;
    private final Map<String, MutablePosition> positionsBySymbol = new ConcurrentHashMap<>();

    public PositionService(AuditSink auditSink) {
        this.auditSink = auditSink;
    }

    public void onTrade(Order order, double executedQty, double executedPrice, long eventTs) {
        MutablePosition position = positionsBySymbol.computeIfAbsent(order.getSymbol(), ignored -> new MutablePosition());

        double signedQty = order.getSide() == Side.BUY ? executedQty : -executedQty;
        double currentNet = position.netQty;
        double nextNet = currentNet + signedQty;

        if (Double.compare(nextNet, 0.0) == 0) {
            position.netQty = 0.0;
            position.avgPrice = 0.0;
        } else if (currentNet == 0.0 || Math.signum(currentNet) == Math.signum(signedQty)) {
            double weightedAmount = Math.abs(currentNet) * position.avgPrice + Math.abs(signedQty) * executedPrice;
            position.netQty = nextNet;
            position.avgPrice = weightedAmount / Math.abs(nextNet);
        } else {
            // Opposite side trade partially/fully offsets existing position.
            position.netQty = nextNet;
            if (Math.signum(currentNet) != Math.signum(nextNet)) {
                position.avgPrice = executedPrice;
            }
        }

        auditSink.publish(AuditEvent.of(
                AuditEventType.POSITION_UPDATED,
                order.getSymbol(),
                eventTs,
                "Position updated, netQty=" + position.netQty + ", avgPrice=" + position.avgPrice
        ));
    }

    public PositionSnapshot snapshot(String symbol) {
        MutablePosition position = positionsBySymbol.get(symbol);
        if (position == null) {
            return new PositionSnapshot(symbol, 0.0, 0.0);
        }
        return new PositionSnapshot(symbol, position.netQty, position.avgPrice);
    }

    private static final class MutablePosition {
        private double netQty;
        private double avgPrice;
    }
}
