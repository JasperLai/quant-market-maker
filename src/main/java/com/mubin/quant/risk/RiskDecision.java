package com.mubin.quant.risk;

public record RiskDecision(boolean passed, String reason) {
    public static RiskDecision pass() {
        return new RiskDecision(true, "PASS");
    }

    public static RiskDecision fail(String reason) {
        return new RiskDecision(false, reason);
    }
}
