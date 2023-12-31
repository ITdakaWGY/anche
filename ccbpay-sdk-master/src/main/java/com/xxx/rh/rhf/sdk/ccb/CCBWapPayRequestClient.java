package com.xxx.rh.rhf.sdk.ccb;

import com.alibaba.fastjson.JSONObject;
import com.xxx.rh.rhf.sdk.ccb.external.CCBMCipherEncryptor;
import com.xxx.rh.rhf.sdk.ccb.external.CCBRSASig;
import com.xxx.rh.rhf.sdk.ccb.internal.codec.MD5;
import com.xxx.rh.rhf.sdk.ccb.internal.utils.HttpUtils;
import com.xxx.rh.rhf.sdk.ccb.internal.utils.StringUtils;
import com.xxx.rh.rhf.sdk.ccb.internal.utils.XmlUtils;
import com.xxx.rh.rhf.sdk.ccb.domain.wap.*;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.util.*;

/**
 * 建行 Wap 支付
 * @author
 * @version 1.0  2021/10/6
 */
public class CCBWapPayRequestClient extends CCBPayBaseRequestClient {

    /**
     * 扫码支付，被扫
     *  - 当 returntype 为 2 时，url 里面放 html 网页
     *  - 当 returntype 为 3 时，url 里面放网页链接
     *
     * @author   v1.0   2021/10/6
     */
    public CCBWapQRCodePayResp qrCodePay(CCBWapQRCodePayReq model, String bankUrl, String publicKey) throws CCBPayApiException {
        // 转化为请求 map
        Map<String, String> reqParams = model.toReqMap();
        if (StringUtils.isEmpty(reqParams.get("CCB_IBSVersion"))) {
            reqParams.put("CCB_IBSVersion", "V6");
        }
        // 获取签名码
        String signData = "MERCHANTID=" + reqParams.get("MERCHANTID") +
                "&POSID=" + reqParams.get("POSID") +
                "&BRANCHID=" + reqParams.get("BRANCHID") +
                "&ORDERID=" + reqParams.get("ORDERID") +
                "&PAYMENT=" + reqParams.get("PAYMENT") +
                "&CURCODE=" + reqParams.get("CURCODE") +
                "&TXCODE=" + reqParams.get("TXCODE") +
                "&REMARK1=" + reqParams.get("REMARK1") +
                "&REMARK2=" + reqParams.get("REMARK2") +
                "&RETURNTYPE=" + reqParams.get("RETURNTYPE") +
                "&TIMEOUT=" + reqParams.get("TIMEOUT") +
                "&PUB=" + publicKey.substring(publicKey.length() - 30);

        logger.info("获取建行(wap)二维码，签名原始数据为：" + signData);
        reqParams.put("MAC", MD5.md5Str(signData, CCBPayConstants.CHARSET_UTF8).toLowerCase());

        // 发送并同步获取响应
        logger.info("获取建行(wap)二维码，请求参数为：" + JSONObject.toJSONString(reqParams));
        try {
            String recMsg = HttpUtils.doPost(bankUrl, reqParams, CCBPayConstants.CHARSET_UTF8, 5000, 5000, "", 0);
            logger.info("获取建行(wap)二维码 post，建行端响应报文为：" + recMsg);

            if ("2".equals(reqParams.get("RETURNTYPE"))) {
                return new CCBWapQRCodePayResp(Boolean.TRUE.toString(), recMsg);
            }

            JSONObject postRes = JSONObject.parseObject(recMsg);
            if (Boolean.FALSE.toString().equals(postRes.getString("SUCCESS"))) {
                return new CCBWapQRCodePayResp(Boolean.FALSE.toString(), "");
            }

            recMsg = HttpUtils.doGet(postRes.getString("PAYURL"), null);
            logger.info("获取建行(wap)二维码 get，建行端响应报文为：" + recMsg);
            if (recMsg == null) {
                return new CCBWapQRCodePayResp(Boolean.FALSE.toString(), "");
            }

            JSONObject getRes = JSONObject.parseObject(recMsg);
            if (!Boolean.TRUE.toString().equals(getRes.getString("SUCCESS"))) {
                return new CCBWapQRCodePayResp(Boolean.FALSE.toString(), "");
            }
            String url = URLDecoder.decode(getRes.getString("QRURL"), CCBPayConstants.CHARSET_UTF8);
            return new CCBWapQRCodePayResp(Boolean.TRUE.toString(), url);
        } catch (Exception e) {
            logger.error("获取建行(wap)二维码异常", e);
            throw new CCBPayApiException(e);
        }
    }

