package com.github.bluecatlee.dcep.constant;

/**
 *  成功标志
 */
public enum SuccessFlagEnum {

    SUCCESS("Y"),
    FAIL("N"),
    ;

    private final String value;

    SuccessFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
