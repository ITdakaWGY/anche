package com.github.bluecatlee.dcep.vo.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bluecatlee.dcep.annotation.DCEPField;
import com.github.bluecatlee.dcep.vo.DCEPRequest;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 建行数币支付通知参数
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 15:01
 */
@Data
public class DCEPNotifyParams extends DCEPRequest {

    /**
     * 商户柜台代码
     */
    @JsonProperty("POSID")
    @DCEPField(order = 1)
    @NotEmpty(message = "POSID不能为空")
    private String posId;

    /**
     * 分行代码
     */
    @JsonProperty("BRANCHID")
    @DCEPField(order = 2)
    @NotEmpty(message = "BRANCHID不能为空")
    private String branchId;

    /**
     * 订单号
     */
    @JsonProperty("ORDERID")
    @DCEPField(order = 3)
    @NotEmpty(message = "ORDERID不能为空")
    private String orderId;

    /**
     * 付款金额
     */
    @JsonProperty("PAYMENT")
    @DCEPField(order = 4)
    @NotEmpty(message = "PAYMENT不能为空")
    private String payment;

    /**
     * 币种
     */
    @JsonProperty("CURCODE")
    @DCEPField(order = 5)
    @NotEmpty(message = "CURCODE不能为空")
    private String curCode;

    /**
     * 备注1
     */
    @JsonProperty("REMARK1")
    @DCEPField(order = 6)
    @NotNull(message = "REMARK1不能为空")
    private String remark1;

    /**
     * 备注2
     */
    @JsonProperty("REMARK2")
    @DCEPField(order = 7)
    @NotNull(message = "REMARK2不能为空")
    private String remark2;

    /**
     * 账户类型
     */
    @JsonProperty("ACC_TYPE")
    @DCEPField(order = 8, name = "ACC_TYPE")
    private String accType;

    /**
     * 成功标志     成功－Y，失败－N
     */
    @JsonProperty("SUCCESS")
    @DCEPField(order = 9)
    @NotEmpty(message = "SUCCESS不能为空")
    private String success;

    /**
     * 接口类型     分行业务人员在P2员工渠道后台设置防钓鱼的开关
     *      1.开关关闭时，无此字段返回且不参与验签。
     *      2.开关打开时，有此字段返回且参与验签。
     *      参数值为1时表示开关打开
     */
    @JsonProperty("TYPE")
    @DCEPField(order = 10, orderNonNull = true)
    private String type;

    /**
     * Referer信息    分行业务人员在P2员工渠道后台设置防钓鱼开关。
     *      1.开关关闭时，无此字段返回且不参与验签。
     *      2.开关打开时，有此字段返回且参与验签。
     */
    @JsonProperty("REFERER")
    @DCEPField(order = 11, orderNonNull = true)
    private String referer;

    /**
     * 客户端IP        客户在商户系统中的IP，即客户登陆（访问）商户系统时使用的ip）
     */
    @JsonProperty("CLIENTIP")
    @DCEPField(order = 12, orderNonNull = true)
    private String clientip;

    /**
     * 系统记账日期       商户登陆商户后台设置返回记账日期的开关
     *       1.开关关闭时，无此字段返回且不参与验签。
     *       2.开关打开时，有此字段返回且参与验签。参数值格式为YYYYMMDD（如20100907）。
     */
    @JsonProperty("ACCDATE")
    @DCEPField(order = 13, orderNonNull = true)
    private String accDate;

    /**
     * 数字签名
     */
    @JsonProperty("SIGN")
    @NotEmpty(message = "SIGN不能为空")
    private String sign;

}
