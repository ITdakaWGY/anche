package com.github.bluecatlee.redundance.bean;

import com.github.bluecatlee.redundance.BaseRequest;
import lombok.Data;

/**
 * 建设银行请求bean
 *
 * @Date 2021/2/23 15:16
 */
@Data
public class CCBPayRequest extends BaseRequest {

    private String tradeName;  // 商品名称

}
