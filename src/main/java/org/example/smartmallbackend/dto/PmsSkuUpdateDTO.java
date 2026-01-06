package org.example.smartmallbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU更新DTO
 *
 * @author smart-mall-backend
 */
@Data
public class PmsSkuUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * SKU编码
     */
    @Size(max = 100, message = "SKU编码长度不能超过100个字符")
    private String skuCode;

    /**
     * SKU名称
     */
    @Size(max = 200, message = "SKU名称长度不能超过200个字符")
    private String name;

    /**
     * SKU价格
     */
    @DecimalMin(value = "0.01", message = "SKU价格必须大于0")
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 规格数据（JSON格式）
     */
    private String specData;

    /**
     * 商品图片URL
     */
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    private String picUrl;
}