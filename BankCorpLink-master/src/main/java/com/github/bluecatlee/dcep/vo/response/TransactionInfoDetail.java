package com.github.bluecatlee.dcep.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 交易信息明细
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 13:40
 */
@Data
public class TransactionInfoDetail {

    /**
     * 交易日期
     */
    @JsonProperty("Txn_Dt")
    private String txnDt;

    /**
     * 交易时间
     */
    @JsonProperty("Txn_Tm")
    private String txnTm;

    /**
     * 订单号
     */
    @JsonProperty("OnLn_Py_Txn_Ordr_ID")
    private String onLnPyTxnOrdrID;

    /**
     * 对方账号
     */
    @JsonProperty("CntprtAcc")
    private String cntprtAcc;

    /**
     * 交易金额
     */
    @JsonProperty("TxnAmt")
    private String txnAmt;

    /**
     * 实际扣款金额
     */
    @JsonProperty("Act_ADbAmt")
    private String actADbAmt;

    /**
     * 手续费金额
     */
    @JsonProperty("HdCg_Amt")
    private String hdCgAmt;

    /**
     * 柜台号
     */
    @JsonProperty("POS_ID")
    private String posId;

    /**
     * 付款凭证号
     */
    @JsonProperty("Pym_Vchr_No")
    private String pymVchrNo;

    /**
     * 银行流水号
     */
    @JsonProperty("Orig_TxnSrlNo")
    private String origTxnSrlNo;

    /**
     * 交易类型
     *      02－支付
     *      04－退款
     *      14—撤销
     *      15-冲正
     */
    @JsonProperty("TRD_TP_ECD")
    private String trdTpEcd;

    /**
     * 交易状态
     *      00-成功
     *      01-失败
     *      02-不确定
     */
    @JsonProperty("SYS_TX_STATUS")
    private String sysTxStatus;

    /**
     * 商户退款金额
     *      为空或0表示无退款
     */
    @JsonProperty("Mrch_Rfnd_Amt")
    private String mrchRfndAmt;

    /**
     * 交易备注
     */
    @JsonProperty("Txn_Rmrk")
    private String txnRmrk;

}
