package com.github.bluecatlee.dcep;

import CCBSign.RSASig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bluecatlee.ccb.utils.Escape;
import com.github.bluecatlee.ccb.utils.MD5Utils;
import com.github.bluecatlee.dcep.annotation.DCEPField;
import com.github.bluecatlee.dcep.api.DCEPApi;
import com.github.bluecatlee.dcep.constant.ReturnCodeEnum;
import com.github.bluecatlee.dcep.utils.AESUtil;
import com.github.bluecatlee.dcep.utils.DecipherUtil_Ft;
import com.github.bluecatlee.dcep.utils.EncipherUtil_Ft;
import com.github.bluecatlee.dcep.vo.DCEPRequest;
import com.github.bluecatlee.dcep.vo.authorize.AuthorizeResult;
import com.github.bluecatlee.dcep.vo.pay.DCEPBasePayRequest;
import com.github.bluecatlee.dcep.vo.pay.DCEPNotifyParams;
import com.github.bluecatlee.dcep.vo.pay.H5PayRequest;
import com.github.bluecatlee.dcep.vo.request.AddressingRequest;
import com.github.bluecatlee.dcep.vo.request.DCEPBaseRequest;
import com.github.bluecatlee.dcep.vo.request.QueryRequest;
import com.github.bluecatlee.dcep.vo.request.RefundRequest;
import com.github.bluecatlee.dcep.vo.response.*;
import com.github.bluecatlee.redundance.IdGenerator;
import com.github.bluecatlee.redundance.JSONUtil;
import com.github.bluecatlee.redundance.MessagePack;
import com.google.common.base.Enums;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.net.ssl.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  建行数币支付调用
 *
 * @Author Bluecatlee
 * @Date 2021/10/08 17:12
 */
@Data
@Slf4j
@Component
public class DCEPClient {

