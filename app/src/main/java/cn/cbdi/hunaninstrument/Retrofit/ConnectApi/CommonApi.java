package cn.cbdi.hunaninstrument.Retrofit.ConnectApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @author Created by WZW on 2021-03-03 16:55.
 * @description
 */
public interface CommonApi {

    @FormUrlEncoded
    @POST
    Observable<ResponseBody> recentPic(@Field("dataType") String dataType, @Field("key") String key, @Field("idcard") String id);

    @FormUrlEncoded
    @POST
    Observable<String> syncPersonInfo(@Field("dataType") String dataType, @Field("key") String key, @Field("persionType") int persionType);

}
