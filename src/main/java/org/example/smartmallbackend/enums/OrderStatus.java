package org.example.smartmallbackend.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author smart-mall-backend
 */
@Getter
public enum OrderStatus {
    /**
     * 待付款
     */
    PENDING_PAYMENT(0, "待付款"),

    /**
     * 待发货
     */
    PENDING_DELIVERY(1, "待发货"),

    /**
     * 已发货
     */
    DELIVERED(2, "已发货"),

    /**
     * 已完成
     */
    COMPLETED(3, "已完成"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消"),

    /**
     * 售后中
     */
    AFTER_SALE(5, "售后中"),

    /**
     * 退款中
     */
    REFUNDING(6, "退款中"),

    /**
     * 已退款
     */
    REFUNDED(7, "已退款");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus getByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING_PAYMENT;
    }

    /**
     * 判断是否可以支付
     */
    public boolean canPay() {
        return this == PENDING_PAYMENT;
    }

    /**
     * 判断是否可以发货
     */
    public boolean canDeliver() {
        return this == PENDING_DELIVERY;
    }

    /**
     * 判断是否可以完成
     */
    public boolean canComplete() {
        return this == DELIVERED;
    }

    /**
     * 判断是否可以申请售后
     */
    public boolean canAfterSale() {
        return this == DELIVERED || this == COMPLETED;
    }

    /**
     * 判断是否可以退款
     */
    public boolean canRefund() {
        return this == PENDING_DELIVERY || this == DELIVERED;
    }
}
