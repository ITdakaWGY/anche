package com.github.bluecatlee.dcep.constant;

/**
 * 返回码枚举
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 11:12
 */
public enum ReturnCodeEnum {

    SUCCESS("000000", "成功"),

    // ...

    // 需要特殊处理：
    UNKNOWN_PBOC("XNAA12110016", "人行返回状态不确定"),
    TIMEOUT("0130Z0100001", "通讯超时"),

    ;

    private String code;
    private String msg;

    ReturnCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static String getMessageByCode(String code) {
        for(ReturnCodeEnum returnCodeEnum : ReturnCodeEnum.values()){
            if(returnCodeEnum.code.equals(code)){
                return returnCodeEnum.msg;
            }
        }
        return null;
    }

}