    /**
     * 扫码支付查询，被扫
     * @author   v1.0   2021/10/6
     */
    public CCBWapQRCodePayQueryResp qrCodePayQuery(CCBWapQRCodePayQueryReq model, String bankUrl, String publicKey) throws CCBPayApiException {
        // 转化为请求 map
        Map<String, String> reqParams = model.toReqMap();
        // 生成签源字符串
        String sign = model.buildSignStr();
        logger.info("建行(wap)交易流水查询，签名原始数据为：" + sign);
        String macParam = MD5.md5Str(sign, CCBPayConstants.CHARSET_UTF8).toLowerCase();
        reqParams.put("MAC", macParam);

        try {
            logger.info("建行(wap)交易流水查询，请求报文为：" + JSONObject.toJSONString(reqParams));
            String respXml = HttpUtils.doPost(bankUrl, reqParams, CCBPayConstants.CHARSET_UTF8, 5000, 5000, "", 0);
            logger.info("建行(wap)交易流水查询，建行端响应报文为：" + respXml);
            respXml = (respXml==null) ? "" : respXml.trim();

            // 解析返回报文
            Element root = XmlUtils.getRootElementFromStream(new ByteArrayInputStream(respXml.getBytes(CCBPayConstants.CHARSET_UTF8)));
            CCBWapQRCodePayQueryResp resp = new CCBWapQRCodePayQueryResp();
            resp.setReturnCode(XmlUtils.getElementValue(root, "RETURN_CODE"));
            resp.setReturnMsg(XmlUtils.getElementValue(root, "RETURN_MSG"));
            resp.setCurPage(XmlUtils.getElementValue(root, "CURPAGE"));
            resp.setPageCount(XmlUtils.getElementValue(root, "PAGECOUNT"));
            resp.setTotal(XmlUtils.getElementValue(root, "TOTAL"));
            resp.setPayAmount(XmlUtils.getElementValue(root, "PAYAMOUNT"));
            resp.setRefundAmount(XmlUtils.getElementValue(root, "REFUNDAMOUNT"));

            // 如果节点不为空，继续组装实体类
            List<Element> orderEles = XmlUtils.getChildElements(root, "QUERYORDER");
            if (orderEles == null || orderEles.isEmpty()) {
                return resp;
            }
            List<Map<String, String>> details = new ArrayList<>();
            for (Element orderEle : orderEles) {
                Map<String, String> map = new HashMap<>();
                map.put("MERCHANTID", XmlUtils.getElementValue(orderEle, "MERCHANTID"));
                map.put("BRANCHID", XmlUtils.getElementValue(orderEle, "BRANCHID"));
                map.put("POSID", XmlUtils.getElementValue(orderEle, "POSID"));
                map.put("ORDERID", XmlUtils.getElementValue(orderEle, "ORDERID"));
                map.put("ORDERDATE", XmlUtils.getElementValue(orderEle, "ORDERDATE"));
                map.put("ACCDATE", XmlUtils.getElementValue(orderEle, "ACCDATE"));
                map.put("AMOUNT", XmlUtils.getElementValue(orderEle, "AMOUNT"));
                map.put("STATUSCODE", XmlUtils.getElementValue(orderEle, "STATUSCODE"));
                map.put("STATUS", XmlUtils.getElementValue(orderEle, "STATUS"));
                map.put("REFUND", XmlUtils.getElementValue(orderEle, "REFUND"));
                map.put("SIGN", XmlUtils.getElementValue(orderEle, "SIGN"));
                details.add(map);
            }
            resp.setDetails(details);
            return resp;
        } catch(Exception e){
            logger.error("建行(wap)交易流水查询异常", e);
            throw new CCBPayApiException(e);
        }
    }

