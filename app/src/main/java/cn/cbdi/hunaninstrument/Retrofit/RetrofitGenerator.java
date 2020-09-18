package cn.cbdi.hunaninstrument.Retrofit;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.HNMBYApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.HeBeiApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.NMGYZBApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.NewNMGApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.XinWeiGuanApi;
import cn.cbdi.hunaninstrument.Retrofit.ConnectApi.YZBApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Retrofit变量初始化
 * Created by SmileXie on 16/7/16.
 */
public class RetrofitGenerator {

    private static HNMBYApi hnmbyApi;

    private HNMBYApi testHnmbyApi;

    private static XinWeiGuanApi xinWeiGuanApi;

    private XinWeiGuanApi testXinWeiGuanApi;

    private static HeBeiApi heBeiApi;

    private HeBeiApi testHeBeiApi;

    private static NMGYZBApi nmgyzbApi;

    private NMGYZBApi testNMGYZBApi;

    private static NewNMGApi nmgApi;

    private NewNMGApi testNewNMGApi;

    private YZBApi testYzbApi;

    private static YZBApi yzbApi;

    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static <S> S createService(Class<S> serviceClass) {
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("Content-Type", "application/json; charset=UTF-8")
                                .build();

                        return chain.proceed(request);
                    }
                })
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(AppInit.getInstrumentConfig().getServerId()).client(client).build();
                .baseUrl(SPUtils.getInstance("config").getString("ServerId")).client(client).build();
        return retrofit.create(serviceClass);
    }

    private <S> S createService(Class<S> serviceClass, String url) {
        OkHttpClient client = okHttpClient.connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(url).client(client).build();
        return retrofit.create(serviceClass);
    }

    public HNMBYApi getHnmbyApi(String url) {
        if (testHnmbyApi == null) {
            testHnmbyApi = createService(HNMBYApi.class, url);
        }
        return testHnmbyApi;
    }

    public static HNMBYApi getHnmbyApi() {
        if (hnmbyApi == null) {
            hnmbyApi = createService(HNMBYApi.class);
        }
        return hnmbyApi;
    }

    public XinWeiGuanApi getXinWeiGuanApi(String url) {
        if (testXinWeiGuanApi == null) {
            testXinWeiGuanApi = createService(XinWeiGuanApi.class, url);
        }
        return testXinWeiGuanApi;
    }

    public static XinWeiGuanApi getXinWeiGuanApi() {
        if (xinWeiGuanApi == null) {
            xinWeiGuanApi = createService(XinWeiGuanApi.class);
        }
        return xinWeiGuanApi;
    }

    public HeBeiApi getHeBeiApi(String url) {
        if (testHeBeiApi == null) {
            testHeBeiApi = createService(HeBeiApi.class, url);
        }
        return testHeBeiApi;
    }

    public static HeBeiApi getHeBeiApi() {
        if (heBeiApi == null) {
            heBeiApi = createService(HeBeiApi.class);
        }
        return heBeiApi;
    }

    public NMGYZBApi getNMGYZBApi(String url) {
        if (testNMGYZBApi == null) {
            testNMGYZBApi = createService(NMGYZBApi.class, url);
        }
        return testNMGYZBApi;
    }

    public static NMGYZBApi getNMGYZBApi() {
        if (nmgyzbApi == null) {
            nmgyzbApi = createService(NMGYZBApi.class);
        }
        return nmgyzbApi;
    }


    public YZBApi getYzbApi(String url){
        if(testYzbApi==null){
            testYzbApi = createService(YZBApi.class,url);
        }
        return testYzbApi;
    }


    public static YZBApi getYzbApi(){
        if (yzbApi==null){
            yzbApi = createService(YZBApi.class);
        }
        return yzbApi;
    }

    public NewNMGApi getTestNewNMGApi (String url){
        if(testNewNMGApi==null){
            testNewNMGApi = createService(NewNMGApi.class,url);
        }
        return testNewNMGApi;
    }


    public static NewNMGApi getNewNMGApi(){
        if (nmgApi==null){
            nmgApi = createService(NewNMGApi.class);
        }
        return nmgApi;
    }
}
