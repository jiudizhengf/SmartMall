package org.example.smartmallbackend.service;

import org.example.smartmallbackend.dto.OmsOrderSaveDTO;

/**
 * 订单业务服务接口
 *
 * @author smart-mall-backend
 * @description 定义订单核心业务逻辑，包括库存锁定、支付处理、状态流转等
 */
public interface IOrderService {

    /**
     * 订单超时时间（分钟）
     */
    long ORDER_TIMEOUT_MINUTES = 30;

    /**
     * 创建订单
     * 核心逻辑：
     * 1. 校验商品信息
     * 2. 校验并锁定库存（行锁防超卖）
     * 3. 计算并校验订单金额（防篡改）
     * 4. 创建订单和订单明细
     *
     * @param dto 订单信息
     * @return 订单编号
     */
    String createOrder(OmsOrderSaveDTO dto);

    /**
     * 发起支付
     * 校验订单状态并更新为支付中
     *
     * @param orderSn 订单编号
     * @param payType 支付方式
     */
    void initiatePayment(String orderSn, Integer payType);

    /**
     * 支付回调处理
     * 第三方支付成功后调用，含幂等性处理
     *
     * @param orderSn       订单编号
     * @param transactionNo 第三方支付交易号
     */
    void handlePaymentSuccess(String orderSn, String transactionNo);

    /**
     * 取消订单
     * 自动恢复库存
     *
     * @param orderSn 订单编号
     */
    void cancelOrder(String orderSn);

    /**
     * 发货
     * 校验订单和支付状态
     *
     * @param orderSn 订单编号
     */
    void deliverOrder(String orderSn);

    /**
     * 确认收货
     *
     * @param orderSn 订单编号
     */
    void confirmReceive(String orderSn);

    /**
     * 自动取消超时未支付订单
     * 定时任务调用
     */
    void cancelExpiredOrders();
}
