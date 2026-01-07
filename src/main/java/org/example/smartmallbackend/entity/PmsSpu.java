package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SPU实体类
 * SPU（Standard Product Unit）标准产品单位，商品通用信息
 *
 * @TableName pms_spu
 */
@Schema(description = "商品SPU实体")
@TableName(value = "pms_spu")
@Data
public class PmsSpu implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String name;

    /**
     * 商品副标题
     */
    @Schema(description = "商品副标题", example = "全新设计，强大性能")
    private String subTitle;

    /**
     * 品牌名称
     */
    @Schema(description = "品牌名称", example = "Apple")
    private String brandName;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", example = "商品详细介绍")
    private String description;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格", example = "7999.00")
    private BigDecimal price;

    /**
     * 发布状态：0-未发布，1-已发布
     */
    @Schema(description = "发布状态：0-未发布，1-已发布", example = "0")
    private Integer publishStatus;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-01-01T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-01-01T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @Schema(description = "逻辑删除：0-未删除，1-已删除", example = "0")
    @TableLogic
    private Integer isDeleted;
}