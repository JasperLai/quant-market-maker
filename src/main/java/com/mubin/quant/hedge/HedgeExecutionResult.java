package com.mubin.quant.hedge;

import java.util.List;

public record HedgeExecutionResult(
        HedgePlan plan,
        List<String> submittedOrderIds
) {
}
