package com.github.bluecatlee.dcep.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 商户流水查询响应
 *
 * @Author Bluecatlee
 * @Date 2021/10/12 13:35
 */
@Data
public class QueryResponse extends DataInfo {

    /**
     * 总页次
     */
    @JsonProperty("PAGE_COUNT")
    private String pageCount;

    /**
     * 当前页次
     */
    @JsonProperty("CUR_PAG")
    private String curPage;

    private List<TransactionInfoDetail> list;

}
