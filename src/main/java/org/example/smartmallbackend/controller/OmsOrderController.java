package org.example.smartmallbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.dto.OmsOrderSaveDTO;
import org.example.smartmallbackend.dto.OmsOrderUpdateDTO;
import org.example.smartmallbackend.entity.OmsOrder;
import org.example.smartmallbackend.entity.OmsOrderItem;
import org.example.smartmallbackend.enums.OrderStatus;
import org.example.smartmallbackend.enums.PayStatus;
import org.example.smartmallbackend.enums.PayType;
import org.example.smartmallbackend.service.IOrderService;
import org.example.smartmallbackend.service.OmsOrderItemService;
import org.example.smartmallbackend.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 订单管理Controller
 *
 * @author smart-mall-backend
 * @description 订单管理接口，包含下单、支付、发货、退款等完整流程
 */
@Validated
@RestController
@RequestMapping("/api/oms/order")
public class OmsOrderController {

    @Autowired
    private OmsOrderService omsOrderService;

    @Autowired
    private OmsOrderItemService omsOrderItemService;

    @Autowired
    private IOrderService orderService;

    /**
     * 分页查询订单列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @param userId  用户ID
     * @param status  订单状态
     * @param orderSn 订单编号
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Page<OmsOrder>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderSn) {

        Page<OmsOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(userId != null, OmsOrder::getUserId, userId)
                .eq(status != null, OmsOrder::getStatus, status)
                .like(orderSn != null, OmsOrder::getOrderSn, orderSn)
                .orderByDesc(OmsOrder::getCreateTime);

        omsOrderService.page(page, wrapper);
        return Result.success(page);
    }

    /**
     * 根据ID查询订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<OmsOrder> getById(@PathVariable Long id) {
        OmsOrder order = omsOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 根据订单编号查询订单详情
     *
     * @param orderSn 订单编号
     * @return 订单详情
     */
    @GetMapping("/sn/{orderSn}")
    public Result<OmsOrder> getByOrderSn(@PathVariable String orderSn) {
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrder::getOrderSn, orderSn);
        OmsOrder order = omsOrderService.getOne(wrapper);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 根据订单ID查询订单明细
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @GetMapping("/items/{orderId}")
    public Result<List<OmsOrderItem>> getOrderItems(@PathVariable Long orderId) {
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderItem::getOrderId, orderId);
        List<OmsOrderItem> items = omsOrderItemService.list(wrapper);
        return Result.success(items);
    }

    /**
     * 创建订单
     * 业务逻辑：
     * 1. 校验商品信息和库存
     * 2. 锁定库存（使用行锁防止超卖）
     * 3. 校验订单金额（防止价格篡改）
     * 4. 创建订单和订单明细
     *
     * @param dto 订单信息
     * @return 订单编号
     */
    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody @Validated OmsOrderSaveDTO dto) {
        try {
            String orderSn = orderService.createOrder(dto);
            return Result.success("下单成功", orderSn);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发起支付
     * 校验订单状态后返回支付参数
     *
     * @param orderSn 订单编号
     * @param payType 支付方式（1-微信，2-支付宝，3-银联，4-余额）
     * @return 支付参数
     */
    @PostMapping("/pay/initiate")
    public Result<?> initiatePayment(
            @RequestParam String orderSn,
            @RequestParam Integer payType) {
        try {
            orderService.initiatePayment(orderSn, payType);
            // TODO: 返回实际的支付参数
            return Result.success("支付发起成功", orderSn);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付回调接口
     * 第三方支付平台支付成功后调用此接口
     * 包含幂等性处理，防止重复回调
     *
     * @param orderSn 订单编号
     * @param transactionNo 第三方支付交易号
     * @return 处理结果
     */
    @PostMapping("/pay/callback")
    public Result<String> paymentCallback(
            @RequestParam String orderSn,
            @RequestParam String transactionNo) {
        try {
            orderService.handlePaymentSuccess(orderSn, transactionNo);
            return Result.success("支付回调处理成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消订单
     * 自动恢复库存
     *
     * @param orderSn 订单编号
     * @return 操作结果
     */
    @PutMapping("/cancel")
    public Result<?> cancelOrder(@RequestParam String orderSn) {
        try {
            orderService.cancelOrder(orderSn);
            return Result.success("订单取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发货
     * 校验订单和支付状态
     *
     * @param orderSn 订单编号
     * @return 操作结果
     */
    @PutMapping("/deliver")
    public Result<?> deliver(@RequestParam String orderSn) {
        try {
            orderService.deliverOrder(orderSn);
            return Result.success("发货成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认收货
     *
     * @param orderSn 订单编号
     * @return 操作结果
     */
    @PutMapping("/confirm/receive")
    public Result<?> confirmReceive(@RequestParam String orderSn) {
        try {
            orderService.confirmReceive(orderSn);
            return Result.success("确认收货成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新订单
     * 仅允许更新部分字段
     *
     * @param dto 订单信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody @Validated OmsOrderUpdateDTO dto) {
        OmsOrder order = omsOrderService.getById(dto.getId());
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 只允许更新收货信息（订单未发货时）
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus()) &&
                !OrderStatus.PENDING_DELIVERY.getCode().equals(order.getStatus())) {
            return Result.error("订单已发货，无法修改收货信息");
        }

        try {
            cn.hutool.core.bean.BeanUtil.copyProperties(dto, order);
            omsOrderService.updateById(order);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除订单（逻辑删除）
     * 只能删除已完成或已取消的订单
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        OmsOrder order = omsOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 只允许删除已完成或已取消的订单
        if (!OrderStatus.COMPLETED.getCode().equals(order.getStatus()) &&
                !OrderStatus.CANCELLED.getCode().equals(order.getStatus())) {
            return Result.error("只能删除已完成或已取消的订单");
        }

        boolean success = omsOrderService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<OmsOrder>> listByUserId(@PathVariable Long userId) {
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrder::getUserId, userId)
                .orderByDesc(OmsOrder::getCreateTime);
        List<OmsOrder> list = omsOrderService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 查询订单状态枚举
     *
     * @return 状态列表
     */
    @GetMapping("/status/list")
    public Result<List<OrderStatus>> getOrderStatusList() {
        return Result.success(Arrays.asList(OrderStatus.values()));
    }

    /**
     * 查询支付状态枚举
     *
     * @return 状态列表
     */
    @GetMapping("/pay-status/list")
    public Result<List<PayStatus>> getPayStatusList() {
        return Result.success(Arrays.asList(PayStatus.values()));
    }

    /**
     * 查询支付方式枚举
     *
     * @return 支付方式列表
     */
    @GetMapping("/pay-type/list")
    public Result<List<PayType>> getPayTypeList() {
        return Result.success(Arrays.asList(PayType.values()));
    }
}
