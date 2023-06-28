package com.github.bluecatlee.dcep.vo.request;

import com.github.bluecatlee.dcep.annotation.DCEPField;
import lombok.Data;

/**
 * 商户流水查询交易请求
 *
 * @Author Bluecatlee
 * @Date 2021/10/11 11:11
 */
@Data
public class QueryRequest extends DCEPBaseRequest {

    /**
     * 交易功能号
     *      01-根据商户号查询
     *      02-根据商户号+终端号查询
     *      05-根据商户号+订单号查询
     *      08-根据商户号+退款流水号
     */
    @DCEPField(required = true, name = "Txn_Fcn_No", order = 13)
    private String txnFcnNo;

    /**
     * POS终端编号
     */
    @DCEPField(name = "POS_ID", orderNonNull = true, order = 14)
    private String posId;

    /**
     * 开始日期 YYYYMMDD
     */
    @DCEPField(required = true, name = "Enqr_StDt", order = 15)
    private String startDate;

    /**
     * 截止日期 YYYYMMDD
     */
    @DCEPField(required = true, name = "Enqr_CODt", order = 16)
    private String endDate;

    /**
     * 退款流水号
     */
    @DCEPField(name = "ExoStm_Py_Rmrk", orderNonNull = true, order = 17)
    private String refundNo;

    /**
     * 订单号
     */
    @DCEPField(name = "ORDER", orderNonNull = true, order = 18)
    private String orderNo;

    /**
     * 当前页次
     */
    @DCEPField(required = true, order = 19)
    private String page = "1";

    @Override
    public String txCode() {
        return "5WX003";
    }
}
