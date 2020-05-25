package cn.cbsd.cjyfunctionlib.Func_HttpConnect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import cn.cbsd.cjyfunctionlib.Func_HttpConnect.Api.TestApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by wzw on 16/7/16.
 */
public class NetworkHelper {

    static String staticUrl = "http://129.204.110.143:8031/";

    private static TestApi testApi;

    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static <S> S createService(Class<S> serviceClass) {
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
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
                .baseUrl(staticUrl).client(client).build();
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

    public static TestApi testApi() {
        if (testApi == null) {
            testApi = createService(TestApi.class);
        }
        return testApi;
    }

    private static NetworkCallback global_callback;

    public static void getInternetStatus(Context context, NetworkCallback callback) {
        global_callback = callback;
        if (isNetworkConnected(context)) {
            callback.NowStatus(NetworkStatus.Internet_connecting);
            new Thread(new Runnable() {
                String result = null;

                @Override
                public void run() {
                    try {
                        String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
                        Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
                        // 读取ping的内容，可以不加
                        InputStream input = p.getInputStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(input));
                        StringBuffer stringBuffer = new StringBuffer();
                        String content = "";
                        while ((content = in.readLine()) != null) {
                            stringBuffer.append(content);
                        }
                        Log.d("------ping-----", "result content : " + stringBuffer.toString());
                        // ping的状态
                        int status = p.waitFor();
                        if (status == 0) {
                            result = "success";
                            handler.sendEmptyMessage(0x666);
                            return;
                        } else {
                            result = "failed";
                        }
                    } catch (IOException e) {
                        result = "IOException";
                    } catch (InterruptedException e) {
                        result = "InterruptedException";
                    } finally {
                        Log.d("----result---", "result = " + result);
                    }
                    handler.sendEmptyMessage(0x777);
                }
            }).start();

        } else {
            callback.NowStatus(NetworkStatus.Network_disconnected);
        }
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x666:
                    global_callback.NowStatus(NetworkStatus.connected);
                    break;
                case 0x777:
                    global_callback.NowStatus(NetworkStatus.Internet_disconnected);
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public static boolean isNetworkConnected(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    public enum NetworkStatus {
        Network_disconnected, Internet_disconnected, connected, Internet_connecting
    }

    public interface NetworkCallback {
        void NowStatus(NetworkStatus status);
    }

}
