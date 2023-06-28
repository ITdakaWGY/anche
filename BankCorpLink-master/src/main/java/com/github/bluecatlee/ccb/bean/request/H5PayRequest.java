package com.github.bluecatlee.ccb.bean.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.bluecatlee.ccb.annotation.CCBPayField;
import lombok.Data;

/**
 * h5支付请求参数
 *
 * @Date 2021/8/13 11:15
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class H5PayRequest {

    /**
     * 系统参数 默认V6
     */
    @CCBPayField(required = true, name = "CCB_IBSVersion")
    private String version = "V6";

    /**
     * 加密串
     */
    @CCBPayField(required = true)
    private String mac;

    /**
     * 商户代码
     */
    @CCBPayField(required = true)
    private String merchantId;

    /**
     * 商户柜台代码
     */
    @CCBPayField(required = true)
    private String posId;

    /**
     * 分行代码
     */
    @CCBPayField(required = true)
    private String branchId;

    /**
     * 订单号
     */
    @CCBPayField(required = true)
    private String orderId;

    /**
     * 付款金额
     */
    @CCBPayField(required = true)
    private String payment;

    /**
     * 币种   只支持人民币支付
     *      01-人民币
     */
    @CCBPayField(required = true)
    private String curCode = "01";

    /**
     * 交易码
     */
    @CCBPayField(required = true)
    private String txCode = "SDKW05";

    /**
     * 备注1
     */
    private String remark1;

    /**
     * 备注2
     */
    private String remark2;

    /**
     * 接口类型
     */
    @CCBPayField(required = true)
    private String type;

    /**
     * 公钥后30位
     */
//    @CCBPayField(required = true)
    private String pub;

    /**
     * 网关类型     默认送0
     */
    @CCBPayField(required = true)
    private String gateway;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 客户注册信息
     */
    @CCBPayField(needEscape = true)
    private String regInfo;

    /**
     * 商品信息
     */
    @CCBPayField(needEscape = true)
    private String proInfo;

    /**
     * 商户URL
     */
    private String referer;

    // 以下为二级商户信息 目前无用
    /***********************************************/
    /**
     * 二级商户代码
     */
    private String sMerId;

    /**
     * 二级商户名称
     */
    @CCBPayField(needEscape = true)
    private String sMerName;

    /**
     * 二级商户类别代码
     */
    private String sMerTypeId;

    /**
     * 二级商户类别名称
     */
    @CCBPayField(needEscape = true)
    private String sMerType;

    /**
     * 交易类型代码
     */
    private String tradeCode;

    /**
     * 交易类型名称
     */
    @CCBPayField(needEscape = true)
    private String tradeName;

    /**
     * 商品类别代码
     */
    private String sMeProType;

    /**
     * 商品类别名称
     */
    @CCBPayField(needEscape = true)
    private String proName;
    /******************************************************/

    /**
     * 订单超时时间
     */
    private String timeout;

    /**
     * 证件类型
     */
    private String lzfIdType;

    /**
     * 证件号码
     */
    private String lzfUserId;

    /**
     * 客户名称
     */
    @CCBPayField(needEscape = true)
    private String lzfUsername;

    /**
     * 客户手机号
     */
    private String lzfMobile;


    /**
     * 商户URL
     *      退出支付流程时返回商户 URL，小程序接入方式不生效
     */
    @CCBPayField(name = "Mrch_url")
    private String merchantUrl;

    /**
     * 标志位
     *      交易标志位 固定值3
     */
    @CCBPayField(name = "TX_FLAG")
    private String txFlag = "3";

}
