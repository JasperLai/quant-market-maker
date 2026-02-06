package com.mubin.quant.adapter.so;

public interface SoQuoteBridge extends AutoCloseable {
    void start() throws Exception;

    void stop();

    @Override
    default void close() {
        stop();
    }
}
