package com.github.bluecatlee.dcep.vo.authorize;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 授权结果
 *
 * @Author Bluecatlee
 * @Date 2021/10/11 9:50
 */
@Data
public class AuthorizeResult {

    /**
     * 是否授权成功
     */
    @JsonProperty("SUCCEED")
    private boolean succeed;

    /*以下授权成功时返回*/
    /**
     * ACCESS_TOKEN
     */
    @JsonProperty("ACCESS_TOKEN")
    private String accessToken;

    /**
     * 加密后的通讯秘钥 需要再次解密
     */
    @JsonProperty("COMMUNICATION_PWD")
    private String communicationPwd;

    /**
     * 解密后的通讯秘钥
     */
    private String communicationKey;

    /**
     * 访问地址
     */
    @JsonProperty("ACC_ENTRY")
    private String accEntry;

    /*以下授权失败时返回*/
    /**
     * 错误码
     */
    @JsonProperty("ERROR_CODE")
    private String errorCode;

    /**
     * 错误信息
     */
    @JsonProperty("ERROR_MSG")
    private String errorMsg;

}
