package org.example.smartmallbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SPU新增DTO
 *
 * @author smart-mall-backend
 */
@Schema(description = "商品SPU新增DTO")
@Data
public class PmsSpuSaveDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    private String name;

    /**
     * 商品副标题
     */
    @Size(max = 200, message = "商品副标题长度不能超过200个字符")
    private String subTitle;

    /**
     * 品牌名称
     */
    @Size(max = 100, message = "品牌名称长度不能超过100个字符")
    private String brandName;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 商品描述
     */
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    /**
     * 商品价格
     */
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 发布状态：0-未发布，1-已发布
     */
    private Integer publishStatus;
}