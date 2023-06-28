package com.github.bluecatlee.dcep.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bluecatlee.dcep.vo.authorize.AuthorizeResult;
import lombok.Data;

/**
 * 授权+寻址信息
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 13:13
 */
@Data
public class AuthorizeAndAddressingResult extends AuthorizeResult {

    /**
     * 多通道服务地址
     */
    @JsonProperty("REQ_URL")
    private String reqUrl;

}
