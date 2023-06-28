package com.github.bluecatlee.dcep.constant;

/**
 * 交易状态枚举
 *
 * @Author Bluecatlee
 * @Date 2021/10/14 14:35
 */
public enum DCEPTxStatusEnum {

    SUCCESS("00", "成功"),
    FAIL("01", "失败"),
    UNKNOWN("03", "未知"),

    ;

    private final String status;
    private final String message;

    DCEPTxStatusEnum(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
