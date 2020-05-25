package cn.cbsd.cjyfunctionlib.Func_HttpConnect.Api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TestApi {

    @FormUrlEncoded
    @POST("cjy/s/updata")
    Observable<String> withDataRs(@Field("dataType") String dataType, @Field("key") String key,
                                  @Field("jsonData") String jsonData);


    @FormUrlEncoded
    @POST("cjy/s/updata")
    Observable<ResponseBody> withDataRr(@Field("dataType") String dataType, @Field("key") String key, @Field("jsonData") String jsonData);

}
