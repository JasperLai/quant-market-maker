package com.mubin.quant.adapter.mq;

import com.mubin.quant.core.MarketDataService;
import com.mubin.quant.ingest.PlatformQuoteMessage;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RocketMqQuoteConsumer implements AutoCloseable {

    private final RocketMqConfig config;
    private final PlatformQuoteParser parser;
    private final MarketDataService marketDataService;
    private DefaultMQPushConsumer consumer;

    public RocketMqQuoteConsumer(RocketMqConfig config, PlatformQuoteParser parser, MarketDataService marketDataService) {
        this.config = config;
        this.parser = parser;
        this.marketDataService = marketDataService;
    }

    public synchronized void start() throws MQClientException {
        if (consumer != null) {
            return;
        }

        consumer = new DefaultMQPushConsumer(config.consumerGroup());
        consumer.setNamesrvAddr(config.nameServerAddress());
        consumer.subscribe(config.topic(), config.tagExpression());
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    String raw = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        PlatformQuoteMessage parsed = parser.parse(raw);
                        marketDataService.onPlatformQuote(parsed);
                    } catch (Exception e) {
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }

    public synchronized void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
            consumer = null;
        }
    }

    @Override
    public void close() {
        shutdown();
    }
}
