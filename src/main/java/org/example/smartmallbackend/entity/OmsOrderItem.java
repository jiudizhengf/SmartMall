package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.smartmallbackend.handler.PostgresJsonbTypeHandler;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细实体类
 * 存储订单商品快照信息
 *
 * @TableName oms_order_item
 */
@Schema(description = "订单明细实体")
@Data
public class OmsOrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    @TableId
    private Long id;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "202301010001")
    private String orderSn;

    /**
     * SPU ID
     */
    @Schema(description = "SPU ID", example = "1")
    private Long spuId;

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID", example = "1")
    private Long skuId;

    /**
     * SPU名称（快照）
     */
    @Schema(description = "SPU名称（快照）", example = "iPhone 15 Pro")
    private String spuName;

    /**
     * SKU图片（快照）
     */
    @Schema(description = "SKU图片（快照）", example = "https://example.com/image.jpg")
    private String skuPic;

    /**
     * SKU价格（快照）
     */
    @Schema(description = "SKU价格（快照）", example = "7999.00")
    private BigDecimal skuPrice;

    /**
     * 购买数量
     */
    @Schema(description = "购买数量", example = "1")
    private Integer quantity;

    /**
     * SKU规格属性（JSON格式）
     */
    @Schema(description = "SKU规格属性（JSON格式）", example = "{\"color\":\"黑色\",\"storage\":\"256GB\"}")
    private String skuAttrs;
}