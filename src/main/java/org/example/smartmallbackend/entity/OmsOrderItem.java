package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细实体类
 * 存储订单商品快照信息
 *
 * @TableName oms_order_item
 */
@TableName(value = "oms_order_item")
@Data
public class OmsOrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SPU名称（快照）
     */
    private String spuName;

    /**
     * SKU图片（快照）
     */
    private String skuPic;

    /**
     * SKU价格（快照）
     */
    private BigDecimal skuPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * SKU规格属性（JSON格式）
     */
    private String skuAttrs;
}