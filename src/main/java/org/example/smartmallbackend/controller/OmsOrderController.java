package org.example.smartmallbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.dto.OmsOrderSaveDTO;
import org.example.smartmallbackend.dto.OmsOrderUpdateDTO;
import org.example.smartmallbackend.entity.OmsOrder;
import org.example.smartmallbackend.entity.OmsOrderItem;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.service.OmsOrderItemService;
import org.example.smartmallbackend.service.OmsOrderService;
import org.example.smartmallbackend.service.PmsSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单管理Controller
 *
 * @author smart-mall-backend
 * @description 订单的增删改查接口，包含下单、支付、发货等流程
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
    private PmsSkuService pmsSkuService;

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
     *
     * @param dto 订单信息
     * @return 操作结果
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createOrder(@RequestBody @Validated OmsOrderSaveDTO dto) {
        // 1. 校验库存并扣减
        for (OmsOrderSaveDTO.OrderItemDTO itemDto : dto.getOrderItems()) {
            PmsSku sku = pmsSkuService.getById(itemDto.getSkuId());
            if (sku == null) {
                return Result.error("商品SKU不存在");
            }
            if (sku.getStock() < itemDto.getQuantity()) {
                return Result.error("商品库存不足");
            }
            // 扣减库存
            sku.setStock(sku.getStock() - itemDto.getQuantity());
            pmsSkuService.updateById(sku);
        }

        // 2. 创建订单
        OmsOrder order = new OmsOrder();
        order.setOrderSn(generateOrderSn());
        order.setUserId(dto.getUserId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setPayAmount(dto.getPayAmount());
        order.setStatus(0); // 待付款
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setCreateTime(LocalDateTime.now());
        omsOrderService.save(order);

        // 3. 创建订单明细
        List<OmsOrderItem> orderItems = new ArrayList<>();
        for (OmsOrderSaveDTO.OrderItemDTO itemDto : dto.getOrderItems()) {
            OmsOrderItem item = new OmsOrderItem();
            item.setOrderId(order.getId());
            item.setOrderSn(order.getOrderSn());
            item.setSpuId(itemDto.getSpuId());
            item.setSkuId(itemDto.getSkuId());
            item.setSpuName(itemDto.getSpuName());
            item.setSkuPic(itemDto.getSkuPic());
            item.setSkuPrice(itemDto.getSkuPrice());
            item.setQuantity(itemDto.getQuantity());
            item.setSkuAttrs(itemDto.getSkuAttrs());
            orderItems.add(item);
        }
        omsOrderItemService.saveBatch(orderItems);

        return Result.success("下单成功", order.getOrderSn());
    }

    /**
     * 更新订单
     *
     * @param dto 订单信息
     * @return 操作结果
     */
    @PutMapping
    public Result<String> update(@RequestBody @Validated OmsOrderUpdateDTO dto) {
        OmsOrder order = BeanUtil.copyProperties(dto, OmsOrder.class);
        boolean success = omsOrderService.updateById(order);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 取消订单
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @PutMapping("/cancel/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> cancelOrder(@PathVariable Long id) {
        OmsOrder order = omsOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error("只有待付款订单可以取消");
        }

        // 恢复库存
        LambdaQueryWrapper<OmsOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OmsOrderItem::getOrderId, id);
        List<OmsOrderItem> items = omsOrderItemService.list(itemWrapper);
        for (OmsOrderItem item : items) {
            PmsSku sku = pmsSkuService.getById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                pmsSkuService.updateById(sku);
            }
        }

        // 更新订单状态
        order.setStatus(4); // 已取消
        omsOrderService.updateById(order);

        return Result.success("订单取消成功");
    }

    /**
     * 支付订单
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @PutMapping("/pay/{id}")
    public Result<?> payOrder(@PathVariable Long id) {
        OmsOrder order = omsOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态不正确");
        }

        order.setStatus(1); // 待发货
        order.setPaymentTime(LocalDateTime.now());
        boolean success = omsOrderService.updateById(order);
        return success ? Result.success("支付成功") : Result.error("支付失败");
    }

    /**
     * 发货
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @PutMapping("/deliver/{id}")
    public Result<?> deliver(@PathVariable Long id) {
        OmsOrder order = omsOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 1) {
            return Result.error("订单状态不正确");
        }

        order.setStatus(2); // 已发货
        order.setDeliveryTime(LocalDateTime.now());
        boolean success = omsOrderService.updateById(order);
        return success ? Result.success("发货成功") : Result.error("发货失败");
    }

    /**
     * 完成订单
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @PutMapping("/complete/{id}")
    public Result<?> complete(@PathVariable Long id) {
        OmsOrder order = omsOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 2) {
            return Result.error("订单状态不正确");
        }

        order.setStatus(3); // 已完成
        boolean success = omsOrderService.updateById(order);
        return success ? Result.success("订单完成") : Result.error("操作失败");
    }

    /**
     * 删除订单
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
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
     * 生成订单编号
     *
     * @return 订单编号
     */
    private String generateOrderSn() {
        return "ORD" + System.currentTimeMillis() + IdUtil.getSnowflake(1, 1).nextId();
    }
}
