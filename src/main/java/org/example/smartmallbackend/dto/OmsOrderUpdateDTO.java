package org.example.smartmallbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单更新DTO
 *
 * @author smart-mall-backend
 */
@Data
public class OmsOrderUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long id;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态：0-待付款，1-待发货，2-已发货，3-已完成，4-已取消
     */
    private Integer status;

    /**
     * 收货人姓名
     */
    @Size(max = 50, message = "收货人姓名长度不能超过50个字符")
    private String receiverName;

    /**
     * 收货人电话
     */
    @Size(max = 20, message = "收货人电话长度不能超过20个字符")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @Size(max = 200, message = "收货地址长度不能超过200个字符")
    private String receiverAddress;
}