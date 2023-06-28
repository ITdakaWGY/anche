package com.github.bluecatlee.dcep.controller;

import com.github.bluecatlee.dcep.DCEPClient;
import com.github.bluecatlee.dcep.service.impl.DCEPPayServiceImpl;
import com.github.bluecatlee.dcep.vo.response.AuthorizeAndAddressingResult;
import com.github.bluecatlee.redundance.*;
import com.github.bluecatlee.redundance.bean.DCEPPayRequest;
import com.github.bluecatlee.redundance.bean.DCEPPayResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author Bluecatlee
 * @Date 2021/10/14 9:01
 */
@RestController
@RequestMapping("/dcep")
@Api(tags = "建行数币支付")
public class DCEPPayController {

    @Resource
    private DCEPClient dcepClient;

    @Resource
    private DCEPPayServiceImpl dcepPayService;

    @ApiOperation(value = "刷新缓存")
    @GetMapping("/refresh")
    public MessagePack refresh() {
        MessagePack messagePack = new MessagePack();
        try {
            AuthorizeAndAddressingResult authorizeAndAddressingResult = dcepClient.authorizeAndAddressing(true);
            String s = JSONUtil.bean2json(authorizeAndAddressingResult);
            messagePack.setCode(MessagePack.OK);
            messagePack.setFullMessage(s);
        } catch (Exception e) {
//            e.printStackTrace();
            messagePack.setCode(MessagePack.EXCEPTION);
            messagePack.setMessage(e.getMessage());
        }
        return messagePack;
    }

    @ApiOperation(value = "查询支付结果")
    @ApiImplicitParams({
            @ApiImplicitParam(name="outTradeNo", value="支付单号", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name="orderDate", value="订单日期/交易日期 yyyy-MM-dd", dataTypeClass = String.class),
    })
    @GetMapping("/queryPayResult")
    public BaseResponse queryPayResult(String outTradeNo, String orderDate) {

        DCEPPayRequest dcepPayRequest = new DCEPPayRequest();
        DCEPPayResponse dcepPayResponse = new DCEPPayResponse();
        dcepPayRequest.setOutTradeNo(outTradeNo);
        dcepPayRequest.setOrdeDate(orderDate);//yyyy-MM-dd
        dcepPayRequest.setPlatType(Constants.DCEP_PAY_TYPE);

        try {
            dcepPayService.queryPayResult(dcepPayRequest, dcepPayResponse);
        } catch (Exception e) {
            dcepPayResponse.setCode(RespEnum.FAIL.getStatus());
            dcepPayResponse.setMessage(e.getMessage());
        }
        return dcepPayResponse;
    }

    @ApiOperation(value = "退款")
    @ApiImplicitParams({
            @ApiImplicitParam(name="srcOutTradeNo", value="支付单号", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name="outTradeNo", value="退款单号", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name="orderDate", value="订单日期/交易日期 yyyy-MM-dd", dataTypeClass = String.class),
            @ApiImplicitParam(name="refundAmount", value="退款金额(分)", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name="channel", value="渠道", defaultValue = "98", dataTypeClass = String.class),
    })
    @GetMapping("/refund")
    public BaseResponse refund(String outTradeNo, String srcOutTradeNo, String orderDate, Long refundAmount, String channel) {

        DCEPPayRequest dcepPayRequest = new DCEPPayRequest();
        DCEPPayResponse dcepPayResponse = new DCEPPayResponse();
        dcepPayRequest.setOutTradeNo(outTradeNo);
        dcepPayRequest.setSrcOutTradeNo(srcOutTradeNo);
        dcepPayRequest.setTotalFee(String.valueOf(refundAmount)); // 分
        dcepPayRequest.setOrdeDate(orderDate);//yyyy-MM-dd
        dcepPayRequest.setChannel(channel);
        dcepPayRequest.setPlatType(Constants.DCEP_PAY_TYPE);

        try {
            dcepPayService.refund(dcepPayRequest, dcepPayResponse);
        } catch (Exception e) {
            dcepPayResponse.setCode(RespEnum.FAIL.getStatus());
            dcepPayResponse.setMessage(e.getMessage());
        }
        return dcepPayResponse;
    }

    @ApiOperation(value = "查询退款结果")
    @ApiImplicitParams({
            @ApiImplicitParam(name="srcOutTradeNo", value="支付单号", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name="outTradeNo", value="退款单号", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name="orderDate", value="订单日期/交易日期 yyyy-MM-dd", dataTypeClass = String.class),
    })
    @GetMapping("/queryRefundResult")
    public BaseResponse queryRefundResult(String srcOutTradeNo, String outTradeNo, String orderDate) {

        DCEPPayRequest dcepPayRequest = new DCEPPayRequest();
        DCEPPayResponse dcepPayResponse = new DCEPPayResponse();
        dcepPayRequest.setSrcOutTradeNo(srcOutTradeNo);
        dcepPayRequest.setOutTradeNo(outTradeNo);
        dcepPayRequest.setOrdeDate(orderDate);//yyyy-MM-dd
        dcepPayRequest.setPlatType(Constants.DCEP_PAY_TYPE);

        try {
            dcepPayService.queryRefundResult(dcepPayRequest, dcepPayResponse);
        } catch (Exception e) {
            dcepPayResponse.setCode(RespEnum.FAIL.getStatus());
            dcepPayResponse.setMessage(e.getMessage());
        }
        return dcepPayResponse;
    }

}
