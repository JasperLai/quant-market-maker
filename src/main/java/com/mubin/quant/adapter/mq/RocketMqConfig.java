package com.mubin.quant.adapter.mq;

public record RocketMqConfig(
        String consumerGroup,
        String nameServerAddress,
        String topic,
        String tagExpression
) {
}
