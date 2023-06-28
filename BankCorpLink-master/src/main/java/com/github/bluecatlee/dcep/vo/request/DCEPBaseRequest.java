package com.github.bluecatlee.dcep.vo.request;

import com.github.bluecatlee.dcep.annotation.DCEPField;
import com.github.bluecatlee.dcep.vo.DCEPRequest;
import com.github.bluecatlee.dcep.vo.TxCodeAware;
import lombok.Data;

/**
 * Transaction_Header 商户平台交易请求通用参数
 *
 * @Author Bluecatlee
 * @Date 2021/10/11 11:12
 */
@Data
public abstract class DCEPBaseRequest extends DCEPRequest implements TxCodeAware {

    /**
     * 调用方id
     */
    @DCEPField(required = true, name = "FT_CORPID", order = 1)
    private String ftCorpId;

    /**
     * 接口id 瓦片编号
     */
    @DCEPField(required = true, name = "FT_TILEID", order = 2)
    private String ftTileId;

    /**
     * 接口使用场景
     */
    @DCEPField(required = true, name = "FT_SCENARIO", order = 3)
    private String ftScenario;

    /**
     * 接口订单号
     */
    @DCEPField(required = true, name = "FT_ORDERNO", order = 4)
    private String ftOrderNo;

    /**
     * 建行商户编号
     */
    @DCEPField(required = true, order = 5)
    private String customerId;

    /**
     * 操作员号
     */
    @DCEPField(required = true, order = 6)
    private String userId;

    /**
     * 密码
     */
    @DCEPField(required = true, order = 7)
    private String password;

    /**
     * 交易码
     */
    @DCEPField(required = true, order = 8)
    private String txCode;

    /**
     * 语言
     */
    @DCEPField(required = true, order = 9)
    private String language = "CN";

    /**
     * 版本 固定值
     */
    @DCEPField(required = true, name = "CCB_IBSVersion", order = 10)
    private String version = "V6";

    /**
     * 固定值
     */
    @DCEPField(required = true, name = "PT_STYLE", order = 11)
    private String ptStyle = "F";

    /**
     * 固定值
     */
    @DCEPField(required = true, strategy = DCEPField.Strategy.NONE, order = 12)
    private String resType = "jsp";

}
