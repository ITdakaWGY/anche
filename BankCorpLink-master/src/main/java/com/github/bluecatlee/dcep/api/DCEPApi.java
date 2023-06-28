package com.github.bluecatlee.dcep.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface DCEPApi {

    @GET
    Call<ResponseBody> authorize(@Url String url, @Query("FT_CORPID") String ftCorpid, @Query("ccbParam") String ccbParam);

    @Headers({
        "User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
    })
    @GET
    Call<ResponseBody> execute(@Url String url, @QueryMap Map<String, String> map);

}
