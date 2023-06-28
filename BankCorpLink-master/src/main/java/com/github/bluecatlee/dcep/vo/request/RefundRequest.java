package com.github.bluecatlee.dcep.vo.request;

import com.github.bluecatlee.dcep.annotation.DCEPField;
import lombok.Data;

/**
 * 单笔退款交易请求 支持部分退款
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 14:49
 */
@Data
public class RefundRequest extends DCEPBaseRequest {

    /**
     * 退款金额
     */
    @DCEPField(required = true, order = 13)
    private String money;

    /**
     * 开始日期 YYYYMMDD
     */
    @DCEPField(required = true, name = "Enqr_StDt", order = 14)
    private String startDate;

    /**
     * 截止日期 YYYYMMDD
     */
    @DCEPField(required = true, name = "Enqr_CODt", order = 15)
    private String endDate;

    /**
     * 退款流水号
     */
    @DCEPField(name = "MsgRp_Jrnl_No", orderNonNull = true, order = 16)
    private String refundNo;

    /**
     * 订单号
     */
    @DCEPField(required = true, name = "ORDER", orderNonNull = true, order = 17)
    private String orderNo;

    @Override
    public String txCode() {
        return "5WX004";
    }
}
