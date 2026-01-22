package org.example.smartmallbackend.listener;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.dto.OmsOrderSaveDTO;
import org.example.smartmallbackend.entity.OmsOrder;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.enums.OrderStatus;
import org.example.smartmallbackend.service.IOrderService;
import org.example.smartmallbackend.service.OmsOrderService;
import org.example.smartmallbackend.service.PmsSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderReceiver {
    private final IOrderService orderService;
    private final OmsOrderService omsOrderService;
    private final PmsSkuService pmsSkuService;
    private final StringRedisTemplate redisTemplate;

    @RabbitListener(queues = "seckill_order_queue")
    public void handle(String msgStr, Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        log.info("收到秒杀下单消息: {}", msgStr);
        try{
            JSONObject json= JSONUtil.parseObj(msgStr);
            Long userId = json.getLong("userId");
            Long skuId = json.getLong("skuId");
            //幂等性检查
            long count = omsOrderService.count(new LambdaQueryWrapper<OmsOrder>()
                    .eq(OmsOrder::getUserId,userId)
                    .eq(OmsOrder::getStatus, OrderStatus.PENDING_PAYMENT.getCode())
            );
            if(count>0){
                log.warn("用户已存在未支付秒杀订单，不能重复下单，userId: {}, skuId: {}",userId,skuId);
                channel.basicAck(tag,false);
                return;
            }
            //创建秒杀订单
            PmsSku sku=pmsSkuService.getById(skuId);
            if(sku==null){
                log.warn("秒杀商品不存在，skuId: {}",skuId);
                channel.basicAck(tag,false);
                return;
            }
            OmsOrderSaveDTO orderDto=new OmsOrderSaveDTO();
            orderDto.setUserId(userId);
            orderDto.setTotalAmount(sku.getPrice());
            orderDto.setPayAmount(sku.getPrice());
            // 这里填写一个默认地址，或者查询用户的默认地址表
            orderDto.setReceiverName("秒杀用户");
            orderDto.setReceiverPhone("13900000000");
            orderDto.setReceiverAddress("秒杀专用极速发货通道");
            //构造订单详情
            OmsOrderSaveDTO.OrderItemDTO orderItem=new OmsOrderSaveDTO.OrderItemDTO();
            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setSpuName(sku.getName());
            orderItem.setSkuPic(sku.getPicUrl());
            orderItem.setQuantity(1);
            orderDto.setOrderItems(Collections.singletonList(orderItem));
            //调用订单服务创建订单
            orderService.createOrder(orderDto);
            log.info("秒杀订单创建成功，userId: {}, skuId: {}",userId,skuId);
            //手动ack
            channel.basicAck(tag,false);
        }catch (Exception e){
            log.error("处理秒杀下单消息失败: {}", e.getMessage());
            // 发生异常，拒绝消息并丢弃 (false, false)，或者重回队列 (false, true)
            try{
                JSONObject json= JSONUtil.parseObj(msgStr);
                Long userId = json.getLong("userId");
                Long skuId = json.getLong("skuId");
                //回滚库存和购买资格
                String stockKey = "seckill:stock:" + skuId;
                String userKey = "seckill:bought:" + skuId;
                redisTemplate.opsForValue().increment(stockKey,1);
                redisTemplate.opsForSet().remove(userKey,String.valueOf(userId));
                log.warn("秒杀订单创建失败，已回滚库存和购买资格，userId: {}, skuId: {}",userId,skuId);
            }catch (Exception ex){
                log.error("回滚秒杀库存和购买资格失败: {}", ex.getMessage());
            }
            channel.basicReject(tag,false);
        }
    }

}
