package com.github.bluecatlee.dcep.vo.pay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.bluecatlee.dcep.annotation.DCEPField;
import lombok.Data;

/**
 * H5数字货币支付下单请求
 *
 * @Author Bluecatlee
 * @Date 2021/10/8 17:05
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class H5PayRequest extends DCEPBasePayRequest {

    @DCEPField(required = true, name = "CCB_IBSVersion")
    private String version = "V6";

    /**
     * 加密串
     */
    @DCEPField(required = true)
    private String mac;

    /**
     * 退出支付流程时返回商户URL。
     * 该字段不参与MAC校验
     */
    @DCEPField(name = "Mrch_url")
    private String merchantUrl;

    /**
     * 商户代码
     */
    @DCEPField(required = true, order = 1)
    private String merchantId;

    /**
     * 商户柜台代码
     */
    @DCEPField(required = true, order = 2)
    private String posId;

    /**
     * 分行代码
     */
    @DCEPField(required = true, order = 3)
    private String branchId;

    /**
     * 订单号
     */
    @DCEPField(required = true, order = 5)
    private String orderId;

    /**
     * 付款金额 元
     */
    @DCEPField(required = true, order = 6)
    private String payment;

    /**
     * 币种
     */
    @DCEPField(required = true, order = 8)
    private String curCode = "01";

    /**
     * 交易码
     */
    @DCEPField(required = true, order = 9)
    private String txCode = "HT0000";

    /**
     * 备注1
     */
    @DCEPField(required = true, order = 10)
    private String remark1 = "";

    /**
     * 备注2
     */
    @DCEPField(required = true, order = 11)
    private String remark2 = "";

    /**
     * 返回类型 1-json
     */
    @DCEPField(required = true, order = 12)
    private String returnType = "1";

    /**
     * 订单超时时间 格式YYYYMMDDHHMMSS，如20120214143005
     */
    @DCEPField(required = true, order = 13)
    private String timeout = "";

    /**
     * 商户结算账号 央行app使用
     */
    @DCEPField(name = "CdtrWltId", orderNonNull = true, order = 7)
    private String cdtrWltId;

    /**
     * 二级商户代码
     */
    @DCEPField(name = "SUB_MERCHANTID", orderNonNull = true, order = 4)
    private String subMerchantId;

    /**
     * 公钥 计算mac用 不传参
     */
    @DCEPField(order = 14)
    private String pub;

    /**
     * 交易标识位 固定值3 不参与mac校验
     */
    @DCEPField(name = "TX_FLAG")
    private String txFlag = "3";

}
