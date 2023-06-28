package com.github.bluecatlee.dcep.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 单笔退款交易响应
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 14:53
 */
@Data
public class RefundResponse extends DataInfo {

    /**
     * 订单号
     */
    @JsonProperty("ORDER_NUM")
    private String ORDER_NUM;

    /**
     * 退款金额
     */
    @JsonProperty("AMOUNT")
    private String amount;

//    /**
//     * 支付金额
//     */
//    @JsonProperty("PAY_AMOUNT")
//    private String payAmount;
//
//    /**
//     * 银行流水号
//     */
//    @JsonProperty("OriOvrlsttnEV_Trck_No")
//    private String oriOvrlsttnEVTrckNo;

}
