package org.example.smartmallbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.dto.PmsSpuSaveDTO;
import org.example.smartmallbackend.dto.PmsSpuUpdateDTO;
import org.example.smartmallbackend.entity.PmsSpu;
import org.example.smartmallbackend.event.ProductOnShelfEvent;
import org.example.smartmallbackend.event.ProductionOffShelfEvent;
import org.example.smartmallbackend.service.PmsSpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品SPU管理Controller
 *
 * @author smart-mall-backend
 * @description 商品SPU的增删改查接口
 */
@Validated
@Tag(name = "商品SPU管理", description = "商品SPU的增删改查接口")
@RestController
@RequestMapping("/api/pms/spu")
public class PmsSpuController {

    @Autowired
    private PmsSpuService pmsSpuService;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 分页查询商品SPU列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @param name    商品名称（模糊查询）
     * @param brandName 品牌名称（模糊查询）
     * @param categoryId 分类ID
     * @param publishStatus 发布状态
     * @return 分页结果
     */
    @Operation(summary = "分页查询商品SPU列表", description = "支持按商品名称、品牌名称、分类ID、发布状态等条件筛选")
    @GetMapping("/page")
    public Result<Page<PmsSpu>> page(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "商品名称（模糊查询）") @RequestParam(required = false) String name,
            @Parameter(description = "品牌名称（模糊查询）") @RequestParam(required = false) String brandName,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "发布状态：0-未发布，1-已发布") @RequestParam(required = false) Integer publishStatus) {

        Page<PmsSpu> page = new Page<>(current, size);
        LambdaQueryWrapper<PmsSpu> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(name != null, PmsSpu::getName, name)
                .like(brandName != null, PmsSpu::getBrandName, brandName)
                .eq(categoryId != null, PmsSpu::getCategoryId, categoryId)
                .eq(publishStatus != null, PmsSpu::getPublishStatus, publishStatus)
                .orderByDesc(PmsSpu::getCreateTime);

        pmsSpuService.page(page, wrapper);
        return Result.success(page);
    }

    /**
     * 根据ID查询商品SPU详情
     *
     * @param id SPU ID
     * @return SPU详情
     */
    @Operation(summary = "根据ID查询商品SPU详情", description = "通过SPU主键ID查询商品完整信息")
    @GetMapping("/{id}")
    public Result<PmsSpu> getById(@Parameter(description = "SPU ID", required = true) @PathVariable Long id) {
        PmsSpu spu = pmsSpuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }
        return Result.success(spu);
    }

    /**
     * 新增商品SPU
     *
     * @param dto 商品SPU信息
     * @return 操作结果
     */
    @Operation(summary = "新增商品SPU", description = "创建新的商品SPU，默认为未发布状态")
    @PostMapping
    public Result<String> save(@RequestBody @Validated PmsSpuSaveDTO dto) {
        PmsSpu spu = BeanUtil.copyProperties(dto, PmsSpu.class);
        // 默认未发布状态
        if (spu.getPublishStatus() == null) {
            spu.setPublishStatus(0);
        }
        boolean success = pmsSpuService.save(spu);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新商品SPU
     *
     * @param dto 商品SPU信息
     * @return 操作结果
     */
    @Operation(summary = "更新商品SPU", description = "更新商品SPU信息")
    @PutMapping
    public Result<String> update(@RequestBody @Validated PmsSpuUpdateDTO dto) {
        PmsSpu spu = BeanUtil.copyProperties(dto, PmsSpu.class);
        boolean success = pmsSpuService.updateById(spu);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除商品SPU
     *
     * @param id SPU ID
     * @return 操作结果
     */
    @Operation(summary = "删除商品SPU", description = "根据ID删除商品SPU")
    @DeleteMapping("/{id}")
    public Result<?> delete(@Parameter(description = "SPU ID", required = true) @PathVariable Long id) {
        boolean success = pmsSpuService.removeById(id);
        if(success){
            applicationContext.publishEvent(new ProductionOffShelfEvent(this, id));
        }
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除商品SPU
     *
     * @param ids SPU ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除商品SPU", description = "根据ID列表批量删除商品SPU")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        boolean success = pmsSpuService.removeByIds(ids);
        if(success){
            for(Long id : ids){
                applicationContext.publishEvent(new ProductionOffShelfEvent(this, id));
            }
        }
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 商品上架
     *
     * @param id SPU ID
     * @return 操作结果
     */
    @Operation(summary = "商品上架", description = "将商品状态设置为已发布")
    @PutMapping("/publish/{id}")
    public Result<?> publish(@Parameter(description = "SPU ID", required = true) @PathVariable Long id) {
        PmsSpu spu = new PmsSpu();
        spu.setId(id);
        spu.setPublishStatus(1);
        boolean success = pmsSpuService.updateById(spu);
        if(success){
            applicationContext.publishEvent(new ProductOnShelfEvent(this, id));
        }
        return success ? Result.success("上架成功") : Result.error("上架失败");
    }

    /**
     * 商品下架
     *
     * @param id SPU ID
     * @return 操作结果
     */
    @Operation(summary = "商品下架", description = "将商品状态设置为未发布")
    @PutMapping("/unpublish/{id}")
    public Result<?> unpublish(@Parameter(description = "SPU ID", required = true) @PathVariable Long id) {
        PmsSpu spu = new PmsSpu();
        spu.setId(id);
        spu.setPublishStatus(0);
        boolean success = pmsSpuService.updateById(spu);
        if(success){
            applicationContext.publishEvent(new ProductionOffShelfEvent(this, id));
        }
        return success ? Result.success("下架成功") : Result.error("下架失败");
    }

    /**
     * 查询所有已发布的商品列表
     *
     * @return 商品列表
     */
    @Operation(summary = "查询已发布商品列表", description = "查询所有已发布的商品，按创建时间倒序")
    @GetMapping("/published")
    public Result<List<PmsSpu>> getPublishedList() {
        LambdaQueryWrapper<PmsSpu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsSpu::getPublishStatus, 1)
                .orderByDesc(PmsSpu::getCreateTime);
        List<PmsSpu> list = pmsSpuService.list(wrapper);
        return Result.success(list);
    }
}