package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.smartmallbackend.handler.PostgresJsonbTypeHandler;

import java.io.Serializable;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "oms_cart_item",autoResultMap = true)
@Schema(description = "购物车商品")
@Data
public class OmsCartItem extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "商品图片")
    private String skuPic;

    @Schema(description = "商品单价")
    private BigDecimal skuPrice;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "销售属性（JSON格式）")
    @TableField(typeHandler = PostgresJsonbTypeHandler.class) // 使用之前的TypeHandler处理JSON
    private String skuAttrs;
}
