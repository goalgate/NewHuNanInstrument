package cn.cbdi.hunaninstrument.Retrofit.ConnectApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Body;
import okhttp3.RequestBody;

public interface XinWeiGuanApi {

    @FormUrlEncoded
    @POST("caijiyiDuijie/s/kaiguanmen")
    Observable<ResponseBody> withDataRr(@Field("dataType") String dataType, @Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("caijiyiDuijie/s/kaiguanmen")
    Observable<ResponseBody> noData(@Field("dataType") String dataType, @Field("key") String key);


    @FormUrlEncoded
    @POST("caijiyiDuijie/s/kaiguanmen")
    Observable<ResponseBody> queryPersonInfo(@Field("dataType") String dataType, @Field("key") String key, @Field("psonIdcard") String id);


    @POST("caijiyiDuijie/s/kaiguanmenWithImage")
    Observable<ResponseBody> upDatawithBody(@Query("dataType") String dataType, @Query("key") String key, @Body RequestBody jsonData);

}
