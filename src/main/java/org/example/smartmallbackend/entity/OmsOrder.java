package org.example.smartmallbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @TableName oms_order
 */
@Schema(description = "订单实体")
@TableName(value = "oms_order")
@Data
public class OmsOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    @TableId
    private Long id;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "202301010001")
    private String orderSn;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额", example = "99.99")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @Schema(description = "实付金额", example = "89.99")
    private BigDecimal payAmount;


    /**
     * 订单状态：0-待付款，1-待发货，2-已发货，3-已完成，4-已取消，5-售后中，6-退款中，7-已退款
     */
    @Schema(description = "订单状态：0-待付款，1-待发货，2-已发货，3-已完成，4-已取消，5-售后中，6-退款中，7-已退款", example = "0")
    private Integer status;

    /**
     * 支付状态：0-未支付，1-支付中，2-已支付，3-支付失败，4-已退款
     */
    @Schema(description = "支付状态：0-未支付，1-支付中，2-已支付，3-支付失败，4-已退款", example = "0")
    private Integer payStatus;

    /**
     * 支付方式：1-微信支付，2-支付宝，3-银联，4-余额支付
     */
    @Schema(description = "支付方式：1-微信支付，2-支付宝，3-银联，4-余额支付", example = "1")
    private Integer payType;

    /**
     * 第三方支付交易号
     */
    @Schema(description = "第三方支付交易号", example = "WX20230101001")
    private String paymentTransactionNo;

    /**
     * 收货人姓名
     */
    @Schema(description = "收货人姓名", example = "张三")
    private String receiverName;

    /**
     * 收货人电话
     */
    @Schema(description = "收货人电话", example = "13800138000")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @Schema(description = "收货地址", example = "北京市朝阳区xx路xx号")
    private String receiverAddress;

    /**
     * 支付时间
     */
    @Schema(description = "支付时间", example = "2023-01-01T12:00:00")
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    @Schema(description = "发货时间", example = "2023-01-02T10:00:00")
    private LocalDateTime deliveryTime;

    /**
     * 收货时间
     */
    @Schema(description = "收货时间", example = "2023-01-05T15:00:00")
    private LocalDateTime receiveTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2023-01-05T15:30:00")
    private LocalDateTime finishTime;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-01-01T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-01-01T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @Schema(description = "逻辑删除：0-未删除，1-已删除", example = "0")
    @TableLogic
    private Integer isDeleted;
}