    private static final String TAG = "[DCEPClient] ";
    private static final String cacheKey = "dcep_authorize_addressing";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);  // 允许单引号键和值
    }

    @Value("${dcep.url}")
    private String apiUrl;  // 下单url
    @Value("${dcep.publicKey}")
    private String dcepPublicKey;   // 数币业务商户公钥(支付回调验签用)

    @Value("${dcep.custId}")
    private String dcepCustId;  // 数币业务商户号
    @Value("${dcep.posId}")
    private String dcepPosId;   // 数币业务商户柜台代码
    @Value("${dcep.branchId}")
    private String dcepBranchId;   // 数币业务分行代码
    @Value("${dcep.userId}")
    private String dcepUserId;   // 数币业务操作员
    @Value("${dcep.password}")
    private String dcepPassword;   // 数币业务交易密码

    @Value("${dcep.ftCorpId}")
    private String ftCorpId;        // 调用方编号
    @Value("${dcep.ftTileId}")
    private String ftTileId;        // 瓦片编号
    @Value("${dcep.ftScenario}")
    private String ftScenario;      // 使用场景

    @Value("${dcep.auth.url}")
    private String authUrl;   // 授权请求url
    @Value("${dcep.auth.priKey1}")
    private String authPriKey1;   // 授权参数签名私钥
    @Value("${dcep.auth.pubKey1}")
    private String authPubKey1;   // 授权参数加密公钥
    @Value("${dcep.auth.pubKey2}")
    private String authPubKey2;   // 授权结果解密公钥

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Getter
    private DCEPApi dcepApi;

    @PostConstruct
    public void generateApi() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        this.skipSslVerify(builder);
        OkHttpClient okHttpClient = builder.build();

        dcepApi = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(authUrl)   // 注意baseUrl必须设置 且必须以/结尾   当传入的@Url包含完整的schema和host时会覆盖baseUrl
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(DCEPApi.class);
    }

    private void skipSslVerify(OkHttpClient.Builder builder) {

        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        builder.sslSocketFactory(sslContext.getSocketFactory(), xtm)
                .hostnameVerifier(DO_NOT_VERIFY);

    }

    /**
     * 生成下单url
     * @param request
     * @return
     */
    public String prepay(DCEPBasePayRequest request) {
        return this.prepay(request, false);
    }

    /**
     * 生成下单url
     * @param request
     * @param addHostPath   是否添加host path 生成完整url
     * @return
     */
    public String prepay(DCEPBasePayRequest request, boolean addHostPath) {
        if (request instanceof H5PayRequest) {
            H5PayRequest h5PayRequest = (H5PayRequest)request;  // 下单通用参数可以抽取到DCEPBasePayRequest 暂时只有h5支付
            h5PayRequest.setMerchantId(dcepCustId);
            h5PayRequest.setBranchId(dcepBranchId);
            h5PayRequest.setPosId(dcepPosId);
            h5PayRequest.setPub(dcepPublicKey.substring(dcepPublicKey.length() - 30));

            h5PayRequest.setMac(this.calMac(h5PayRequest));
            h5PayRequest.setPub(null);

            String s = this.assembleParams(h5PayRequest);
            if (addHostPath) {
                s = apiUrl + "?" + s;
            }
            return s;
        }
        throw new RuntimeException("生成下单url失败: 参数类型不正确!");
    }

    /**
     * 组装参数
     * @param request
     * @return
     */
    private String assembleParams(DCEPRequest request) {
        Field[] fields;
        StringBuilder sb = new StringBuilder();
        if (request instanceof H5PayRequest) {
            fields = H5PayRequest.class.getDeclaredFields();
//        } else if (request instanceof DCEPBaseRequest) {
//            Field[] parentFields = DCEPBaseRequest.class.getDeclaredFields();
//            Field[] declaredFields = request.getClass().getDeclaredFields();
//            fields = new Field[parentFields.length + declaredFields.length];
//            System.arraycopy(parentFields, 0, fields, 0, parentFields.length);
//            System.arraycopy(declaredFields, 0, fields, parentFields.length, declaredFields.length);
        } else {
            throw new RuntimeException("组装参数异常: 参数类型不正确!");
        }

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldValue = null;
            try {
                fieldValue = (String)field.get(request);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            DCEPField annotation = field.getAnnotation(DCEPField.class);
            if ((annotation == null || !annotation.required()) && fieldValue == null) {
                // 非必填字段且字段值为null时 不传
                continue;
            }
            if (annotation != null) {
                if (annotation.required() && fieldValue == null) {
                    throw new RuntimeException("参数[" + fieldName + "]值不能为空");
                }
                fieldName = this.getKeyName(field);
                if (StringUtils.isNotBlank(fieldValue) && annotation.needEscape()) {
                    fieldValue = Escape.escape(fieldValue);
                }
            }

            sb.append(fieldName).append("=").append(fieldValue).append("&");
        }

        String payParams = sb.substring(0, sb.length() - 1);
        return payParams;

    }

    /**
     * mac计算
     * @param request
     * @return
     */
    private String calMac(DCEPBasePayRequest request) {
        String macParams = this.genOrderedKVParams(request);
        String mac = MD5Utils.getMD5Code(macParams);
        return mac;
    }

    /**
     * 生成有序的k=v参数字符串
     * @param request
     * @return
     */
    private String genOrderedKVParams(DCEPRequest request) {
        Field[] fields;
        if (request instanceof H5PayRequest) {
            fields = H5PayRequest.class.getDeclaredFields();
        } else if (request instanceof DCEPBaseRequest) {
            Field[] parentFields = DCEPBaseRequest.class.getDeclaredFields();
            Field[] declaredFields = request.getClass().getDeclaredFields();
            fields = new Field[parentFields.length + declaredFields.length];
            System.arraycopy(parentFields, 0, fields, 0, parentFields.length);
            System.arraycopy(declaredFields, 0, fields, parentFields.length, declaredFields.length);
        } else if (request instanceof DCEPNotifyParams) {
            fields = DCEPNotifyParams.class.getDeclaredFields();
        } else {
            throw new RuntimeException("组装参数异常: 参数类型不正确!");
        }

        Map[] params = new Map[fields.length];

        for (int i = 0; i < fields.length; ++i) {
            final Field field = fields[i];
            field.setAccessible(true);
            String fieldValue = null;
            try {
                fieldValue = (String) field.get(request);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("计算mac失败: 反射获取字段值异常!", e);
            }

            DCEPField annotation = field.getAnnotation(DCEPField.class);
            // order小于等于0时不参与排序
            if ((annotation == null || annotation.order() <= 0)) {
                continue;
            }
            // 如果设置了orderNonNull为true 且字段值为空时则不参与排序
            if (annotation.orderNonNull() && fieldValue == null) {
                continue;
            }

            String keyName = this.getKeyName(field);
            Map<Object, Object> map = new HashMap<>(1);
            map.put(keyName, fieldValue);
            params[annotation.order()-1] = map;              // 注意 order值不能重复 否则会被覆盖

        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            Map map = params[i];
            if (map == null) {
                continue;
            }
            String keyName = (String)map.keySet().iterator().next();
            sb.append(keyName).append("=").append(map.get(keyName)).append("&");
        }

        String paramsStr = sb.substring(0, sb.length() - 1);
        return paramsStr;

    }

    /**
     * 获取key名称
     * @param field
     * @return
     */
    private String getKeyName(Field field) {
        field.setAccessible(true);
        DCEPField annotation = field.getAnnotation(DCEPField.class);
        String fieldName = field.getName();
        if (StringUtils.isNotBlank(annotation.name())) {    // 如果name属性有值 优先取name属性的值
            fieldName = annotation.name();
        } else {
            if (annotation.strategy() == DCEPField.Strategy.NONE) {
            } else if (annotation.strategy() == DCEPField.Strategy.UPPER) {
                fieldName = fieldName.toUpperCase();
            } else if (annotation.strategy() == DCEPField.Strategy.LOWER) {
                fieldName = fieldName.toLowerCase();
            } else if (annotation.strategy() == DCEPField.Strategy.CUSTOM) {
                if (StringUtils.isBlank(annotation.name())) {
                    throw new RuntimeException("CUSTOM策略(自定义字段名)必须指定name属性");
                }
                fieldName = annotation.name();
            }
        }
        return fieldName;
    }

