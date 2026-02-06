package com.mubin.quant.adapter;

import com.mubin.quant.adapter.mq.RocketMqQuoteConsumer;
import com.mubin.quant.adapter.so.SoQuoteBridge;

public class IngestionRuntime implements AutoCloseable {

    private final RocketMqQuoteConsumer mqConsumer;
    private final SoQuoteBridge soQuoteBridge;

    public IngestionRuntime(RocketMqQuoteConsumer mqConsumer, SoQuoteBridge soQuoteBridge) {
        this.mqConsumer = mqConsumer;
        this.soQuoteBridge = soQuoteBridge;
    }

    public void start() throws Exception {
        soQuoteBridge.start();
        mqConsumer.start();
    }

    public void stop() {
        mqConsumer.shutdown();
        soQuoteBridge.stop();
    }

    @Override
    public void close() {
        stop();
    }
}
