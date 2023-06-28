package com.github.bluecatlee.dcep.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bluecatlee.ccb.utils.MathUtil;
import com.github.bluecatlee.dcep.DCEPClient;
import com.github.bluecatlee.dcep.constant.DCEPTxStatusEnum;
import com.github.bluecatlee.dcep.constant.DCEPTxTypeEnum;
import com.github.bluecatlee.dcep.constant.ReturnCodeEnum;
import com.github.bluecatlee.dcep.constant.SuccessFlagEnum;
import com.github.bluecatlee.dcep.utils.DateUtils;
import com.github.bluecatlee.dcep.vo.pay.DCEPNotifyParams;
import com.github.bluecatlee.dcep.vo.pay.H5PayRequest;
import com.github.bluecatlee.dcep.vo.request.QueryRequest;
import com.github.bluecatlee.dcep.vo.request.RefundRequest;
import com.github.bluecatlee.dcep.vo.response.DCEPBaseResponse;
import com.github.bluecatlee.dcep.vo.response.QueryResponse;
import com.github.bluecatlee.dcep.vo.response.RefundResponse;
import com.github.bluecatlee.dcep.vo.response.TransactionInfoDetail;
import com.github.bluecatlee.redundance.*;
import com.github.bluecatlee.redundance.bean.DCEPPayRequest;
import com.github.bluecatlee.redundance.bean.DCEPPayResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 建行数币支付
 *      基于支付中台框架的实现 todo 部分业务代码注释掉了 业务代码可以自行剥离
 *      注意异步回调的入口在支付中台 建行数币支付以响应标准的http状态码=200时表示回调成功
 *
 *
 * @see <a href="https://github.com/bluecatlee/SOA/tree/main/gs4d-pay">支付中台</a>
 * @Author Bluecatlee
 * @Date 2021/10/9 15:03
 */
@Service("dcepPayServiceImpl")
@Slf4j
public class DCEPPayServiceImpl implements PaymentService<DCEPPayRequest, DCEPPayResponse> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String spliter = "#";

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Resource
    private DCEPClient dcepClient;

    @Resource
    private Validator validator;