//    /**
//     * mac计算
//     * @param request
//     * @return
//     * @throws Exception
//     */
//    private String calMac(DCEPBasePayRequest request) throws Exception {
//        if (request instanceof H5PayRequest) {
//            H5PayRequest h5PayRequest = (H5PayRequest)request;
//            Map<String, Field> map = new HashMap<>();
//            Field[] declaredFields = H5PayRequest.class.getDeclaredFields();
//            for (Field field : declaredFields) {
//                field.setAccessible(true);
//                String fieldValue = (String) field.get(request);
//
//                DCEPField annotation = field.getAnnotation(DCEPField.class);
//                // order小于0时不参与mac计算
//                if ((annotation == null || annotation.order() < 0)) {
//                    continue;
//                }
//                // 如果设置了orderNonNull为true 且字段值为空时则不参与mac计算
//                if (annotation.orderNonNull() && fieldValue == null) {
//                    continue;
//                }
//                map.put(String.valueOf(annotation.order()), field);  // 注意 order值不能重复 否则会被覆盖
//            }
//
//            // 根据order升序排序
//            LinkedHashMap<String, Field> orderedMap = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//
//            StringBuilder sb = new StringBuilder();
//            orderedMap.forEach((k, v) -> {
//                String fieldName = this.getKeyName(v);
//                String fieldValue = null;
//                try {
//                    fieldValue = (String)v.get(request);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//                sb.append(fieldName).append("=").append(fieldValue).append("&");
//            });
//            String macParams = sb.substring(0, sb.length() - 1);
//            String mac = MD5Utils.getMD5Code(macParams);
//
//            return mac;
//
//        }
//        throw new RuntimeException("计算mac失败: 参数类型不正确!");
//    }

    /**
     * 授权+寻址    （不需要每次都调用 建议一个月调一次 因此将结果缓存 有效期1个月）
     * @return
     * @throws Exception
     */
    public AuthorizeAndAddressingResult authorizeAndAddressing() throws Exception {
        return this.authorizeAndAddressing(true);
    }

    /**
     * 授权+寻址
     * @param useCache 是否走缓存
     * @return
     * @throws Exception
     */
    public AuthorizeAndAddressingResult authorizeAndAddressing(boolean useCache) throws Exception {
        String value = stringRedisTemplate.opsForValue().get(cacheKey);
        if (useCache && StringUtils.isNotBlank(value)) {
            AuthorizeAndAddressingResult authorizeAndAddressingResult = OBJECT_MAPPER.readValue(value, AuthorizeAndAddressingResult.class);
            log.info(TAG + "从缓存中获取授权+寻址结果：" + OBJECT_MAPPER.writeValueAsString(authorizeAndAddressingResult));
            return authorizeAndAddressingResult;
        }
        log.info(TAG + "============= 授权+寻址 开始 ==============");
        AuthorizeResult authorizeResult = this.doAuthorize();
        String reqUrl = this.doAddressing(authorizeResult.getAccEntry(), authorizeResult.getCommunicationKey(), authorizeResult.getAccessToken());
        AuthorizeAndAddressingResult authorizeAndAddressingResult = new AuthorizeAndAddressingResult();
        BeanUtils.copyProperties(authorizeResult, authorizeAndAddressingResult);
        authorizeAndAddressingResult.setReqUrl(reqUrl);

        value = OBJECT_MAPPER.writeValueAsString(authorizeAndAddressingResult);
        stringRedisTemplate.opsForValue().set(cacheKey, value, 30, TimeUnit.DAYS);

        log.info(TAG + "============= 授权+寻址 结束 ==============");
        log.info(TAG + "重新获取授权+寻址结果：" + OBJECT_MAPPER.writeValueAsString(authorizeAndAddressingResult));
        return authorizeAndAddressingResult;
    }

    /**
     * 授权
     * @return
     */
    private AuthorizeResult doAuthorize() {
        String ori = "FT_CORPID=" + ftCorpId + "&FT_TILEID=" + ftTileId + "&FT_SCENARIO=" + ftScenario;
        String ccbParam = "";
        try {
            ccbParam = EncipherUtil_Ft.encipherWithRSASignandAES(ori, authPubKey1, authPriKey1);
        } catch (Exception e) {
            throw new RuntimeException("授权失败: 加密失败", e);
        }

        try {
            Call<ResponseBody> call = dcepApi.authorize("CCBIS/ReqCorpAccAuth", ftCorpId, ccbParam);
            Response<ResponseBody> response = call.execute();

            String result;
            if (response.isSuccessful()) {
                result = response.body().string();
                log.info(TAG + "授权--请求参数: {},  返回参数: {}", ori, result);
                AuthorizeResult authorizeResult = OBJECT_MAPPER.readValue(result, AuthorizeResult.class);
                boolean succeed = authorizeResult.isSucceed();
                if (succeed) {
                    String communicationKeyJson = DecipherUtil_Ft.decipherWithRSASignandAES(authorizeResult.getCommunicationPwd(), authPubKey2);    // 解密结果格式: {'communicationKey':'xxx'}
                    JsonNode jsonNode = OBJECT_MAPPER.readTree(communicationKeyJson);
                    String communicationKey = jsonNode.get("communicationKey").asText();
                    authorizeResult.setCommunicationKey(communicationKey);
                    return authorizeResult;
                } else {
                    throw new RuntimeException(String.format("授权失败: 错误码ERROR_CODE[%s], 错误信息ERROR_MSG[%s]", authorizeResult.getErrorCode(), authorizeResult.getErrorMsg()));
                }
            } else {
                String errorResult = response.errorBody().string();
                throw new RuntimeException("授权失败: 请求授权接口异常! " + errorResult);
            }

        } catch (Exception e) {
            throw new RuntimeException("授权失败!", e);
        }

    }

    /**
     * 寻址
     * @param accEntry
     * @param communicationKey
     * @param accessToken
     * @return
     */
    private String doAddressing(String accEntry, String communicationKey, String accessToken) {
        AddressingRequest request = new AddressingRequest();
        request.setFtCorpId(ftCorpId);
        request.setFtTileId(ftTileId);
        request.setFtScenario(ftScenario);
        request.setFtOrderNo(String.valueOf(IdGenerator.nextId()));
        request.setCustomerId(dcepCustId);
        request.setUserId(dcepUserId);
        request.setPassword(dcepPassword);
        request.setTxCode(request.txCode());

        String encryptParams = this.encrypt(request, communicationKey);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("FT_CORPID", ftCorpId);
        paramsMap.put("ccbParam", encryptParams);
        paramsMap.put("ACCESS_TOKEN", accessToken);

//        accEntry = this.addSchemeIfNotPresent(accEntry);

        try {
            Call<ResponseBody> call = dcepApi.execute(accEntry, paramsMap);
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                String decryptResult = AESUtil.decrypt(result, communicationKey);
                log.info(TAG + "寻址--请求参数: {},  返回参数: {}, 【解密结果】:{}", JSONUtil.bean2json(request), result, decryptResult);
                TypeReference<DCEPBaseResponse<AddressingResponse>> typeReference = new TypeReference<DCEPBaseResponse<AddressingResponse>>() {};
                DCEPBaseResponse<AddressingResponse> addressingResponse = OBJECT_MAPPER.readValue(decryptResult, typeReference);

//                this.enclosureCodeMessage(addressingResponse, decryptResult);
                if (ReturnCodeEnum.SUCCESS.getCode().equals(addressingResponse.getReturnCode())) {
                    if (addressingResponse.getDataInfo() == null || StringUtils.isBlank(addressingResponse.getDataInfo().getReqUrl())) {
                        throw new RuntimeException("寻址失败: 返回REQ_URL数据为空!");
                    }
                    String reqUrl = addressingResponse.getDataInfo().getReqUrl();
                    return reqUrl;
                } else {
                    String errorMessage = addressingResponse.getReturnMsg();
                    if (StringUtils.isBlank(errorMessage)) {
                        ReturnCodeEnum returnCodeEnum = Enums.getIfPresent(ReturnCodeEnum.class, addressingResponse.getReturnCode()).orNull();
                        if (returnCodeEnum != null) {
                            errorMessage = returnCodeEnum.getMsg();
                        }
                    }
                    throw new RuntimeException(String.format("寻址失败: 错误码RETURN_CODE[%s], 错误信息RETURN_MSG[%s]", addressingResponse.getReturnCode(), errorMessage));
                }
            } else {
                String result = response.errorBody().string();
                throw new RuntimeException(String.format("寻址失败: 请求授权接口异常! 响应结果: %s", result));
            }
        } catch (IOException e) {
            throw new RuntimeException("寻址失败!", e);
        }
    }

    /**
     * 参数加密
     * @param request
     * @return
     */
    private String encrypt(DCEPBaseRequest request, String communicationKey) {
        String params = this.genOrderedKVParams(request);
        String result = AESUtil.encrypt(params, communicationKey);
        log.info(TAG + "请求参数: {},  参数拼接结果: {}, communicationKey: {}, 加密结果: {}", JSONUtil.bean2json(request), params, communicationKey, result);
        return result;
    }

    /**
     * 退款交易
     * @param request
     * @return
     */
    public DCEPBaseResponse<RefundResponse> refund(RefundRequest request) {
        return this.execute(request, RefundResponse.class);
    }

    /**
     * 分页查询流水
     * @param request
     * @return
     */
    public DCEPBaseResponse<QueryResponse> queryByPage(QueryRequest request) {
        return this.execute(request, QueryResponse.class);
    }

    /**
     * 查询全部流水
     *      实测建行一页20条数据 对于指定单号查交易流水 几乎不会超过一页数据 查一页够了
     * @param request
     * @return
     */
    public DCEPBaseResponse<QueryResponse> queryAll(QueryRequest request) {
        int queryPage = 1;
        request.setPage(String.valueOf(queryPage));
        DCEPBaseResponse<QueryResponse> queryResponse = this.queryByPage(request);
        if (MessagePack.OK != queryResponse.getCode() || queryResponse.getDataInfo() == null
                || StringUtils.isBlank(queryResponse.getDataInfo().getPageCount()) || StringUtils.isBlank(queryResponse.getDataInfo().getCurPage())) {
            return queryResponse;
        }

        QueryResponse dataInfo = queryResponse.getDataInfo();
        String pageCount = dataInfo.getPageCount();
        String curPage = dataInfo.getCurPage();
        int page = Integer.valueOf(pageCount);
        int currentPage = Integer.valueOf(curPage);
        List<TransactionInfoDetail> list = dataInfo.getList();

        List<TransactionInfoDetail> transactionInfoDetails = new ArrayList<>();
        transactionInfoDetails.addAll(list);

        if (page > 10) {
            throw new RuntimeException("数据页数超过10页 请分页查询 不要查询全部数据!");
        }

        // 如果存在多页数据 继续查询下一页
        if (page > 1) {
            while (currentPage < page) {
                queryPage++;
                request.setPage(String.valueOf(queryPage));
                DCEPBaseResponse<QueryResponse> response = this.queryByPage(request);
                if (MessagePack.OK != response.getCode() || response.getDataInfo() == null
                        || StringUtils.isBlank(response.getDataInfo().getPageCount()) || StringUtils.isBlank(response.getDataInfo().getCurPage())) {
                    break;
                }
                List<TransactionInfoDetail> curList = response.getDataInfo().getList();
                transactionInfoDetails.addAll(curList);

                currentPage++;
            }
        }

        queryResponse.getDataInfo().setList(transactionInfoDetails);
        return queryResponse;
    }

    /**
     * 通用交易
     * @param request
     * @param respClazz
     * @param <T>
     * @return
     */
    public <T extends DataInfo> DCEPBaseResponse<T> execute(DCEPBaseRequest request, Class<T> respClazz) {
        request.setFtCorpId(ftCorpId);
        request.setFtTileId(ftTileId);
        request.setFtScenario(ftScenario);
        request.setFtOrderNo(String.valueOf(IdGenerator.nextId()));
        request.setCustomerId(dcepCustId);
        request.setUserId(dcepUserId);
        request.setPassword(dcepPassword);
        request.setTxCode(request.txCode());

        String requestParamsStr = JSONUtil.bean2json(request);

        AuthorizeAndAddressingResult authorizeAndAddressingResult;
        try {
            authorizeAndAddressingResult = this.authorizeAndAddressing();
        } catch (Exception e) {
            throw new RuntimeException("授权+寻址失败！", e);
        }
        String url = authorizeAndAddressingResult.getReqUrl();
        String communicationKey = authorizeAndAddressingResult.getCommunicationKey();
        String accessToken = authorizeAndAddressingResult.getAccessToken();
        String encryptParams = this.encrypt(request, communicationKey);

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("FT_CORPID", ftCorpId);
        paramsMap.put("ccbParam", encryptParams);
        paramsMap.put("ACCESS_TOKEN", accessToken);
        try {
            log.info(TAG + "请求参数: {}, 最终参数: {}", requestParamsStr, OBJECT_MAPPER.writeValueAsString(paramsMap));
        } catch (JsonProcessingException e) {
//            e.printStackTrace();
        }

//        url = this.addSchemeIfNotPresent(url);

        String result;
        DCEPBaseResponse<T> dcepBaseResponse = null;
        try {
            Call<ResponseBody> call = dcepApi.execute(url, paramsMap);
            Response<ResponseBody> response = call.execute();

            try {
                okhttp3.Response rawResponse = response.raw();
                String actualReqUrl = rawResponse.request().url().toString();
                log.info(TAG + "实际请求url：" + actualReqUrl);
            } catch (Exception e) {}

            if (response.isSuccessful()) {
                result = response.body().string();
                String decryptResult = AESUtil.decrypt(result, communicationKey);
                log.info(TAG + "请求参数: {},  返回参数: {}, 【解密结果】:{}", requestParamsStr, result, decryptResult);
                JavaType parametricType = OBJECT_MAPPER.getTypeFactory().constructFromCanonical(respClazz.getCanonicalName());
                JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(DCEPBaseResponse.class, parametricType);

                dcepBaseResponse = OBJECT_MAPPER.readValue(decryptResult, javaType);

                this.enclosureCodeMessage(dcepBaseResponse, decryptResult);
            } else {
                result = response.errorBody().string();
                log.error(TAG + "请求失败! 请求参数: {},  返回信息: {}", requestParamsStr, result);
                dcepBaseResponse = new DCEPBaseResponse();
                dcepBaseResponse.setCode(response.code());
                dcepBaseResponse.setMessage(result);
            }
            return dcepBaseResponse;
        } catch (Exception e) {
            log.error(TAG + "系统错误。请求参数: {}", requestParamsStr, e);
            dcepBaseResponse = new DCEPBaseResponse();
            dcepBaseResponse.setCode(MessagePack.EXCEPTION);
            dcepBaseResponse.setMessage("系统错误");
            dcepBaseResponse.setFullMessage(e.getMessage());
        }
        return dcepBaseResponse;
    }

    /**
     * 封装code message
     * @param dcepBaseResponse
     * @param result
     */
    private void enclosureCodeMessage(DCEPBaseResponse dcepBaseResponse, String result) {
        if (ReturnCodeEnum.SUCCESS.getCode().equals(dcepBaseResponse.getReturnCode())) {
            dcepBaseResponse.setCode(MessagePack.OK);
            dcepBaseResponse.setMessage(dcepBaseResponse.getReturnMsg());
        } else {
            dcepBaseResponse.setCode(MessagePack.EXCEPTION);
            String returnMsg = dcepBaseResponse.getReturnMsg();
            if (StringUtils.isNotBlank(returnMsg)) {
                dcepBaseResponse.setMessage(returnMsg);
            } else {
                ReturnCodeEnum returnCodeEnum = Enums.getIfPresent(ReturnCodeEnum.class, dcepBaseResponse.getReturnCode()).orNull();
                if (returnCodeEnum != null) {
                    dcepBaseResponse.setMessage(returnCodeEnum.getMsg());
                } else {
                    dcepBaseResponse.setMessage(dcepBaseResponse.getReturnCode());
                    dcepBaseResponse.setFullMessage(result);
                }
            }
        }
    }

    /**
     * 异步回调参数验签
     * @param params
     * @return
     */
    public boolean verifySign(DCEPNotifyParams params) {
        String oriStr = this.genOrderedKVParams(params);
        // 验签
        RSASig rsaSig = new RSASig();
        rsaSig.setPublicKey(dcepPublicKey);
        log.debug("建行异步回调，准备验签。 签名原始串：{},  返回的签名参数: {}", oriStr, params.getSign());
        boolean verifyResult = rsaSig.verifySigature(params.getSign(), oriStr);
        return verifyResult;
    }

    /**
     * 添加scheme
     * @param url
     * @return
     */
    @Deprecated
    private String addSchemeIfNotPresent(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (StringUtils.isBlank(scheme)) {
                url = "http://" + url;  // https?
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("非法的uri格式: " + url);
        }
        return url;
    }

}
