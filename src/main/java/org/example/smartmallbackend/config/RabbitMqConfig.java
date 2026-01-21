package org.example.smartmallbackend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    /**
     * 定义取消队列(实际被消费者监听的队列)
     */
    @Bean
    public Queue orderCancelQueue() {
        return new Queue(ORDER_CANCEL_QUEUE, true);
    }

    /**
     * 定义死信交换机
     */
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE);
    }
    /**
     * 绑定取消队列到死信交换机
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderDlxExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }
    /**
     * 定义延时队列
     */
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DLX_EXCHANGE) // 死信交换机
                .withArgument("x-dead-letter-routing-key", ORDER_CANCEL_ROUTING_KEY) // 死信路由键
                .withArgument("x-message-ttl", 30*60*1000)
                .build();
    }

    /**
     * 定义普通交换机
     */
    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange(ORDER_EVENT_EXCHANGE);
    }
    /**
     * 绑定延迟队列到普通交换机
     */
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderEventExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

}
