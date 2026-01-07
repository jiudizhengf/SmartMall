package org.example.smartmallbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.entity.OmsOrderItem;
import org.example.smartmallbackend.service.OmsOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单明细管理Controller
 *
 * @author smart-mall-backend
 * @description 订单明细的查询接口，订单明细一般不单独修改，随订单一起创建
 */
@Validated
@Tag(name = "订单明细管理", description = "订单明细的查询接口")
@RestController
@RequestMapping("/api/oms/order-item")
public class OmsOrderItemController {

    @Autowired
    private OmsOrderItemService omsOrderItemService;

    /**
     * 分页查询订单明细列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @param orderId 订单ID
     * @param orderSn 订单编号
     * @param spuId  SPU ID
     * @param skuId  SKU ID
     * @return 分页结果
     */
    @Operation(summary = "分页查询订单明细列表", description = "支持按订单ID、订单编号、SPU ID、SKU ID等条件筛选")
    @GetMapping("/page")
    public Result<Page<OmsOrderItem>> page(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "订单编号") @RequestParam(required = false) String orderSn,
            @Parameter(description = "SPU ID") @RequestParam(required = false) Long spuId,
            @Parameter(description = "SKU ID") @RequestParam(required = false) Long skuId) {

        Page<OmsOrderItem> page = new Page<>(current, size);
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(orderId != null, OmsOrderItem::getOrderId, orderId)
                .eq(orderSn != null, OmsOrderItem::getOrderSn, orderSn)
                .eq(spuId != null, OmsOrderItem::getSpuId, spuId)
                .eq(skuId != null, OmsOrderItem::getSkuId, skuId)
                .orderByDesc(OmsOrderItem::getId);

        omsOrderItemService.page(page, wrapper);
        return Result.success(page);
    }

    /**
     * 根据ID查询订单明细
     *
     * @param id 订单明细ID
     * @return 订单明细
     */
    @Operation(summary = "根据ID查询订单明细", description = "通过订单明细主键ID查询订单明细信息")
    @GetMapping("/{id}")
    public Result<OmsOrderItem> getById(@PathVariable Long id) {
        OmsOrderItem item = omsOrderItemService.getById(id);
        if (item == null) {
            return Result.error("订单明细不存在");
        }
        return Result.success(item);
    }

    /**
     * 根据订单ID查询所有明细
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @Operation(summary = "根据订单ID查询所有明细", description = "通过订单主键ID查询该订单下的所有订单明细")
    @GetMapping("/list/order/{orderId}")
    public Result<List<OmsOrderItem>> listByOrderId(@PathVariable Long orderId) {
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderItem::getOrderId, orderId);
        List<OmsOrderItem> list = omsOrderItemService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 根据订单编号查询所有明细
     *
     * @param orderSn 订单编号
     * @return 订单明细列表
     */
    @Operation(summary = "根据订单编号查询所有明细", description = "通过订单编号查询该订单下的所有订单明细")
    @GetMapping("/list/sn/{orderSn}")
    public Result<List<OmsOrderItem>> listByOrderSn(@PathVariable String orderSn) {
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderItem::getOrderSn, orderSn);
        List<OmsOrderItem> list = omsOrderItemService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 根据SPU ID查询订单明细（查看商品销售情况）
     *
     * @param spuId SPU ID
     * @return 订单明细列表
     */
    @Operation(summary = "根据SPU ID查询订单明细", description = "通过SPU ID查询包含该商品的所有订单明细")
    @GetMapping("/list/spu/{spuId}")
    public Result<List<OmsOrderItem>> listBySpuId(@PathVariable Long spuId) {
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderItem::getSpuId, spuId);
        List<OmsOrderItem> list = omsOrderItemService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 根据SKU ID查询订单明细（查看规格销售情况）
     *
     * @param skuId SKU ID
     * @return 订单明细列表
     */
    @Operation(summary = "根据SKU ID查询订单明细", description = "通过SKU ID查询包含该商品规格的所有订单明细")
    @GetMapping("/list/sku/{skuId}")
    public Result<List<OmsOrderItem>> listBySkuId(@PathVariable Long skuId) {
        LambdaQueryWrapper<OmsOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrderItem::getSkuId, skuId);
        List<OmsOrderItem> list = omsOrderItemService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 删除订单明细
     *
     * @param id 订单明细ID
     * @return 操作结果
     */
    @Operation(summary = "删除订单明细", description = "通过订单明细主键ID删除订单明细")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = omsOrderItemService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除订单明细
     *
     * @param ids 订单明细ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除订单明细", description = "通过订单明细主键ID列表批量删除订单明细")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        boolean success = omsOrderItemService.removeByIds(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }
}
