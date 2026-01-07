package org.example.smartmallbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.dto.PmsSkuSaveDTO;
import org.example.smartmallbackend.dto.PmsSkuUpdateDTO;
import org.example.smartmallbackend.entity.PmsSku;
import org.example.smartmallbackend.service.PmsSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品SKU管理Controller
 *
 * @author smart-mall-backend
 * @description 商品SKU的增删改查接口，包含库存管理
 */
@Validated
@Tag(name = "商品SKU管理", description = "商品SKU的增删改查接口，包含库存管理")
@RestController
@RequestMapping("/api/pms/sku")
public class PmsSkuController {

    @Autowired
    private PmsSkuService pmsSkuService;

    /**
     * 分页查询商品SKU列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @param spuId  SPU ID
     * @param name   SKU名称（模糊查询）
     * @param skuCode SKU编码
     * @return 分页结果
     */
    @Operation(summary = "分页查询商品SKU列表", description = "支持按SPU ID、SKU名称、SKU编码等条件筛选")
    @GetMapping("/page")
    public Result<Page<PmsSku>> page(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "SPU ID") @RequestParam(required = false) Long spuId,
            @Parameter(description = "SKU名称（模糊查询）") @RequestParam(required = false) String name,
            @Parameter(description = "SKU编码") @RequestParam(required = false) String skuCode) {

        Page<PmsSku> page = new Page<>(current, size);
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(spuId != null, PmsSku::getSpuId, spuId)
                .like(name != null, PmsSku::getName, name)
                .eq(skuCode != null, PmsSku::getSkuCode, skuCode)
                .orderByDesc(PmsSku::getId);

        pmsSkuService.page(page, wrapper);
        return Result.success(page);
    }

    /**
     * 根据SPU ID查询其下的所有SKU
     *
     * @param spuId SPU ID
     * @return SKU列表
     */
    @Operation(summary = "根据SPU ID查询SKU列表", description = "查询指定SPU下的所有SKU")
    @GetMapping("/list/by-spu/{spuId}")
    public Result<List<PmsSku>> listBySpuId(@Parameter(description = "SPU ID", required = true) @PathVariable Long spuId) {
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsSku::getSpuId, spuId);
        List<PmsSku> list = pmsSkuService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 根据ID查询商品SKU详情
     *
     * @param id SKU ID
     * @return SKU详情
     */
    @Operation(summary = "根据ID查询商品SKU详情", description = "通过SKU主键ID查询商品SKU完整信息")
    @GetMapping("/{id}")
    public Result<PmsSku> getById(@PathVariable Long id) {
        PmsSku sku = pmsSkuService.getById(id);
        if (sku == null) {
            return Result.error("商品SKU不存在");
        }
        return Result.success(sku);
    }

    /**
     * 新增商品SKU
     *
     * @param dto 商品SKU信息
     * @return 操作结果
     */
    @Operation(summary = "新增商品SKU", description = "添加新的商品SKU信息")
    @PostMapping
    public Result<?> save(@RequestBody @Validated PmsSkuSaveDTO dto) {
        // 校验SKU编码唯一性
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsSku::getSkuCode, dto.getSkuCode());
        long count = pmsSkuService.count(wrapper);
        if (count > 0) {
            return Result.error("SKU编码已存在");
        }

        PmsSku sku = BeanUtil.copyProperties(dto, PmsSku.class);
        boolean success = pmsSkuService.save(sku);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新商品SKU
     *
     * @param dto 商品SKU信息
     * @return 操作结果
     */
    @Operation(summary = "更新商品SKU", description = "修改商品SKU信息")
    @PutMapping
    public Result<?> update(@RequestBody @Validated PmsSkuUpdateDTO dto) {
        // 校验SKU编码唯一性（排除自身）
        if (dto.getSkuCode() != null) {
            LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PmsSku::getSkuCode, dto.getSkuCode())
                    .ne(PmsSku::getId, dto.getId());
            long count = pmsSkuService.count(wrapper);
            if (count > 0) {
                return Result.error("SKU编码已存在");
            }
        }

        PmsSku sku = BeanUtil.copyProperties(dto, PmsSku.class);
        boolean success = pmsSkuService.updateById(sku);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除商品SKU
     *
     * @param id SKU ID
     * @return 操作结果
     */
    @Operation(summary = "删除商品SKU", description = "根据ID删除商品SKU")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = pmsSkuService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除商品SKU
     *
     * @param ids SKU ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除商品SKU", description = "根据ID列表批量删除商品SKU")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        boolean success = pmsSkuService.removeByIds(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 增加库存
     *
     * @param id       SKU ID
     * @param quantity 增加数量
     * @return 操作结果
     */
    @Operation(summary = "增加库存", description = "为指定SKU增加库存数量")
    @PutMapping("/stock/add/{id}")
    public Result<?> addStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.error("增加数量必须大于0");
        }

        PmsSku sku = pmsSkuService.getById(id);
        if (sku == null) {
            return Result.error("商品SKU不存在");
        }

        sku.setStock(sku.getStock() + quantity);
        boolean success = pmsSkuService.updateById(sku);
        return success ? Result.success("库存增加成功") : Result.error("操作失败");
    }

    /**
     * 减少库存
     *
     * @param id       SKU ID
     * @param quantity 减少数量
     * @return 操作结果
     */
    @Operation(summary = "减少库存", description = "为指定SKU减少库存数量")
    @PutMapping("/stock/reduce/{id}")
    public Result<?> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.error("减少数量必须大于0");
        }

        PmsSku sku = pmsSkuService.getById(id);
        if (sku == null) {
            return Result.error("商品SKU不存在");
        }

        if (sku.getStock() < quantity) {
            return Result.error("库存不足");
        }

        sku.setStock(sku.getStock() - quantity);
        boolean success = pmsSkuService.updateById(sku);
        return success ? Result.success("库存减少成功") : Result.error("操作失败");
    }

    /**
     * 设置库存
     *
     * @param id       SKU ID
     * @param quantity 库存数量
     * @return 操作结果
     */
    @Operation(summary = "设置库存", description = "为指定SKU设置库存数量")
    @PutMapping("/stock/set/{id}")
    public Result<?> setStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity < 0) {
            return Result.error("库存数量不能为负数");
        }

        PmsSku sku = new PmsSku();
        sku.setId(id);
        sku.setStock(quantity);
        boolean success = pmsSkuService.updateById(sku);
        return success ? Result.success("库存设置成功") : Result.error("操作失败");
    }

    /**
     * 根据SKU编码查询
     *
     * @param skuCode SKU编码
     * @return SKU详情
     */
    @Operation(summary = "根据SKU编码查询", description = "通过SKU编码查询商品SKU详情")
    @GetMapping("/code/{skuCode}")
    public Result<PmsSku> getBySkuCode(@PathVariable String skuCode) {
        LambdaQueryWrapper<PmsSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsSku::getSkuCode, skuCode);
        PmsSku sku = pmsSkuService.getOne(wrapper);
        if (sku == null) {
            return Result.error("商品SKU不存在");
        }
        return Result.success(sku);
    }
}