package org.example.smartmallbackend.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 *
 * @author smart-mall-backend
 */
@Getter
public enum PayType {
    /**
     * 微信支付
     */
    WECHAT_PAY(1, "微信支付"),

    /**
     * 支付宝
     */
    ALIPAY(2, "支付宝"),

    /**
     * 银联支付
     */
    UNIONPAY(3, "银联支付"),

    /**
     * 余额支付
     */
    BALANCE(4, "余额支付");

    private final Integer code;
    private final String desc;

    PayType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayType getByCode(Integer code) {
        for (PayType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
