package com.github.bluecatlee.ccb.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bluecatlee.ccb.CCBClient;
import com.github.bluecatlee.ccb.bean.CCBBaseResponse;
import com.github.bluecatlee.ccb.bean.NotifyParams;
import com.github.bluecatlee.redundance.bean.CCBPayRequest;
import com.github.bluecatlee.redundance.bean.CCBPayResponse;
import com.github.bluecatlee.ccb.bean.request.*;
import com.github.bluecatlee.ccb.bean.response.*;
import com.github.bluecatlee.ccb.constant.CCBBillStatusEnum;
import com.github.bluecatlee.ccb.constant.CCBOrderStatusEnum;
import com.github.bluecatlee.ccb.constant.CCBSuccessFlagEnum;
import com.github.bluecatlee.redundance.Constants;
import com.github.bluecatlee.redundance.*;
import com.github.bluecatlee.ccb.utils.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 建行龙支付实现
 *      基于支付中台框架的实现 todo 部分业务代码注释掉了 业务代码可以自行剥离
 *      todo 支付参数的页面通知地址应该由后台配置并参与加密 懒得重构了
 *      注意异步回调的入口在支付中台 建行龙支付以响应标准的http状态码=200时表示回调成功
 *
 * @see <a href="https://github.com/bluecatlee/SOA/tree/main/gs4d-pay">支付中台</a>
 * @Date 2021/2/23 15:12
 */
@Service("ccbPayServiceImpl")
@Slf4j
public class CCBPayServiceImpl implements PaymentService<CCBPayRequest, CCBPayResponse> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String notify_key_prefix = "paymentplatform_ccb_payNotify_";

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Autowired
    private CCBClient ccbClient;

    @Autowired
    private Validator validator;

//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;

//    @Autowired
//    private PayOrderInfoMapper payOrderInfoMapper; // 用了tkMybatis 懒得配置了 参考支付中台

