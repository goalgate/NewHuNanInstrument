package cn.cbdi.hunaninstrument.Retrofit.ConnectApi;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * @author Created by WZW on 2021-03-16 10:35.
 * @description
 */
public interface HeNanApi {

    @POST("{prefix}")
    Observable<String> GeneralUpdata(
            @Path(value = "prefix", encoded = true) String prefix,
            @QueryMap Map<String, String> map);


    @POST("{prefix}")
    Observable<String> GeneralPersionInfo(
            @Path(value = "prefix", encoded = true) String prefix,
            @QueryMap Map<String, String> map);

    @FormUrlEncoded
    @POST("{prefix}")
    Observable<String> withDataRs(
            @Path(value = "prefix", encoded = true) String prefix,
            @Field("dataType") String dataType, @Field("key") String key, @Field("jsonData") String jsonData);

}
