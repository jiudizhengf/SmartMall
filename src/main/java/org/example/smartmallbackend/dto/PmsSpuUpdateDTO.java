package org.example.smartmallbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SPU更新DTO
 *
 * @author smart-mall-backend
 */
@Data
public class PmsSpuUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long id;

    /**
     * 商品名称
     */
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
    private Long categoryId;

    /**
     * 商品描述
     */
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    /**
     * 商品价格
     */
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 发布状态：0-未发布，1-已发布
     */
    private Integer publishStatus;
}