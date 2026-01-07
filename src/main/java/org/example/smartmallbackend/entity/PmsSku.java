package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.smartmallbackend.handler.PostgresJsonbTypeHandler;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU实体类
 * SKU（Stock Keeping Unit）库存量单位，商品库存和规格信息
 *
 * @TableName pms_sku
 */
@Schema(description = "商品SKU实体")
@TableName(value = "pms_sku",autoResultMap = true)
@Data
public class PmsSku implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    @TableId
    private Long id;

    /**
     * SPU ID
     */
    @Schema(description = "SPU ID", example = "1")
    private Long spuId;

    /**
     * SKU编码
     */
    @Schema(description = "SKU编码", example = "IP15PRO-256-BLK")
    private String skuCode;

    /**
     * SKU名称
     */
    @Schema(description = "SKU名称", example = "iPhone 15 Pro 256GB 黑色")
    private String name;

    /**
     * SKU价格
     */
    @Schema(description = "SKU价格", example = "7999.00")
    private BigDecimal price;

    /**
     * 库存数量
     */
    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    /**
     * 规格数据（JSON格式）
     */
    @Schema(description = "规格数据（JSON格式）", example = "{\"color\":\"黑色\",\"storage\":\"256GB\"}")
    @TableField(typeHandler = PostgresJsonbTypeHandler.class)
    private String specData;

    /**
     * 商品图片URL
     */
    @Schema(description = "商品图片URL", example = "https://example.com/image.jpg")
    private String picUrl;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @Schema(description = "逻辑删除：0-未删除，1-已删除", example = "0")
    @TableLogic
    private Integer isDeleted;
}