    /**
     * 条码支付，主扫
     * @author   v1.0   2021/10/6
     */
    public CCBWapScanPayResp scanPay(CCBWapScanPayReq model, String bankUrl, String publicKey) throws CCBPayApiException {
        // 转化为请求 map
        Map<String,String> reqParams = model.toReqMap();
        // 生成签名码
        String signData = model.buildSignStr();
        logger.info("建行(wap)条码支付，签名原始数据为：" + signData);
        try {
            CCBMCipherEncryptor ccbMCipherEncryptor = new CCBMCipherEncryptor(publicKey.substring(publicKey.length() - 30));
            String ccbParam = ccbMCipherEncryptor.doEncrypt(signData);
            reqParams.put("ccbParam", ccbParam);
        } catch (Exception e) {
            return new CCBWapScanPayResp("N", "", "E8001", "加密异常，交易失败，订单未发送到建行");
        }
        // 移除不需要的参数
        reqParams.remove("ORDERID");
        reqParams.remove("QRCODE");
        reqParams.remove("AMOUNT");
        // 发送主扫请求
        try {
            logger.info("建行(wap)条码支付，请求报文为：" + JSONObject.toJSONString(reqParams));
            String recMsg = HttpUtils.doPost(bankUrl, reqParams, CCBPayConstants.CHARSET_UTF8, 5000, 5000, "", 0);
            logger.info("建行(wap)条码支付，建行端响应报文为：" + recMsg);

            JSONObject respJson = JSONObject.parseObject(recMsg);
            CCBWapScanPayResp respModel = new CCBWapScanPayResp();
            respModel.setResult(respJson.getString("RESULT"));
            respModel.setOrderId(respJson.getString("ORDERID"));
            respModel.setQrcodeType(respJson.getString("QRCODETYPE"));
            respModel.setAmount(respJson.getString("AMOUNT"));
            respModel.setWaitTime(respJson.getString("WAITTIME"));
            respModel.setTraceId(respJson.getString("TRACEID"));
            respModel.setErrCode(respJson.getString("ERRCODE"));
            respModel.setErrMsg(respJson.getString("ERRMSG"));
            respModel.setSign(respJson.getString("SIGN"));
            if (StringUtils.isBlank(respJson.getString("SIGN"))) {
                return respModel;
            }

            String signStr = "RESULT=" + respJson.getString("RESULT")
                    + "&ORDERID=" + respJson.getString("ORDERID")
                    + "&AMOUNT=" + respJson.getString("AMOUNT")
                    + "&WAITTIME=" + StringUtils.trimToEmpty(respJson.getString("WAITTIME"))
                    + "&TRACEID=" + StringUtils.trimToEmpty(respJson.getString("TRACEID"));
            if (!new CCBRSASig(publicKey).verifySigature(respJson.getString("SIGN"), signStr)) {
                logger.error("建行(wap)条码支付验签失败");
                return new CCBWapScanPayResp("Q", "5", "", "验签失败");
            }

            return respModel;
        } catch (Exception e) {
            logger.error("解析建行(wap)条码支付响应报文时异常", e);
            throw new CCBPayApiException(e);
        }
    }

    /**
     * 条码支付查询，主扫
     * @author   v1.0   2021/10/6
     */
    public CCBWapScanPayQueryResp scanPayQuery(CCBWapScanPayQueryReq model, String bankUrl, String publicKey) throws CCBPayApiException {
        // 转化为请求 map
        Map<String, String> reqParams = model.toReqMap();
        // 生成签名码
        String sign = model.buildSignStr();
        logger.info("建行(wap)交易流水查询，签名原始数据为：" + sign);
        try {
            CCBMCipherEncryptor ccbMCipherEncryptor = new CCBMCipherEncryptor(publicKey.substring(publicKey.length() - 30));
            String ccbParam = ccbMCipherEncryptor.doEncrypt(sign);
            reqParams.remove("ORDERID");
            reqParams.put("ccbParam", ccbParam);
        } catch (Exception e) {
            logger.error("建行(wap)交易流水查询加密异常", e);
            return new CCBWapScanPayQueryResp("U", "5", "", "加密异常，交易未知");
        }

        // 查询请求
        try {
            logger.info("建行(wap)交易流水查询，请求报文为:" + JSONObject.toJSONString(reqParams));
            String recMsg = HttpUtils.doPost(bankUrl, reqParams, CCBPayConstants.CHARSET_UTF8, 5000, 5000, "", 0);
            logger.info("建行(wap)交易流水查询，建行端响应报文为:" + recMsg);

            JSONObject recResult = JSONObject.parseObject(recMsg);
            CCBWapScanPayQueryResp respModel = new CCBWapScanPayQueryResp();
            respModel.setResult(recResult.getString("RESULT"));
            respModel.setOrderId(recResult.getString("ORDERID"));
            respModel.setAmount(recResult.getString("AMOUNT"));
            respModel.setWaitTime(recResult.getString("WAITTIME"));
            respModel.setErrCode(recResult.getString("ERRCODE"));
            respModel.setErrMsg(recResult.getString("ERRMSG"));
            respModel.setSign(recResult.getString("SIGN"));
            if (StringUtils.isBlank(recResult.getString("SIGN"))) {
                return respModel;
            }

            String signStr = "RESULT=" + recResult.getString("RESULT")
                    + "&ORDERID=" + recResult.getString("ORDERID")
                    + "&AMOUNT=" + StringUtils.trimToEmpty(recResult.getString("AMOUNT"))
                    + "&WAITTIME=" + StringUtils.trimToEmpty(recResult.getString("WAITTIME"));

            if (!new CCBRSASig(publicKey).verifySigature(recResult.getString("SIGN"), signStr)) {
                logger.error("建行(wap)交易流水查询，验签失败");
                return new CCBWapScanPayQueryResp("U", "5", "", "验签失败，交易未知");
            }
            return respModel;
        } catch (Exception e) {
            logger.error("解析建行(wap)交易流水查询响应报文时异常", e);
            throw new CCBPayApiException(e);
        }
    }

}
