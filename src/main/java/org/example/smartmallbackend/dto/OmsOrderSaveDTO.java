package org.example.smartmallbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "订单新增DTO")
@Data
public class OmsOrderSaveDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单总金额不能为空")
    @DecimalMin(value = "0.01", message = "订单总金额必须大于0")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @Schema(description = "实付金额", example = "89.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实付金额不能为空")
    @DecimalMin(value = "0.01", message = "实付金额必须大于0")
    private BigDecimal payAmount;

    /**
     * 收货人姓名
     */
    @Schema(description = "收货人姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名长度不能超过50个字符")
    private String receiverName;

    /**
     * 收货人电话
     */
    @Schema(description = "收货人电话", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货人电话不能为空")
    @Size(max = 20, message = "收货人电话长度不能超过20个字符")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @Schema(description = "收货地址", example = "北京市朝阳区xx路xx号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址长度不能超过200个字符")
    private String receiverAddress;

    /**
     * 订单明细列表
     */
    @Schema(description = "订单明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDTO> orderItems;

    @Schema(description = "购物车项ID列表（如果是从购物车下单，请传入）")
    private List<Long> cartItemIds;

    /**
     * 订单明细DTO
     */
    @Schema(description = "订单明细DTO")
    @Data
    public static class OrderItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * SPU ID
         */
        @Schema(description = "SPU ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "SPU ID不能为空")
        private Long spuId;

        /**
         * SKU ID
         */
        @Schema(description = "SKU ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /**
         * SPU名称（快照）
         */
        @Schema(description = "SPU名称（快照）", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SPU名称不能为空")
        private String spuName;

        /**
         * SKU图片（快照）
         */
        @Schema(description = "SKU图片（快照）", example = "https://example.com/image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SKU图片不能为空")
        private String skuPic;

        /**
         * SKU价格（快照）
         */
        @Schema(description = "SKU价格（快照）", example = "7999.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "SKU价格不能为空")
        private BigDecimal skuPrice;

        /**
         * 购买数量
         */
        @Schema(description = "购买数量", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "购买数量不能为空")
        private Integer quantity;

        /**
         * SKU规格属性（JSON格式）
         */
        @Schema(description = "SKU规格属性（JSON格式）", example = "{\"color\":\"黑色\",\"storage\":\"256GB\"}")
        private String skuAttrs;
    }
}