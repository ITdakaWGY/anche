package com.github.bluecatlee.dcep.vo.request;

import lombok.Data;

/**
 * 商户多金融通道寻址请求
 *
 * @Author Bluecatlee
 * @Date 2021/10/11 16:59
 */
@Data
public class AddressingRequest extends DCEPBaseRequest {

    @Override
    public String txCode() {
        return "5WX001";
    }
}