//    @Resource
//    private CommonPayService commonPayService;
//
//    @Resource
//    private PayOrderInfoMapper payOrderInfoMapper;

    @Override
    public void checkInputParams(DCEPPayRequest req, DCEPPayResponse res, String requestMethod) throws Exception {

    }

    @Override
    public BaseResponse pay(DCEPPayRequest req, DCEPPayResponse res) throws Exception {
        H5PayRequest h5PayRequest = new H5PayRequest();
        h5PayRequest.setOrderId(req.getOutTradeNo());
        BigDecimal fee = MathUtil.centToYuan2(Integer.valueOf(req.getTotalFee()));
        h5PayRequest.setPayment(fee.toString());
        h5PayRequest.setRemark1(req.getSubUnitNumId() == null ? "" : req.getSubUnitNumId());    // 备注仅支持数字、英文、少数字符
        h5PayRequest.setRemark2(req.getOrdeDate() == null ? "" : req.getOrdeDate());

/*        String timeoutStr = commonPayService.getTimeoutStr(req.getCreateDtme());
        if (StringUtils.isNotBlank(timeoutStr)) {
            h5PayRequest.setTimeout(timeoutStr);
        }*/

        String channel = req.getChannel();
        if (channel.equals("98")) { // 小程序
        } else if (channel.equals("99")) {  // APP app使用熊猫支付h5版  没有这个字段
            h5PayRequest.setTxFlag(null);
        }

        String prepay = dcepClient.prepay(h5PayRequest, true);

        res.setResBody(prepay);
        res.setOutTradeNo(req.getOutTradeNo());
        res.setTradeStatus(TradeStatusEnum.PROCESSING.getStatus());
        res.setTradeStatusRes(prepay);
        res.setCode(RespEnum.SUCCESS.getStatus());
        res.setTradeType(Byte.valueOf(TradeTypeEnum.PAY.getStatus()));
        res.setTotalFee(fee.doubleValue());
        res.setTxndate(DateUtils.getCurrentDateAsString(DateUtils.COMPACT_DATE_FORMAT_PATTERN));
        res.setTxntime(DateUtils.getCurrentDateTimeAsString("HHmmss"));

        return res;
    }

    @Override
    public Map afterPay(DCEPPayRequest req, String res, long id) throws Exception {
        return null;
    }

    @Override
    public BaseResponse refund(DCEPPayRequest req, DCEPPayResponse res) throws Exception {
        log.info("DCEPPayServiceImpl refund request: {}", OBJECT_MAPPER.writeValueAsString(req));

        String outTradeNo = req.getOutTradeNo();
        if (StringUtils.isBlank(outTradeNo)) {
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes("建行数币退款失败: 建行数币退款必须传退款流水号outTradeNo!");
            log.info("DCEPPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        // 使用相同的退款流水号退款 建行会报错  此处可以不校验
/*        PayOrderInfo condition = new PayOrderInfo().setOutTradeNo(outTradeNo)
                .setPlatType(Integer.valueOf(req.getPlatType()))
                .setTradeType(Byte.valueOf(TradeTypeEnum.REFUND.getStatus()));
        PayOrderInfo payOrderInfo = payOrderInfoMapper.selectPayInfoByOutTradeNo(condition);
        if (payOrderInfo != null *//*&& (TradeStatusEnum.SUCCESS.getStatus().equals(payOrderInfo.getTradeStatus())
                                        || TradeStatusEnum.PROCESSING.getStatus().equals(payOrderInfo.getTradeStatus()))*//*) {
//            throw new BussinessException("建行数币退款失败: 该退款流水号outTradeNo("+outTradeNo+")已经发起过退款！");
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes("建行数币退款失败: 该退款流水号outTradeNo("+outTradeNo+")已经发起过退款！");
            log.info("DCEPPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }*/

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderNo(req.getSrcOutTradeNo());
        refundRequest.setRefundNo(req.getOutTradeNo());


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

        String transactionDateRange = this.getTransactionDateRange(req.getOutTradeNo(), req.getOrdeDate());
        String[] split = transactionDateRange.split(spliter);
        refundRequest.setStartDate(split[0]);
        refundRequest.setEndDate(split[1]);

        DCEPBaseResponse<RefundResponse> refundResponse = dcepClient.refund(refundRequest);

        res.setTradeType(Byte.valueOf(TradeTypeEnum.REFUND.getStatus()));
        res.setTxndate(DateUtils.getCurrentDateAsString(DateUtils.COMPACT_DATE_FORMAT_PATTERN));
        res.setTxntime(DateUtils.getCurrentDateTimeAsString("HHmmss"));
        res.setOutTradeNo(req.getOutTradeNo());
        res.setSrcOutTradeNo(req.getSrcOutTradeNo());

        String returnCode = refundResponse.getReturnCode();
        if (MessagePack.OK != refundResponse.getCode() && !returnCode.equals(ReturnCodeEnum.UNKNOWN_PBOC.getCode()) && !returnCode.equals(ReturnCodeEnum.TIMEOUT.getCode())) {
            res.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(refundResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            log.info("DCEPPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        if (returnCode.equals(ReturnCodeEnum.UNKNOWN_PBOC.getCode()) || returnCode.equals(ReturnCodeEnum.TIMEOUT.getCode())) {
            // 人行状态不确定或超时  再次查询退款交易
            String returnMessage = ReturnCodeEnum.getMessageByCode(returnCode);
            log.info(" =================== 退款结果为: {} 需要再次查询退款交易!", returnMessage);
            this.queryRefundResult(req, res);
            res.setTradeType(Byte.valueOf(TradeTypeEnum.REFUND.getStatus()));
            log.info("DCEPPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        res.setTotalFee(new BigDecimal(refundResponse.getDataInfo().getAmount()).doubleValue());
        res.setCode(RespEnum.SUCCESS.getStatus());
        res.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
        res.setTradeStatusRes(JSON.toJSONString(refundResponse));

        log.info("DCEPPayServiceImpl refund response: {}", OBJECT_MAPPER.writeValueAsString(res));
        return res;
    }

    @Override
    public Map afterRefund(DCEPPayRequest req, String res, long id) throws Exception {
        return null;
    }

    @Override
    public BaseResponse queryPayResult(DCEPPayRequest req, DCEPPayResponse res) throws Exception {
        log.info("DCEPPayServiceImpl queryPayResult request: {}", OBJECT_MAPPER.writeValueAsString(req));

        String outTradeNo = req.getOutTradeNo();
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setTxnFcnNo("05");  // 根据商户号+订单号查询
        queryRequest.setOrderNo(outTradeNo);

        String transactionDateRange = this.getTransactionDateRange(outTradeNo, req.getOrdeDate());
        String[] split = transactionDateRange.split(spliter);
        queryRequest.setStartDate(split[0]);
        queryRequest.setEndDate(split[1]);

        DCEPBaseResponse<QueryResponse> payQueryResponse = dcepClient.queryByPage(queryRequest);

        res.setTradeType(Byte.valueOf(TradeTypeEnum.QUERY.getStatus()));
        if (MessagePack.OK != payQueryResponse.getCode()) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(payQueryResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            log.info("DCEPPayServiceImpl queryPayResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        List<TransactionInfoDetail> list = payQueryResponse.getDataInfo().getList(); // 注意 如果发生了退款 查询的时候会返回多条(包含退款交易信息)
        List<TransactionInfoDetail> filteredList = CollectionUtils.isEmpty(list) ? null : list.stream().filter(txInfo -> txInfo.getTrdTpEcd().equals(DCEPTxTypeEnum.PAY.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filteredList)) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(payQueryResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setMessage("支付查询异常！返回交易信息中没有支付类型的数据!");
            log.info("DCEPPayServiceImpl queryPayResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }
        TransactionInfoDetail transactionInfoDetail = filteredList.get(0);
        log.info("DCEPPayServiceImpl queryPayResult 筛选后的支付交易明细: {}", OBJECT_MAPPER.writeValueAsString(transactionInfoDetail));
        String sysTxStatus = transactionInfoDetail.getSysTxStatus();
        if (DCEPTxStatusEnum.SUCCESS.getStatus().equals(sysTxStatus)) {
            res.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(payQueryResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
//            res.setTransactionId(transactionInfoDetail.getOrigTxnSrlNo()); // 银行流水号这个字段不是必返的..
//            res.setTransactionId(payQueryResponse.getSysEvtTraceId());    // SYS_EVT_TRACE_ID是请求流水号 每次请求都不同
            res.setTransactionId(outTradeNo);
            res.setTotalFee(new BigDecimal(transactionInfoDetail.getTxnAmt()).doubleValue());
            if (StringUtils.isNotBlank(transactionInfoDetail.getActADbAmt())) {
                if (new BigDecimal(transactionInfoDetail.getTxnAmt()).compareTo(new BigDecimal(transactionInfoDetail.getActADbAmt())) > 0) {
                    // 计算优惠金额
                    BigDecimal ext1 = new BigDecimal(transactionInfoDetail.getTxnAmt()).subtract(new BigDecimal(transactionInfoDetail.getActADbAmt())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    res.setExt1(ext1.toString());
                }
            }
        } else if (DCEPTxStatusEnum.UNKNOWN.getStatus().equals(sysTxStatus)) {
            res.setTradeStatus(TradeStatusEnum.PROCESSING.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(payQueryResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        } else {
            res.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(payQueryResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        }

        log.info("DCEPPayServiceImpl queryPayResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
        return res;
    }

    @Override
    public BaseResponse queryRefundResult(DCEPPayRequest req, DCEPPayResponse res) throws Exception {
        log.info("DCEPPayServiceImpl queryRefundResult request: {}", OBJECT_MAPPER.writeValueAsString(req));

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setTxnFcnNo("08");  // 根据商户号+退款流水号查询
        queryRequest.setOrderNo(req.getSrcOutTradeNo());
        queryRequest.setRefundNo(req.getOutTradeNo());

        String transactionDateRange = this.getTransactionDateRange(req.getSrcOutTradeNo(), req.getOrdeDate());
        String[] split = transactionDateRange.split(spliter);
        queryRequest.setStartDate(split[0]);
        queryRequest.setEndDate(split[1]);

        DCEPBaseResponse<QueryResponse> refundQueryResponse = dcepClient.queryByPage(queryRequest);

        res.setTradeType(Byte.valueOf(TradeTypeEnum.QUERY.getStatus()));
        if (MessagePack.OK != refundQueryResponse.getCode()) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(refundQueryResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            log.info("DCEPPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }

        List<TransactionInfoDetail> list = refundQueryResponse.getDataInfo().getList();
        if (CollectionUtils.isEmpty(list)) {
            res.setTradeStatus(TradeStatusEnum.ERROR.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(refundQueryResponse));
            res.setCode(RespEnum.ERROR_MINUS_100.getStatus());
            res.setMessage(String.format("建行数币退款查询失败: 未查询到退款流水! 原单号：%s，退款流水号：%s", req.getSrcOutTradeNo(), req.getOutTradeNo()));
            log.info("DCEPPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
            return res;
        }
        TransactionInfoDetail transactionInfoDetail = list.get(0);
        String sysTxStatus = transactionInfoDetail.getSysTxStatus();

        if (DCEPTxStatusEnum.SUCCESS.getStatus().equals(sysTxStatus)) {
            // 退款成功
            res.setTransactionId(req.getOutTradeNo());
            res.setTotalFee(new BigDecimal(transactionInfoDetail.getTxnAmt()).doubleValue());
            res.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(refundQueryResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        } else if (DCEPTxStatusEnum.UNKNOWN.getStatus().equals(sysTxStatus)) {
            res.setTradeStatus(TradeStatusEnum.PROCESSING.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(refundQueryResponse));
            res.setCode(RespEnum.SUCCESS.getStatus());
        } else {
            res.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            res.setTradeStatusRes(JSON.toJSONString(refundQueryResponse));
            res.setCode(RespEnum.FAIL.getStatus());
        }

        log.info("DCEPPayServiceImpl queryRefundResult response: {}", OBJECT_MAPPER.writeValueAsString(res));
        return res;
    }

    @Override
    public BaseResponse callbackNotify(BaseRequest request, BaseResponse response) throws Exception {
        log.info("DCEPPayServiceImpl callbackNotify request: {}", OBJECT_MAPPER.writeValueAsString(request));

        // 解析参数
        Map<String, String> flatParams = new HashMap<>();
        String body = request.getBody();
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
                            flatParams.put(key, value);
                        }
                    }
                }
            }
        }

        String requestStr = OBJECT_MAPPER.writeValueAsString(request);
        String paramsStr = OBJECT_MAPPER.writeValueAsString(flatParams);
        log.info("建行数币支付异步回调：request = {}, params:{}", requestStr, paramsStr);

        DCEPNotifyParams notifyParams = OBJECT_MAPPER.readValue(paramsStr, DCEPNotifyParams.class);
        if (notifyParams == null) {
            throw new Exception("建行数币支付异步回调失败：通知参数解析异常!");
        }

        String orderId = notifyParams.getOrderId();  // 传给建行的单号 即outTradeNo
        request.setOutTradeNo(orderId);
        response.setOutTradeNo(orderId);

        // 参数校验
        Set<ConstraintViolation<Object>> validateResults = validator.validate(notifyParams, Default.class);
        if (CollectionUtils.isNotEmpty(validateResults)) {
            for (ConstraintViolation<Object> result : validateResults) {
                String message = result.getMessage();
                throw new Exception("建行数币支付异步回调(单号："+orderId+")失败：" + message);
            }
        }

        if (notifyParams.getAccType() == null) {
            // 没有ACC_TYPE参数说明是页面回调 不处理
            throw new Exception("建行数币支付异步回调(单号："+orderId+")：本次回调不是服务器回调，是页面回调！请检查页面回调的地址配置");
        }

        // 验签
        boolean signResult = dcepClient.verifySign(notifyParams);
        if (!signResult) {
//            throw new Exception("建行数币支付异步回调(单号："+orderId+")失败：验签失败");
            log.info("建行数币支付异步回调(单号："+orderId+")失败：验签失败");
        }

        String success = notifyParams.getSuccess();  // 成功标识
        String payment = notifyParams.getPayment();  // 付款金额
        // 建行数币支付异步回调的时候没有交易号..

        // 交易状态校验
        if (!SuccessFlagEnum.SUCCESS.getValue().equals(success)) {
            response.setMessage("建行数币支付异步回调失败：回调状态为FAIL");
            response.setTradeStatus(TradeStatusEnum.FAIL.getStatus());
            response.setCode(RespEnum.ERROR_99.getStatus());
            response.setTradeStatusRes(paramsStr);
            response.setTotalFee(new BigDecimal(payment).doubleValue());
            response.setResBody("fail");
            log.info("DCEPPayServiceImpl callbackNotify 建行数币支付异步回调(单号："+orderId+")失败：回调状态为FAIL");
            log.info("DCEPPayServiceImpl callbackNotify request: {}", OBJECT_MAPPER.writeValueAsString(request));
            return response;
        }

        PayOrderInfo payOrderInfoRequest = new PayOrderInfo();
        payOrderInfoRequest.setPlatType(Integer.valueOf(Constants.DCEP_PAY_TYPE));
        payOrderInfoRequest.setOutTradeNo(orderId);
//        PayOrderInfo payOrderInfo = payOrderInfoMapper.selectByPlatTypeAndOutTradeNo(payOrderInfoRequest);
        PayOrderInfo payOrderInfo = null;
        request.setChannel(payOrderInfo != null ? payOrderInfo.getChannel() : null);
        if (null != payOrderInfo && payOrderInfo.getTradeStatus() == TradeStatusEnum.SUCCESS.getStatus()) {
            // 已经通知成功了
            response.setResBody("success");
            log.info("DCEPPayServiceImpl callbackNotify 建行数币支付异步回调(单号："+orderId+")已经处理，本次不再处理");
            log.info("DCEPPayServiceImpl callbackNotify request: {}", OBJECT_MAPPER.writeValueAsString(request));
            return response;
        }

        response.setResBody("success");
        response.setCode(RespEnum.SUCCESS.getStatus());
        response.setTradeStatus(TradeStatusEnum.SUCCESS.getStatus());
        response.setTradeStatusRes(paramsStr);
        response.setTotalFee(new BigDecimal(payment).doubleValue());

        // 建行数币支付回调参数中没有实付/优惠字段 需要发起交易流水查询请求来获取
        try {
            DCEPPayRequest queryReq = new DCEPPayRequest();
            DCEPPayResponse queryResp = new DCEPPayResponse();
            queryReq.setOutTradeNo(orderId);
            log.error(" ==========  DCEPPayServiceImpl callbackNotify 建行数币支付异步回调(单号："+orderId+") 再次查询交易信息开始!! ==========");
            this.queryPayResult(queryReq, queryResp);
            log.error(" ==========  DCEPPayServiceImpl callbackNotify 建行数币支付异步回调(单号："+orderId+") 再次查询交易信息结束!! ==========");
            if (StringUtils.isNotBlank(queryResp.getExt1())) {
                response.setExt1(queryResp.getExt1());
            }
        } catch (Exception e) {
            log.error("DCEPPayServiceImpl callbackNotify 建行数币支付异步回调(单号："+orderId+") 再次查询交易信息失败!!", e);
        }

        log.info("DCEPPayServiceImpl callbackNotify 建行数币支付异步回调(单号："+orderId+")成功");
        log.info("DCEPPayServiceImpl callbackNotify response: {}", OBJECT_MAPPER.writeValueAsString(response));
        return response;

    }

    /**
     * 获取交易的时间范围
     *      线上订单有超时自动关闭机制(默认20分钟) 所以支付日期与下单日期最多差一天
     * @param outTradeNo
     * @param orderDate
     * @return
     */
    private String getTransactionDateRange(String outTradeNo, String orderDate) {
        String startDateStr = "";
        String endDateStr = "";

        PayOrderInfo payOrderInfoRequest = new PayOrderInfo();
        payOrderInfoRequest.setPlatType(Integer.valueOf(Constants.DCEP_PAY_TYPE));
        payOrderInfoRequest.setOutTradeNo(outTradeNo);
//        PayOrderInfo payOrderInfo = payOrderInfoMapper.selectByPlatTypeAndOutTradeNo(payOrderInfoRequest);
        PayOrderInfo payOrderInfo = null;
        if (payOrderInfo != null) {
            startDateStr = payOrderInfo.getTxndate();
        } else {
            if (StringUtils.isNotBlank(orderDate)) {
                Date date = DateUtils.parse(orderDate, DateUtils.DATE_FORMAT_PATTERN);
                String format = DateUtils.format(date, DateUtils.COMPACT_DATE_FORMAT_PATTERN);
                startDateStr = format;
            }
        }
        if (StringUtils.isBlank(startDateStr)) {
            startDateStr = DateUtils.getCurrentDateAsString(DateUtils.COMPACT_DATE_FORMAT_PATTERN);
        }

        // 开始时间和结束时间的差值不能超过3天
        Date startDate = DateUtils.parse(startDateStr, DateUtils.COMPACT_DATE_FORMAT_PATTERN);
        Date endDate = DateUtils.addDays(startDate, 3);
        if (endDate.compareTo(new Date()) > 0) {
            endDateStr = DateUtils.getCurrentDateAsString(DateUtils.COMPACT_DATE_FORMAT_PATTERN);
        } else {
            endDateStr = DateUtils.format(endDate, DateUtils.COMPACT_DATE_FORMAT_PATTERN);
        }

        return startDateStr + spliter + endDateStr;

    }

}
