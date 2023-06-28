package com.github.bluecatlee.dcep.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 商户多金融通道寻址响应
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 9:27
 */
@Data
public class AddressingResponse extends DataInfo {

    /**
     * 多通道服务地址
     */
    @JsonProperty("REQ_URL")
    private String reqUrl;

}
