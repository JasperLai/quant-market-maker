package com.mubin.quant.execution;

import com.mubin.quant.domain.OrderEventType;

public record ExecutionReport(
        String venue,
        String orderId,
        String execId,
        String tradeId,
        OrderEventType eventType,
        double lastFilledQty,
        long businessTs,
        long receiveTs
) {
    public String dedupKey() {
        String uniquePart = (execId != null && !execId.isBlank()) ? execId : tradeId;
        return venue + "|" + orderId + "|" + uniquePart;
    }
}
