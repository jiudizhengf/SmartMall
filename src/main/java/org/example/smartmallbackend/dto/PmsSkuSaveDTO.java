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
 * 商品SKU新增DTO
 *
 * @author smart-mall-backend
 */
@Schema(description = "商品SKU库存新增DTO")
@Data
public class PmsSkuSaveDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * SPU ID
     */
    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    /**
     * SKU编码
     */
    @NotBlank(message = "SKU编码不能为空")
    @Size(max = 100, message = "SKU编码长度不能超过100个字符")
    private String skuCode;

    /**
     * SKU名称
     */
    @NotBlank(message = "SKU名称不能为空")
    @Size(max = 200, message = "SKU名称长度不能超过200个字符")
    private String name;

    /**
     * SKU价格
     */
    @NotNull(message = "SKU价格不能为空")
    @DecimalMin(value = "0.01", message = "SKU价格必须大于0")
    private BigDecimal price;

    /**
     * 库存数量
     */
    @NotNull(message = "库存数量不能为空")
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