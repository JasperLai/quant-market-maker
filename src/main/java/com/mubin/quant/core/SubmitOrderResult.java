package com.mubin.quant.core;

public record SubmitOrderResult(boolean accepted, String reason) {
    public static SubmitOrderResult accept() {
        return new SubmitOrderResult(true, "ACCEPTED");
    }

    public static SubmitOrderResult reject(String reason) {
        return new SubmitOrderResult(false, reason);
    }
}
