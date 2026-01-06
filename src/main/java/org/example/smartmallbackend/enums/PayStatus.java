package org.example.smartmallbackend.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author smart-mall-backend
 */
@Getter
public enum PayStatus {
    /**
     * 未支付
     */
    UNPAID(0, "未支付"),

    /**
     * 支付中
     */
    PAYING(1, "支付中"),

    /**
     * 已支付
     */
    PAID(2, "已支付"),

    /**
     * 支付失败
     */
    PAY_FAILED(3, "支付失败"),

    /**
     * 已退款
     */
    REFUNDED(4, "已退款");

    private final Integer code;
    private final String desc;

    PayStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayStatus getByCode(Integer code) {
        for (PayStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
