package org.example.smartmallbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name="订单管理", description = "订单相关接口")
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
    @Operation(summary = "分页查询订单列表", description = "支持按用户ID、订单状态、订单编号等条件筛选")
    @GetMapping("/page")
    public Result<Page<OmsOrder>> page(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单状态：0-待付款，1-待发货，2-已发货，3-已完成，4-已取消") @RequestParam(required = false) Integer status,
            @Parameter(description = "订单编号") @RequestParam(required = false) String orderSn) {

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
    @Operation(summary = "根据ID查询订单详情", description = "通过订单主键ID查询订单完整信息")
    @GetMapping("/{id}")
    public Result<OmsOrder> getById(@Parameter(description = "订单ID", required = true) @PathVariable Long id) {
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
    @Operation(summary = "根据订单编号查询", description = "通过订单编号查询订单完整信息")
    @GetMapping("/sn/{orderSn}")
    public Result<OmsOrder> getByOrderSn(@Parameter(description = "订单编号", required = true) @PathVariable String orderSn) {
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
    @Operation(summary = "查询订单明细", description = "根据订单ID查询订单明细列表")
    @GetMapping("/items/{orderId}")
    public Result<List<OmsOrderItem>> getOrderItems(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId) {
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
    @Operation(summary = "创建订单", description = "校验商品信息和库存，锁定库存，校验订单金额，创建订单和订单明细")
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
    @Operation(summary = "发起支付", description = "校验订单状态后返回支付参数")
    @PostMapping("/pay/initiate")
    public Result<?> initiatePayment(
            @Parameter(description = "订单编号", required = true) @RequestParam String orderSn,
            @Parameter(description = "支付方式：1-微信，2-支付宝，3-银联，4-余额", required = true) @RequestParam Integer payType) {
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
    @Operation(summary = "支付回调", description = "第三方支付平台支付成功后调用此接口，包含幂等性处理")
    @PostMapping("/pay/callback")
    public Result<String> paymentCallback(
            @Parameter(description = "订单编号", required = true) @RequestParam String orderSn,
            @Parameter(description = "第三方支付交易号", required = true) @RequestParam String transactionNo) {
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
    @Operation(summary = "取消订单", description = "取消订单并自动恢复库存")
    @PutMapping("/cancel")
    public Result<?> cancelOrder(@Parameter(description = "订单编号", required = true) @RequestParam String orderSn) {
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
    @Operation(summary = "订单发货", description = "校验订单和支付状态后发货")
    @PutMapping("/deliver")
    public Result<?> deliver(@Parameter(description = "订单编号", required = true) @RequestParam String orderSn) {
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
    @Operation(summary = "确认收货", description = "用户确认收货，订单状态更新为已完成")
    @PutMapping("/confirm/receive")
    public Result<?> confirmReceive(@Parameter(description = "订单编号", required = true) @RequestParam String orderSn) {
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
    @Operation(summary = "更新订单", description = "仅允许更新收货信息，订单未发货时才可修改")
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
    @Operation(summary = "删除订单", description = "逻辑删除订单，只能删除已完成或已取消的订单")
    @DeleteMapping("/{id}")
    public Result<?> delete(@Parameter(description = "订单ID", required = true) @PathVariable Long id) {
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
    @Operation(summary = "根据用户ID查询订单", description = "查询指定用户的所有订单，按创建时间倒序")
    @GetMapping("/user/{userId}")
    public Result<List<OmsOrder>> listByUserId(@Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
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
    @Operation(summary = "查询订单状态枚举", description = "获取所有订单状态枚举值")
    @GetMapping("/status/list")
    public Result<List<OrderStatus>> getOrderStatusList() {
        return Result.success(Arrays.asList(OrderStatus.values()));
    }

    /**
     * 查询支付状态枚举
     *
     * @return 状态列表
     */
    @Operation(summary = "查询支付状态枚举", description = "获取所有支付状态枚举值")
    @GetMapping("/pay-status/list")
    public Result<List<PayStatus>> getPayStatusList() {
        return Result.success(Arrays.asList(PayStatus.values()));
    }

    /**
     * 查询支付方式枚举
     *
     * @return 支付方式列表
     */
    @Operation(summary = "查询支付方式枚举", description = "获取所有支付方式枚举值")
    @GetMapping("/pay-type/list")
    public Result<List<PayType>> getPayTypeList() {
        return Result.success(Arrays.asList(PayType.values()));
    }
}
