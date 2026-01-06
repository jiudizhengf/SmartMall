package org.example.smartmallbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单新增DTO
 *
 * @author smart-mall-backend
 */
@Data
public class OmsOrderSaveDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 订单总金额
     */
    @NotNull(message = "订单总金额不能为空")
    @DecimalMin(value = "0.01", message = "订单总金额必须大于0")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @NotNull(message = "实付金额不能为空")
    @DecimalMin(value = "0.01", message = "实付金额必须大于0")
    private BigDecimal payAmount;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名长度不能超过50个字符")
    private String receiverName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    @Size(max = 20, message = "收货人电话长度不能超过20个字符")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址长度不能超过200个字符")
    private String receiverAddress;

    /**
     * 订单明细列表
     */
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDTO> orderItems;

    /**
     * 订单明细DTO
     */
    @Data
    public static class OrderItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * SPU ID
         */
        @NotNull(message = "SPU ID不能为空")
        private Long spuId;

        /**
         * SKU ID
         */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /**
         * SPU名称（快照）
         */
        @NotBlank(message = "SPU名称不能为空")
        private String spuName;

        /**
         * SKU图片（快照）
         */
        @NotBlank(message = "SKU图片不能为空")
        private String skuPic;

        /**
         * SKU价格（快照）
         */
        @NotNull(message = "SKU价格不能为空")
        private BigDecimal skuPrice;

        /**
         * 购买数量
         */
        @NotNull(message = "购买数量不能为空")
        private Integer quantity;

        /**
         * SKU规格属性（JSON格式）
         */
        private String skuAttrs;
    }
}