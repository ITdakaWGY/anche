package com.github.bluecatlee.dcep.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bluecatlee.redundance.MessagePack;
import lombok.Data;

/**
 * Transaction_Header 商户平台交易响应通用参数
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 9:23
 */
@Data
public class DCEPBaseResponse<T extends DataInfo> extends MessagePack {

    /**
     * 接口订单号    同请求报文
     */
    @JsonProperty("FT_ORDERNO")
    private String ftOrderNo;

    /**
     * 建行流水号
     */
    @JsonProperty("SYS_EVT_TRACE_ID")
    private String sysEvtTraceId;

    /**
     * 商户号
     */
    @JsonProperty("CUSTOMERID")
    private String customerId;

    /**
     * 交易码
     */
    @JsonProperty("TXCODE")
    private String txCode;

    /**
     * 响应码  000000表示成功 其他表示异常
     */
    @JsonProperty("RETURN_CODE")
    private String returnCode;

    /**
     * 响应信息
     */
    @JsonProperty("RETURN_MSG")
    private String returnMsg;

    /**
     * 语言
     */
    @JsonProperty("LANGUAGE")
    private String language;

    /**
     * 响应报文的Transaction_body部分
     */
    private T dataInfo;

}
