package com.github.bluecatlee.dcep.constant;

/**
 * 交易类型枚举
 *
 * @Author Bluecatlee
 * @Date 2021/10/14 17:00
 */
public enum  DCEPTxTypeEnum {

    PAY("02", "支付"),
    REFUND("04", "退款"),
    CANCEL("14", "撤销"),
    REVERSE("15", "冲正"),

    ;

    private final String type;
    private final String name;

    DCEPTxTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
