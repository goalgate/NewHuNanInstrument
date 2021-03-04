package cn.cbdi.hunaninstrument.Retrofit.ConnectApi;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface XAYZBApi {
    @FormUrlEncoded
    @POST("cjy/s/fbcjy_updata")
    Observable<String> faceUpload(@Field("dataType") String dataType, @Field("daid") String daid, @Field("pass") String pass, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("cjy/s/fbcjy_updata")
    Observable<ResponseBody> recentPicNew(@Field("dataType") String dataType, @Field("daid") String daid, @Field("pass") String pass, @Field("psonIdcard") String id);

    @FormUrlEncoded
    @POST("cjy/s/fbcjy_updata")
    Observable<String> getAllFace(@Field("dataType") String dataType, @Field("daid") String daid, @Field("pass") String pass);


    @POST("cjy/s/fbcjy_updata")
    Observable<String> GeneralUpdata(@QueryMap Map<String, String> map);


    @POST("cjy/s/fbcjy_updata")
    Observable<String> GeneralPersionInfo(@QueryMap Map<String, String> map);

    @FormUrlEncoded
    @POST("cjy/s/fbcjy_updata")
    Observable<String> withDataRs(@Field("dataType") String dataType, @Field("key") String key, @Field("jsonData") String jsonData);
}