//    @Autowired
//    private BdAccountManageService accountManageService;

    @Override
    public void checkInputParams(CCBPayRequest req, CCBPayResponse res, String requestMethod) throws Exception {

    }

    @Override
    public BaseResponse pay(CCBPayRequest req, CCBPayResponse res) throws Exception {
        log.info("CCBPayServiceImpl pay request: {}", OBJECT_MAPPER.writeValueAsString(req));

        String channel = req.getChannel();
        if ("98".equals(channel)) {
            // 小程序
            this.h5Pay(req, res);
        } else if ("99".equals(channel)) {
            // APP
            this.appPay(req, res);
        } else {
            throw new RuntimeException("建行龙支付仅支持APP、小程序渠道，当前渠道：" + channel);
        }

        log.info("CCBPayServiceImpl pay response: {}", OBJECT_MAPPER.writeValueAsString(res));
        return res;
    }

    private void h5Pay(CCBPayRequest req, CCBPayResponse res) throws Exception {
        H5PayRequest h5PayRequest = new H5PayRequest();
        h5PayRequest.setOrderId(req.getOutTradeNo()); // 订单号
        BigDecimal fee = MathUtil.centToYuan2(Integer.valueOf(req.getTotalFee()));
        h5PayRequest.setPayment(fee.toString());   // 付款金额
        h5PayRequest.setRemark1(req.getSubUnitNumId());
        h5PayRequest.setRemark2(req.getOrdeDate());

//        String timeoutStr = this.getTimeoutStr(req.getCreateDtme());
//        if (StringUtils.isNotBlank(timeoutStr)) {
//            h5PayRequest.setTimeout(timeoutStr);
//        }

//        String payParamsStr = ccbClient.prePay(h5PayRequest);
        ccbClient.prepare(h5PayRequest);

        String payParamsStr = "https://ibsbjstar.ccb.com.cn/CCBIS/ccbMain?" + OBJECT_MAPPER.writeValueAsString(h5PayRequest);   // 支付url

        res.setResBody(payParamsStr);
        res.setOutTradeNo(req.getOutTradeNo());
        res.setTradeStatus(TradeStatusEnum.PROCESSING.getStatus());
        res.setTradeStatusRes(payParamsStr);
        res.setCode(RespEnum.SUCCESS.getStatus());
        res.setTradeType(Byte.valueOf(TradeTypeEnum.PAY.getStatus()));
        res.setTotalFee(fee.doubleValue());

    }

    private void appPay(CCBPayRequest req, CCBPayResponse res) throws Exception {
        // 封装了请求参数 返给前端 前端调用sdk进行支付
        PayRequest payRequest = new PayRequest();
        payRequest.setOrderId(req.getOutTradeNo()); // 订单号
        BigDecimal fee = MathUtil.centToYuan2(Integer.valueOf(req.getTotalFee()));
        payRequest.setPayment(fee.toString());   // 付款金额
        payRequest.setRemark1(req.getSubUnitNumId());
        payRequest.setRemark2(req.getOrdeDate());
//        payRequest.setClientIp(StringUtils.isBlank(req.getCreateIp()) ? "" : req.getCreateIp());

//        payRequest.setProInfo(StringUtils.isBlank(req.getTradeName()) ? "" : this.replaceSpecStr(req.getTradeName().trim()));  // 商品信息
//        payRequest.setRegInfo(StringUtils.isBlank(req.getUsrNumId()) ? "" : this.replaceSpecStr(req.getUsrNumId().trim()));   // 客户注册信息

//        payRequest.setInstallNum(null);   // 分期期数 不支持分期
//        payRequest.setThirdAppInfo(null); // 客户端标识

//        String timeoutStr = this.getTimeoutStr(req.getCreateDtme());
//        if (StringUtils.isNotBlank(timeoutStr)) {
//            payRequest.setTimeout(timeoutStr);
//        }

        payRequest.setPayMap("1100000000");       // 支付方式位图 支持支付宝和微信支付

//        String payParamsStr = "https://ibsbjstar.ccb.com.cn/CCBIS/ccbMain?" + ccbClient.prePay(payRequest);   // 支付url
        ccbClient.prepare(payRequest);

        String payParamsStr = OBJECT_MAPPER.writeValueAsString(payRequest);

        res.setResBody(payParamsStr);
        res.setOutTradeNo(req.getOutTradeNo());
        res.setTradeStatus(TradeStatusEnum.PROCESSING.getStatus());
        res.setTradeStatusRes(payParamsStr);
        res.setCode(RespEnum.SUCCESS.getStatus());
        res.setTradeType(Byte.valueOf(TradeTypeEnum.PAY.getStatus()));
        res.setTotalFee(fee.doubleValue());
    }

    @Override
    public Map afterPay(CCBPayRequest req, String res, long id) throws Exception {
        return null;
    }

    @Override
    public BaseResponse refund(CCBPayRequest req, CCBPayResponse res) throws Exception {
        log.info("CCBPayServiceImpl refund request: {}", OBJECT_MAPPER.writeValueAsString(req));

        // 由于建行没有对退款流水号做业务幂等 校验当前退款流水号是否发起过退款请求
        String outTradeNo = req.getOutTradeNo();
        if (StringUtils.isBlank(outTradeNo)) {
//            throw new BussinessException("建行退款失败: 建行退款必须传退款流水号outTradeNo!");
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes("建行退款失败: 建行退款必须传退款流水号outTradeNo!");
            log.info("CCBPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }
        PayOrderInfo condition = new PayOrderInfo().setOutTradeNo(outTradeNo)
                .setPlatType(Integer.valueOf(req.getPlatType()))
                .setTradeType(Byte.valueOf(TradeTypeEnum.REFUND.getStatus()));
//        PayOrderInfo payOrderInfo = payOrderInfoMapper.selectPayInfoByOutTradeNo(condition);
        PayOrderInfo payOrderInfo = null;
        if (payOrderInfo != null /*&& (TradeStatusEnum.SUCCESS.getStatus().equals(payOrderInfo.getTradeStatus())
                                        || TradeStatusEnum.PROCESSING.getStatus().equals(payOrderInfo.getTradeStatus()))*/) {
//            throw new BussinessException("建行退款失败: 该退款流水号outTradeNo("+outTradeNo+")已经发起过退款！");
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes("建行退款失败: 该退款流水号outTradeNo("+outTradeNo+")已经发起过退款！");
            log.info("CCBPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrder(req.getSrcOutTradeNo());
        String totalFee = req.getTotalFee();
        if (totalFee.startsWith("-")) {
            totalFee = totalFee.substring(1);               // 退款金额转成正数
        }
        if (StringUtils.equals("90", req.getChannel())) {
            // 90渠道传过来的金额是元为单位
            refundRequest.setMoney(totalFee);
        } else {
            // 线上渠道传过来的单位是分为单位
            BigDecimal fee = MathUtil.centToYuan2(Integer.valueOf(totalFee));
            refundRequest.setMoney(fee.toString());
        }

        refundRequest.setRefundCode(outTradeNo);

//        CCBBaseResponse<RefundResponse> baseResponse = ccbClient.executeRequestNew(refundRequest, new TypeReference<CCBBaseResponse<RefundResponse>>() {});
        CCBBaseResponse<RefundResponse> baseResponse = ccbClient.executeRequestNew(refundRequest, RefundResponse.class);

        if (MessagePack.OK != baseResponse.getCode()) {
            res.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            log.info("CCBPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        RefundResponse refundResponse = baseResponse.getCcbResponseBody();
        String amount = refundResponse.getAmount();

        res.setOutTradeNo(req.getOutTradeNo());
        res.setSrcOutTradeNo(req.getSrcOutTradeNo());
        res.setTotalFee(new BigDecimal(amount).doubleValue());
        res.setCode(RespEnum.SUCCESS.getStatus());
        res.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
        res.setTradeType(Byte.valueOf(TradeTypeEnum.REFUND.getStatus()));
        res.setTradeStatusRes(JSON.toJSONString(baseResponse));

//        res.setExt1("");   // todo 这里最好返回优惠金额 但是三方的退款接口并没有返 需要查询

        log.info("CCBPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
        return res;
    }

    @Override
    public Map afterRefund(CCBPayRequest req, String res, long id) throws Exception {
        return null;
    }

    @Override
    public BaseResponse queryPayResult(CCBPayRequest req, CCBPayResponse res) throws Exception {
        log.info("CCBPayServiceImpl queryPayResult request: {}", OBJECT_MAPPER.writeValueAsString(req));

        PayQueryRequest payQueryRequest = new PayQueryRequest();
//        payQueryRequest.setKind("");      // 流水类型 todo
        payQueryRequest.setOrder(req.getOutTradeNo());
        payQueryRequest.setNOrderBy("1");
        payQueryRequest.setPage("1");
        payQueryRequest.setStatus(CCBBillStatusEnum.ALL.getStatus());

//        CCBBaseResponse<PayQueryResponse> baseResponse = ccbClient.executeRequestNew(payQueryRequest, new TypeReference<CCBBaseResponse<PayQueryResponse>>() {
//        });
        CCBBaseResponse<PayQueryResponse> baseResponse = ccbClient.executeRequestNew(payQueryRequest, PayQueryResponse.class);
//        ExceptionUtil.checkDubboException(payQueryResponse);

        res.setTradeType(Byte.valueOf(TradeTypeEnum.QUERY.getStatus()));
        if (MessagePack.OK != baseResponse.getCode()) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            log.info("CCBPayServiceImpl queryPayResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        PayQueryResponse payQueryResponse = baseResponse.getCcbResponseBody();
        List<PayDetails> details = payQueryResponse.getDetails();
        PayDetails detail = details.get(0);             //
        String orderStatus = detail.getOrderStatus();
        if (CCBOrderStatusEnum.SUCCESS.getStatus().equals(orderStatus) ||
                CCBOrderStatusEnum.REBATE.getStatus().equals(orderStatus) || CCBOrderStatusEnum.FULL_REDUND.getStatus().equals(orderStatus)) {
            // 支付成功 理论上如果返回部分退款和全部退款也算是支付成功 因为支付成功才能退款
            res.setTransactionId(detail.getOriOvrlsttnEVTrckNo());
            res.setTotalFee(new BigDecimal(detail.getOrigAmt()).doubleValue());
//            res.setTotalFee(new BigDecimal(detail.getTxnAmt()).doubleValue());     // todo 真实扣卡金额TxnAmtv不是必返的
//            if (StringUtils.isNotBlank(detail.getDiscountAmt())) {
//                res.setExt1(detail.getDiscountAmt());        // todo 存在查询出支付成功并且需要补支付完成的业务的逻辑 理论上需要返回优惠金额
//            }
            res.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        } else if (CCBOrderStatusEnum.TBC.getStatus().equals(orderStatus) || CCBOrderStatusEnum.TBC2.getStatus().equals(orderStatus)) {
            // 待银行确认
//            res.setTransactionId(detail.getOriOvrlsttnEVTrckNo());
//            res.setTotalFee(new BigDecimal(detail.getOrigAmt()).doubleValue());
            res.setTradeStatus(TradeStatusEnum.PROCESSING.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        } else /*if (CCBOrderStatusEnum.FAIL.getStatus().equals(orderStatus))*/ {
            res.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());

        }

        log.info("CCBPayServiceImpl queryPayResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
        return res;
    }

    @Override
    public BaseResponse queryRefundResult(CCBPayRequest req, CCBPayResponse res) throws Exception {
        log.info("CCBPayServiceImpl queryRefundResult request: {}", OBJECT_MAPPER.writeValueAsString(req));

        RefundQueryRequest refundQueryRequest = new RefundQueryRequest();
        refundQueryRequest.setKind("");     // todo
        refundQueryRequest.setOrder(req.getSrcOutTradeNo());  // 只支持用订单号(支付时传给建行的编号)查询 不支持用退款流水号查 查完自行匹配退款流水
        refundQueryRequest.setNOrderBy("1");
        refundQueryRequest.setPage("1");
        refundQueryRequest.setStatus(CCBBillStatusEnum.ALL.getStatus());

        String outTradeNo = req.getOutTradeNo();    // 原退款流水号
        if (StringUtils.isBlank(outTradeNo)) {
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes("建行退款查询失败: 建行退款查询必须传退款流水号outTradeNo!");
            log.info("CCBPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

//        CCBBaseResponse<RefundQueryResponse> baseResponse = ccbClient.executeRequestNew(refundQueryRequest, new TypeReference<CCBBaseResponse<RefundQueryResponse>>() {
//        });
        CCBBaseResponse<RefundQueryResponse> baseResponse = ccbClient.executeRequestNew(refundQueryRequest, RefundQueryResponse.class);

        res.setTradeType(Byte.valueOf(TradeTypeEnum.QUERY.getStatus()));
        if (MessagePack.OK != baseResponse.getCode()) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            log.info("CCBPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        RefundQueryResponse refundQueryResponse = baseResponse.getCcbResponseBody();
        List<RefundDetails> details = refundQueryResponse.getDetails();
        if (CollectionUtils.isNotEmpty(details)) {
            details = details.stream().filter(d -> StringUtils.equals(d.getRefundCode(), outTradeNo)).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(details)) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setMessage(String.format("建行退款查询失败: 未查询到退款流水! 原单号：%s，退款流水号：%s", req.getSrcOutTradeNo(), outTradeNo));
            log.info("CCBPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        } else if (details.size() > 1) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setMessage(String.format("建行退款查询异常: 同一个退款流水号查询到多条退款记录！ 原单号：%s，退款流水号：%s", req.getSrcOutTradeNo(), outTradeNo));
            log.info("CCBPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        RefundDetails detail = details.get(0);
        String status = detail.getStatus();
        if (CCBOrderStatusEnum.SUCCESS.getStatus().equals(status)) {
            // 退款成功
            res.setTransactionId(detail.getOriOvrlsttnEVTrckNo());
            res.setTotalFee(new BigDecimal(detail.getOrigAmt()).doubleValue());
//            res.setTotalFee(new BigDecimal(detail.getTxnAmt()).doubleValue());
            res.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        } else if (CCBOrderStatusEnum.TBC.getStatus().equals(status) || CCBOrderStatusEnum.TBC2.getStatus().equals(status)) {
            // 待银行确认
//            res.setTransactionId(detail.getOriOvrlsttnEVTrckNo());
//            res.setTotalFee(new BigDecimal(detail.getOrigAmt()).doubleValue());
            res.setTradeStatus(TradeStatusEnum.PROCESSING.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        } else /*if (CCBOrderStatusEnum.FAIL.getStatus().equals(orderStatus))*/ {
            res.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(baseResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        }

        log.info("CCBPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
        return res;
    }

    @Override
    public BaseResponse callbackNotify(BaseRequest request, BaseResponse response) throws Exception {
        log.info("CCBPayServiceImpl callbackNotify request: {}", OBJECT_MAPPER.writeValueAsString(request));

        // 解析参数 不确定建行传参的方式 先尝试解析请求体中的键值对
        Map<String, String> flatParams = new HashMap<>();
        String body = request.getBody();
//        body = URLDecoder.decode(body, CCBClient.CHARSET_ISO_8859_1);   // url解码     建行签名的时候是按照iso-8859-1编码的，签名之后再进行url转义
        if (StringUtils.isNotBlank(body)) {
            String[] kvArr = body.split("&");
            if (kvArr != null && kvArr.length > 0) {
                for (int i = 0; i < kvArr.length; i++) {
                    String kvPair = kvArr[i];
                    if (StringUtils.isNotBlank(kvPair)) {
                        String[] split = kvPair.split("=", -1);     // 参数值中可能存在=  因此应该单独对参数值进行解码 不能直接对body体解码
                        if (split != null && split.length == 2) {
                            String key = split[0];
                            String value = split[1];
                            // 编码处理
                            if (key.equals("USRMSG")) {
                                // USRMSG字段不需要解码 而REFERER字段需要解码之后参与验签 ...
                                // USRMSG字段未配置返回
                                // todo USRINFO、PAYMENT_DETAILS等字段没有配置返回  不确定验签前是否需要解码
                                flatParams.put(key, value);
                            } else {
                                flatParams.put(key, URLDecoder.decode(value, CCBClient.CHARSET_ISO_8859_1));

                            }
                        }
                    }
                }
            }
        }
//        if (flatParams.size() == 0) { // 如果从body中未解析到 再尝试从request.getParameterMap()中获取参数
//            Map<String, Object> params = request.getRequestParam();
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//                String[] arr = null;
//                if (value instanceof JSONArray) {           // Map<String, String[]>在前面步骤中转成jsonStr再反序列化回来后变成了 Map<String, JSONArray>
//                    JSONArray jsonArray = (JSONArray)entry.getValue();
//                    arr = jsonArray.toArray(new String[]{});
//                } else if (value instanceof String[]) {
//                    arr = (String[])entry.getValue();
//                }
//                if (arr == null) {
//                    throw new Exception("建行异步回调失败：参数解析失败! ");
//                }
////            String valueStr = StringUtils.join(arr, ",");
//                String valueStr = arr[0];
//                // 编码处理
////                byte[] tempByte = valueStr.getBytes(CCBClient.CHARSET_UTF_8);
////                String a = new String(tempByte, CCBClient.CHARSET_ISO_8859_1);
//                flatParams.put(key, URLDecoder.decode(valueStr, CCBClient.CHARSET_ISO_8859_1));
//            }
//        }

        String requestStr = OBJECT_MAPPER.writeValueAsString(request);
        String paramsStr = OBJECT_MAPPER.writeValueAsString(flatParams);
        log.info("建行异步回调：request = {}, params:{}", requestStr, paramsStr);

        NotifyParams notifyParams = OBJECT_MAPPER.readValue(paramsStr, NotifyParams.class);
        if (notifyParams == null) {
            throw new Exception("建行异步回调失败：通知参数解析异常!");
        }

        String orderId = notifyParams.getOrderId();  // 传给建行的单号 即outTradeNo
        request.setOutTradeNo(orderId);
        response.setOutTradeNo(orderId);

        // 参数校验
        Set<ConstraintViolation<Object>> validateResults = validator.validate(notifyParams, Default.class);
        if (CollectionUtils.isNotEmpty(validateResults)) {
            for (ConstraintViolation<Object> result : validateResults) {
                String message = result.getMessage();
                throw new Exception("建行异步回调(单号："+orderId+")失败：" + message);
            }
        }

        if (notifyParams.getAccType() == null) {
            // 没有ACC_TYPE参数说明是页面回调 不处理
            throw new Exception("建行异步回调(单号："+orderId+")：本次回调不是服务器回调，是页面回调！请检查页面回调的地址配置");
        }

        // 验签
        boolean signResult = ccbClient.verifySign(notifyParams);
        if (!signResult) {
//            throw new Exception("建行异步回调(单号："+orderId+")失败：验签失败");
            log.info("建行异步回调(单号："+orderId+")失败：验签失败");
        }

        // 用户信息解密 字段二次解析  注意： USRMSG解密的时候不需要编码成ISO_8859_1， 因为编码工具类中已经有处理了
        try {
            if (StringUtils.isNotBlank(notifyParams.getUsrMsg())) {
                String decodedUsrMsg = ccbClient.decodeUsrMsg(notifyParams.getUsrMsg());
                notifyParams.setDecodedUsrMsg(decodedUsrMsg);
            }
        } catch (Exception e) {
            log.info("建行异步回调(单号："+orderId+")：USRMSG解密失败", e);
        }

        paramsStr = OBJECT_MAPPER.writeValueAsString(notifyParams);

        try {
            String paymentDetails = notifyParams.getPaymentDetails();
            if (StringUtils.isNotBlank(paymentDetails)) {
                NotifyParams.PaymentDetail pd = OBJECT_MAPPER.readValue(paymentDetails, NotifyParams.PaymentDetail.class);
                notifyParams.set_paymentDetail(pd);
            }
        } catch (Exception e) {
            log.info("建行异步回调(单号："+orderId+")：支付详细信息解析失败", e);
//            throw new Exception("建行异步回调：支付详细信息解析失败", e);
        }

        String success = notifyParams.getSuccess();  // 成功标识
        String payment = notifyParams.getPayment();  // 付款金额
        // 建行异步回调的时候没有交易号..

        // 交易状态校验
        if (!CCBSuccessFlagEnum.SUCCESS.getValue().equals(success)) {
            response.setMessage("建行异步回调失败：回调状态为FAIL");
            response.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            response.setCode(RespEnum.ERROR_99.getStatus());
            response.setTradeStatusRes(paramsStr);
            response.setTotalFee(new BigDecimal(payment).doubleValue());
            response.setResBody("fail");
            log.info("CCBPayServiceImpl callbackNotify 建行异步回调(单号："+orderId+")失败：回调状态为FAIL");
            log.info("CCBPayServiceImpl callbackNotify request: {}", OBJECT_MAPPER.writeValueAsString(request));
            return response;
        }

        PayOrderInfo payOrderInfoRequest = new PayOrderInfo();
        payOrderInfoRequest.setPlatType(Integer.valueOf(Constants.CCB_PAY_TYPE));
        payOrderInfoRequest.setOutTradeNo(orderId);
//        PayOrderInfo payOrderInfo = payOrderInfoMapper.selectByPlatTypeAndOutTradeNo(payOrderInfoRequest);
        PayOrderInfo payOrderInfo = null;
        request.setChannel(payOrderInfo != null ? payOrderInfo.getChannel() : null);
        if (null != payOrderInfo && payOrderInfo.getTradeStatus() == TradeStatusEnum.SUCCESS.getStatus()) {
            // 已经通知成功了
            response.setResBody("success");
            log.info("CCBPayServiceImpl callbackNotify 建行异步回调(单号："+orderId+")已经处理，本次不再处理");
            log.info("CCBPayServiceImpl callbackNotify request: {}", OBJECT_MAPPER.writeValueAsString(request));
            return response;
        }

//        String redisKey = notify_key_prefix + orderId;
        // 去重逻辑有问题 缓存存活的时间不能超过三方发起重试间隔的时间
//        if (stringRedisTemplate.hasKey(redisKey)) {
//            response.setResBody("success");
//            response.setCode(RespEnum.SUCCESS.getStatus());
//            response.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
//            response.setTradeStatusRes(paramsStr);
//            response.setTotalFee(new BigDecimal(payment).doubleValue());
////            response.setExt1();
//        }

//        stringRedisTemplate.opsForValue().set(redisKey, "60", 60, TimeUnit.SECONDS);
        response.setResBody("success");
        response.setCode(RespEnum.SUCCESS.getStatus());
        response.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
        response.setTradeStatusRes(paramsStr);
        response.setTotalFee(new BigDecimal(payment).doubleValue());

        String discount = notifyParams.getDiscount();  // 实付金额
        if (StringUtils.isNotBlank(discount)) {
            if (new BigDecimal(payment).compareTo(new BigDecimal(discount)) > 0) {
                // 计算优惠金额
                BigDecimal ext1 = new BigDecimal(payment).subtract(new BigDecimal(discount)).setScale(2, BigDecimal.ROUND_HALF_UP);
                response.setExt1(ext1.toString());
            }
        }

        log.info("CCBPayServiceImpl callbackNotify 建行异步回调(单号："+orderId+")成功");
        log.info("CCBPayServiceImpl callbackNotify response: {}", OBJECT_MAPPER.writeValueAsString(response));
        return response;

    }

//    private String cut(String input) {
//        try {
//            if (StringUtils.isNotBlank(input)) {
//                if (input.getBytes().length > 256) {
//                    // 截取 针对字符串长度进行截取 而不是截取字符个数 否则可能乱码
////                    input =  URLEncoder.encode(StringUtils.defaultString(input.substring(0, 256 / 3)), "UTF-8");
//                    input = input.substring(0, 256 / 3);
//                    return input;
//                }
//            }
//        } catch (Exception e) {
//            log.info("建行支付截取超长参数时异常", e);
//        }
//        return null;
//    }

    /**
     * 正则替换所有特殊字符
     * @param orgStr
     * @return
     */
    private static String replaceSpecStr(String orgStr) {
        if (null!=orgStr && !"".equals(orgStr.trim())) {
            String regEx="[^A-z0-9\\u4e00-\\u9fa5]";
            if (StringUtils.isBlank(regEx)) {
                return orgStr;
            }
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(orgStr);
            return m.replaceAll("");
        }
        return null;
    }

//    private String getTimeoutStr(String createDtmeStr) {
//        try {
//            if (StringUtils.isNotBlank(createDtmeStr)) {
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//                Date createDtme = formatter.parse(createDtmeStr);
//                String orderCloseTimeStr = this.getOrderCloseTime();  // 分钟   即订单在orderCloseTimeStr分钟后关闭
//                if (StringUtils.isNotBlank(orderCloseTimeStr)) {
//                    Integer orderCloseTime = Integer.valueOf(orderCloseTimeStr);
//                    if (orderCloseTime > 0) {
//                        Date addMinuteDate = DateUtil.getAddMinuteDate(createDtme, orderCloseTime);
//                        String timeout = formatter.format(addMinuteDate);
//                        return timeout;    // 订单超时时间 YYYYMMDDHHMMSS
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    /**
//     * 获取订单关闭的超时时间
//     * @return
//     */
//    private String getOrderCloseTime() {
//        try {
//            GeneralConfigurationInfoSearchRequest conRequest = new GeneralConfigurationInfoSearchRequest();
//            conRequest.setEc_key("ec.order.close");
//            conRequest.setTenantNumId(6L);
//            conRequest.setDataSign(0L);
//            GeneralConfigurationInfoResponse configurationInfo = accountManageService.searchGeneralConfigurationInfo(conRequest);
//            if (configurationInfo != null) {
//                List<GeneralConfigurationInfo> infoList = configurationInfo.getInfoList();
//                if (CollectionUtils.isNotEmpty(infoList)) {
//                    GeneralConfigurationInfo generalConfigurationInfo = infoList.get(0);
//                    String ec_value = generalConfigurationInfo.getEc_value();
//                    return ec_value;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }

}
