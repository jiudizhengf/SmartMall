package org.example.smartmallbackend.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.config.RabbitMqConfig;
import org.example.smartmallbackend.entity.OmsOrder;
import org.example.smartmallbackend.enums.OrderStatus;
import org.example.smartmallbackend.service.IOrderService;
import org.example.smartmallbackend.service.OmsOrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderTimeOutReceiver {
    @Autowired
    private IOrderService orderService;
    @Autowired
    private OmsOrderService omsOrderService;
    /**
     * 监听取消队列
     */
    @RabbitListener(queues = RabbitMqConfig.ORDER_CANCEL_QUEUE)
    public void handle(String orderSn, Message message, Channel channel) throws IOException {
        log.info("收到订单超时取消消息，OrderSn: {}", orderSn);
        try {
            // 1. 查询订单当前状态
            OmsOrder order = omsOrderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OmsOrder>()
                    .eq(OmsOrder::getOrderSn, orderSn));

            if (order == null) {
                log.warn("订单不存在，忽略消息");
                return;
            }

            // 2. 判断是否需要取消 (只有状态为 UNPAID 的才取消)
            if (OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
                orderService.cancelOrder(orderSn); // 调用你之前写好的取消逻辑（含回滚库存）
                log.info("订单已自动取消，库存已回滚: {}", orderSn);
            } else {
                log.info("订单状态正常，无需取消: {}", order.getStatus());
            }

            // 3. 手动确认消息 (ACK)
            // false 表示只确认当前这一条
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("处理订单超时消息失败: {}", e.getMessage());
            // 发生异常，拒绝消息并丢弃 (false, false)，或者重回队列 (false, true)
            // 这里为了防止死循环，选择丢弃
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
