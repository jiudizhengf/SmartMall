package org.example.smartmallbackend.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.common.BusinessException;
import org.example.smartmallbackend.config.RabbitMqConfig;
import org.example.smartmallbackend.dto.OmsOrderSaveDTO;
import org.example.smartmallbackend.entity.OmsOrder;
import org.example.smartmallbackend.entity.OmsOrderItem;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.enums.OrderStatus;
import org.example.smartmallbackend.enums.PayStatus;
import org.example.smartmallbackend.enums.PayType;
import org.example.smartmallbackend.service.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单业务服务实现类
 *
 * @author smart-mall-backend
 * @description 处理订单核心业务逻辑，包括库存锁定、支付处理、状态流转等
 */
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OmsOrderService omsOrderService;

    @Autowired
    private OmsOrderItemService omsOrderItemService;

    @Autowired
    private PmsSkuService pmsSkuService;

    @Autowired
    private OmsCartItemService cartItemService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public String createOrder(OmsOrderSaveDTO dto) {
        //准备锁
        List<RLock> locks = new ArrayList<>();
        for(OmsOrderSaveDTO.OrderItemDTO item:dto.getOrderItems()){
            RLock lock = redissonClient.getLock("product:lock:"+item.getSkuId());
            locks.add(lock);
        }
        //联锁,所有商品库存一起锁
        RLock multiLock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
        try{
            //加锁
            boolean isLocked = multiLock.tryLock(3,30, TimeUnit.SECONDS);
            if(!isLocked){
                throw new BusinessException("当前抢购人数过多，请稍后再试");
            }
            String orderSn = transactionTemplate.execute(status -> {
                try{
                    // 1. 校验并计算金额
                    BigDecimal calculatedTotalAmount = BigDecimal.ZERO;
                    for (OmsOrderSaveDTO.OrderItemDTO itemDto : dto.getOrderItems()) {
                        // 校验商品是否存在
                        PmsSku sku = pmsSkuService.getById(itemDto.getSkuId());
                        if (sku == null) {
                            throw new BusinessException("商品不存在，SKU ID: " + itemDto.getSkuId());
                        }

                        // 校验商品是否已下架
                        if (sku.getIsDeleted() == 1) {
                            throw new BusinessException("商品已下架: " + sku.getName());
                        }

                        // 校验库存
                        if (sku.getStock() < itemDto.getQuantity()) {
                            throw new BusinessException("商品库存不足: " + sku.getName() + "，当前库存: " + sku.getStock());
                        }

                        // 校验价格是否一致（防止价格篡改）
                        if (sku.getPrice().compareTo(itemDto.getSkuPrice()) != 0) {
                            throw new BusinessException("商品价格已发生变化，请重新下单: " + sku.getName());
                        }

                        // 累计金额
                        calculatedTotalAmount = calculatedTotalAmount.add(
                                sku.getPrice().multiply(new BigDecimal(itemDto.getQuantity()))
                        );
                    }

                    // 校验订单金额
                    if (dto.getTotalAmount().compareTo(calculatedTotalAmount) != 0) {
                        throw new BusinessException("订单金额不正确，请重新确认");
                    }

                    // 2. 锁定库存（使用数据库行锁，防止超卖）
                    for (OmsOrderSaveDTO.OrderItemDTO itemDto : dto.getOrderItems()) {
                        boolean stockUpdated = pmsSkuService.lambdaUpdate()
                                .eq(PmsSku::getId, itemDto.getSkuId())
                                .ge(PmsSku::getStock, itemDto.getQuantity())
                                .setSql("stock = stock - " + itemDto.getQuantity())
                                .update();
                        if (!stockUpdated) {
                            throw new BusinessException("扣减库存失败");
                        }
                    }

                    // 3. 创建订单
                    OmsOrder order = new OmsOrder();
                    order.setOrderSn(generateOrderSn());
                    order.setUserId(dto.getUserId());
                    order.setTotalAmount(dto.getTotalAmount());
                    order.setPayAmount(dto.getPayAmount());
                    order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
                    order.setPayStatus(PayStatus.UNPAID.getCode());
                    order.setReceiverName(dto.getReceiverName());
                    order.setReceiverPhone(dto.getReceiverPhone());
                    order.setReceiverAddress(dto.getReceiverAddress());
                    order.setCreateTime(LocalDateTime.now());
                    omsOrderService.save(order);

                    // 4. 创建订单明细
                    List<OmsOrderItem> orderItems = new ArrayList<>();
                    for (OmsOrderSaveDTO.OrderItemDTO itemDto : dto.getOrderItems()) {
                        OmsOrderItem item = new OmsOrderItem();
                        PmsSku skuItem = pmsSkuService.getById(itemDto.getSkuId());
                        item.setOrderId(order.getId());
                        item.setOrderSn(order.getOrderSn());
                        item.setSpuId(itemDto.getSpuId());
                        item.setSkuId(itemDto.getSkuId());
                        item.setSpuName(itemDto.getSpuName());
                        item.setSkuPic(skuItem.getPicUrl());
                        item.setSkuPrice(skuItem.getPrice());
                        item.setQuantity(itemDto.getQuantity());
                        item.setSkuAttrs(skuItem.getSpecData());
                        orderItems.add(item);
                    }
                    omsOrderItemService.saveBatch(orderItems);

                    // 5. 清空购物车(从购物车下单的情况)
                    if(dto.getCartItemIds()!=null&&!dto.getCartItemIds().isEmpty()){
                        cartItemService.removeByIds(dto.getCartItemIds());
                    }
                    return order.getOrderSn();
                }catch (Exception e){
                    status.setRollbackOnly();
                    throw e;
                }
            });
            //事务提交成功后，发送延迟消息
            // 发送到 order.event.exchange，路由键为 order.delay
            if (orderSn != null) {
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.ORDER_EVENT_EXCHANGE,
                        RabbitMqConfig.ORDER_DELAY_ROUTING_KEY,
                        orderSn // 消息内容就是订单号
                );
            }
            log.info("订单创建成功，已发送延迟取消消息，OrderSn: {}", orderSn);

            return orderSn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("服务器繁忙，请稍后再试");
        }finally {
            //释放锁
            if(multiLock.isHeldByCurrentThread()){
                multiLock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initiatePayment(String orderSn, Integer payType) {
        OmsOrder order = getOrderBySn(orderSn);

        // 校验订单状态
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        // 校验支付状态
        if (!PayStatus.UNPAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException("订单已支付或支付中");
        }

        // 校验支付方式
        if (PayType.getByCode(payType) == null) {
            throw new BusinessException("不支持的支付方式");
        }

        // 更新订单为支付中
        order.setPayStatus(PayStatus.PAYING.getCode());
        order.setPayType(payType);
        omsOrderService.updateById(order);

        // TODO: 调用第三方支付接口获取支付参数
        // return paymentService.createPayment(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(String orderSn, String transactionNo) {
        OmsOrder order = getOrderBySn(orderSn);

        // 幂等性校验：防止重复回调
        if (PayStatus.PAID.getCode().equals(order.getPayStatus())) {
            return;
        }

        // 校验订单状态
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.PENDING_DELIVERY.getCode());
        order.setPayStatus(PayStatus.PAID.getCode());
        order.setPaymentTransactionNo(transactionNo);
        order.setPaymentTime(LocalDateTime.now());
        omsOrderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderSn) {
        OmsOrder order = getOrderBySn(orderSn);

        // 校验订单状态
        OrderStatus orderStatus = OrderStatus.getByCode(order.getStatus());
        if (orderStatus == null || !orderStatus.canCancel()) {
            throw new BusinessException("当前订单状态不允许取消");
        }

        // 恢复库存
        LambdaQueryWrapper<OmsOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OmsOrderItem::getOrderId, order.getId());
        List<OmsOrderItem> items = omsOrderItemService.list(itemWrapper);

        for (OmsOrderItem item : items) {
            pmsSkuService.lambdaUpdate()
                    .eq(PmsSku::getId, item.getSkuId())
                    .setSql("stock = stock + " + item.getQuantity())
                    .update();
        }

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED.getCode());
        omsOrderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(String orderSn) {
        OmsOrder order = getOrderBySn(orderSn);

        // 校验订单状态
        if (!OrderStatus.PENDING_DELIVERY.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确，无法发货");
        }

        // 校验支付状态
        if (!PayStatus.PAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException("订单未支付，无法发货");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.DELIVERED.getCode());
        order.setDeliveryTime(LocalDateTime.now());
        omsOrderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(String orderSn) {
        OmsOrder order = getOrderBySn(orderSn);

        // 校验订单状态
        if (!OrderStatus.DELIVERED.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setReceivedTime(LocalDateTime.now());
        order.setFinishTime(LocalDateTime.now());
        omsOrderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);

        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrder::getStatus, OrderStatus.PENDING_PAYMENT.getCode())
                .eq(OmsOrder::getPayStatus, PayStatus.UNPAID.getCode())
                .lt(OmsOrder::getCreateTime, expireTime);

        List<OmsOrder> expiredOrders = omsOrderService.list(wrapper);
        for (OmsOrder order : expiredOrders) {
            try {
                cancelOrder(order.getOrderSn());
            } catch (Exception e) {
                // 记录日志，继续处理下一个
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据订单编号查询订单
     *
     * @param orderSn 订单编号
     * @return 订单
     */
    private OmsOrder getOrderBySn(String orderSn) {
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrder::getOrderSn, orderSn);
        OmsOrder order = omsOrderService.getOne(wrapper);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    /**
     * 生成订单编号
     *
     * @return 订单编号
     */
    private String generateOrderSn() {
        return "ORD" + System.currentTimeMillis() + IdUtil.getSnowflake(1, 1).nextId();
    }
}
