package cn.cbdi.hunaninstrument.Tool;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Config.GZYZB_Config;
import cn.cbdi.hunaninstrument.Config.HebeiConfig;
import cn.cbdi.hunaninstrument.Config.HuNanConfig;
import cn.cbdi.hunaninstrument.Config.NMGFB_NewConfig;
import cn.cbdi.hunaninstrument.Config.NMGYZB_Config;
import cn.cbdi.hunaninstrument.Config.XinWeiGuan_Config;
import cn.cbdi.hunaninstrument.Config.YZBYPT_Config;
import cn.cbdi.hunaninstrument.Config.YanChengConfig;
import cn.cbdi.hunaninstrument.EventBus.FaceIdentityEvent;
import cn.cbdi.hunaninstrument.Project_HuNan.HuNanService;
import cn.cbdi.hunaninstrument.Project_XinWeiGuan.ParsingTool;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Func_WebSocket.SocketHelper;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import cn.cbsd.cjyfunctionlib.Tools.NetInfo;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.http.Url;

public class MySocketHelper extends SocketHelper {

    private String TAG = MySocketHelper.class.getSimpleName();

    SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    Context mContext;

    List<Employer> employers;

    JSONArray dataArray;

    public MySocketHelper(Context mContext) {
        this.mContext = mContext;
    }

    JSONObject jsonObject;

    JSONObject data;

    JSONObject getData;

    HashMap<String, String> paramsMap = new HashMap<String, String>();

    @Override
    public void dealData(WebSocket conn, int code, JSONObject jmessage) {
        try {
            switch (code) {
                case cnt_getDaid:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getDaid);
                    data.put("param", config.getString("daid"));
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_getEthMac:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getEthMac);
                    data.put("param", new NetInfo().getMac());
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_getIP:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getIP);
                    data.put("param", NetworkUtils.getIPAddress(true));
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_getServerId:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getServerId);
                    data.put("param", config.getString("ServerId"));
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_getTem:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getTem);
                    data.put("Tem", OutputControlPresenter.getInstance().getTemperature());
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_getCPUTem:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getCPUTem);
                    data.put("CPUTem", CJYHelper.getInstance(mContext).readCPUTem(0));
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_getGPUTem:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getGPUTem);
                    data.put("GPUTem", CJYHelper.getInstance(mContext).readCPUTem(1));
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_reboot:
                    CJYHelper.getInstance(AppInit.getContext()).reboot();
                    break;
                case cnt_setStaticIP:
                    getData = jmessage.getJSONObject("data");
                    String ip = getData.getString("ip");
                    String mask = getData.getString("mask");
                    String gateway = getData.getString("gateway");
                    String dns1 = getData.getString("dns1");
                    String dns2 = getData.getString("dns2");
                    CJYHelper.getInstance(mContext).setStaticEthIPAddress(ip, gateway, mask, dns1, dns2);
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_CommonBack);
                    data.put("errCode", 0);
                    data.put("errMsg", "Success");
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_setDynamicIP:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_CommonBack);
                    data.put("errCode", 0);
                    data.put("errMsg", "Success");
                    jsonObject.put("data", data);
                    CJYHelper.getInstance(mContext).setDhcpIpAddress();
                    conn.send(jsonObject.toString());
                    break;
                case cnt_setServerId:
                    getData = jmessage.getJSONObject("data");
                    String newServerId = getData.getString("ServerId");
                    SetServerAddress(newServerId, new Callback() {
                        @Override
                        public void Connected() {
                            try {
                                jsonObject = new JSONObject();
                                data = new JSONObject();
                                jsonObject.put("code", cnt_CommonBack);
                                data.put("errCode", 0);
                                data.put("errMsg", "Success");
                                jsonObject.put("data", data);
                                conn.send(jsonObject.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void DisConnected() {
                            try {
                                jsonObject = new JSONObject();
                                data = new JSONObject();
                                jsonObject.put("code", cnt_CommonBack);
                                data.put("errCode", 1);
                                data.put("errMsg", "Failed");
                                jsonObject.put("data", data);
                                conn.send(jsonObject.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case cnt_getCGUser:
                    employers = mdaoSession.loadAll(Employer.class);
                    dataArray = new JSONArray();
                    for (Employer employer : employers) {
                        if (employer.getType() == 1) {
                            JSONObject json = new JSONObject();
                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '"
                                    + employer.getCardID().toUpperCase() + "'").get(0);
                            json.put("ID", employer.getCardID());
                            json.put("name", keeper.getName());
                            dataArray.put(json);
                        }
                    }

                    if (dataArray.length() > 0) {
                        jsonObject = new JSONObject();
                        jsonObject.put("code", cnt_getCGUser);
                        jsonObject.put("data", dataArray);
                        conn.send(jsonObject.toString());
                    } else {
                        jsonObject = new JSONObject();
                        jsonObject.put("code", cnt_getCGUser);
                        conn.send(jsonObject.toString());
                    }
                    break;
                case cnt_getXJUser:
                    employers = mdaoSession.loadAll(Employer.class);
                    dataArray = new JSONArray();
                    for (Employer employer : employers) {
                        if (employer.getType() == 2) {
                            JSONObject json = new JSONObject();
                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '"
                                    + employer.getCardID().toUpperCase() + "'").get(0);
                            json.put("ID", employer.getCardID());
                            json.put("name", keeper.getName());
                            dataArray.put(json);
                        }
                    }
                    if (dataArray.length() > 0) {
                        jsonObject = new JSONObject();
                        jsonObject.put("code", cnt_getXJUser);
                        jsonObject.put("data", dataArray);
                        conn.send(jsonObject.toString());
                    } else {
                        jsonObject = new JSONObject();
                        jsonObject.put("code", cnt_getXJUser);
                        conn.send(jsonObject.toString());
                    }
                    break;
                case cnt_updateUser:
                    if (AppInit.getInstrumentConfig().getUpDataPrefix()
                            .equals(new HebeiConfig().getUpDataPrefix())) {
                        SafeCheck safeCheck = new SafeCheck();
                        safeCheck.setURL(config.getString("ServerId"));
                        paramsMap.put("daid", config.getString("daid"));
                        paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
                        syncData1();
                    } else {
                        syncData();
                    }
                    break;
            }
        } catch (JSONException e) {
            ToastUtils.showLong(e.toString());
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }


    private void syncData() {
        RetrofitGenerator.getCommonApi()
                .syncPersonInfo(AppInit.getInstrumentConfig().getPersonInfoPrefix().substring(0,
                        AppInit.getInstrumentConfig().getPersonInfoPrefix().length()-1), "updatePersion", config.getString("key"), 3)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        try {
                            mdaoSession.getEmployerDao().deleteAll();
                            if (s.equals("no")) {

                            } else {
                                String[] idList = s.split("\\|");
                                if (idList.length > 0) {
                                    for (String id : idList) {
                                        if (!id.equals("")) {
                                            mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 3));
                                        }
                                    }
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new FaceIdentityEvent());
                    }

                    @Override
                    public void onComplete() {
                        RetrofitGenerator.getCommonApi()
                                .syncPersonInfo(AppInit.getInstrumentConfig().getPersonInfoPrefix().substring(0,
                                        AppInit.getInstrumentConfig().getPersonInfoPrefix().length()-1), "updatePersion", config.getString("key"), 2)
                                .subscribeOn(Schedulers.io())
                                .unsubscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<String>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(String s) {
                                        try {
                                            if (s.equals("no")) {

                                            } else {
                                                String[] idList = s.split("\\|");
                                                if (idList.length > 0) {
                                                    for (String id : idList) {
                                                        if (!id.equals("")) {
                                                            mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 2));
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        EventBus.getDefault().post(new FaceIdentityEvent());

                                    }

                                    @Override
                                    public void onComplete() {
                                        RetrofitGenerator.getCommonApi()
                                                .syncPersonInfo(AppInit.getInstrumentConfig().getPersonInfoPrefix().substring(0,
                                                        AppInit.getInstrumentConfig().getPersonInfoPrefix().length()-1), "updatePersion", config.getString("key"), 1)
                                                .subscribeOn(Schedulers.io())
                                                .unsubscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Observer<String>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onNext(String s) {
                                                        try {
                                                            if (s.equals("no")) {

                                                            } else {
                                                                String[] idList = s.split("\\|");
                                                                if (idList.length > 0) {
                                                                    for (String id : idList) {
                                                                        if (!id.equals("")) {
                                                                            mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 1));
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        EventBus.getDefault().post(new FaceIdentityEvent());

                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        try {
                                                            List<Keeper> keeperList = mdaoSession.getKeeperDao().loadAll();
                                                            for (Keeper keeper : keeperList) {
                                                                try {
                                                                    mdaoSession.queryRaw(Employer.class, "where CARD_ID = '" + keeper.getCardID() + "'").get(0);
                                                                } catch (IndexOutOfBoundsException e) {
                                                                    mdaoSession.delete(keeper);
                                                                    FacePresenter.getInstance().FaceDeleteByUserName(keeper.getName());
                                                                }
                                                            }
                                                        } catch (SQLiteException e) {
                                                            Log.e(TAG, e.toString());
                                                        }
                                                        getPic();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    int count = 0;
    StringBuffer logMen;
    private Handler handler = new Handler();

    private void getPic() {
        logMen = new StringBuffer();
        count = 0;
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            for (Employer employer : employers) {
                RetrofitGenerator.getCommonApi()
                        .recentPic(AppInit.getInstrumentConfig().getUpDataPrefix().substring(0,
                                AppInit.getInstrumentConfig().getUpDataPrefix().length() - 1), "recentPic", config.getString("key"), employer.getCardID())
                        .subscribeOn(Schedulers.single())
                        .unsubscribeOn(Schedulers.single())
                        .observeOn(Schedulers.single())
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                try {
                                    count++;
                                    JSONObject jsonObject = new JSONObject(responseBody.string());
                                    String result = jsonObject.getString("result");
                                    if (result.equals("true")) {
                                        String ps = jsonObject.getString("returnPic");
                                        String name = jsonObject.getString("personName");
                                        try {
                                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '" + employer.getCardID().toUpperCase() + "'").get(0);
                                            Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                            FacePresenter.getInstance().FaceUpdate(bitmap, name, new UserInfoManager.UserInfoListener() {
                                                public void updateImageSuccess(Bitmap bitmap) {
                                                    keeper.setHeadphoto(ps);
                                                    keeper.setHeadphotoBW(null);
                                                    mdaoSession.getKeeperDao().insertOrReplace(keeper);
                                                }

                                                public void updateImageFailure(String message) {
                                                    Log.e(TAG, message);
                                                }
                                            });

                                        } catch (IndexOutOfBoundsException e) {
                                            if (!TextUtils.isEmpty(ps)) {
                                                Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                                if (FacePresenter.getInstance().FaceRegByBase64(name, employer.getCardID(), ps)) {
                                                    User user = FacePresenter.getInstance().GetUserByUserName(name);
                                                    Keeper keeper = new Keeper(employer.getCardID().toUpperCase(),
                                                            name, ps, null, null,
                                                            user.getUserId(), user.getFeature());
                                                    mdaoSession.getKeeperDao().insertOrReplace(keeper);
                                                    Log.e("myface", name + "人脸特征已存");

                                                }
                                            }
                                        }
                                    }
                                    if (count == employers.size()) {
                                        EventBus.getDefault().post(new FaceIdentityEvent());
                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                        if (keeperList.size() > 0) {
                                            Set<String> list = new HashSet<>();
                                            for (Keeper keeper : keeperList) {
                                                list.add(keeper.getName());
                                            }
                                            for (String name : list) {
                                                logMen.append(name + "、");
                                            }
                                            logMen.deleteCharAt(logMen.length() - 1);

                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                            Log.e(TAG, logMen.toString());

                                        } else {
                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                            Log.e(TAG, logMen.toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                    if (count == employers.size()) {
                                        EventBus.getDefault().post(new FaceIdentityEvent());
                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                        if (keeperList.size() > 0) {
                                            Set<String> list = new HashSet<>();
                                            for (Keeper keeper : keeperList) {
                                                list.add(keeper.getName());
                                            }
                                            for (String name : list) {
                                                logMen.append(name + "、");
                                            }
                                            logMen.deleteCharAt(logMen.length() - 1);

                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                            Log.e(TAG, logMen.toString());

                                        } else {
                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                            Log.e(TAG, logMen.toString());

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                count++;
                                if (count == employers.size()) {
                                    EventBus.getDefault().post(new FaceIdentityEvent());
                                    List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                    if (keeperList.size() > 0) {
                                        Set<String> list = new HashSet<>();
                                        for (Keeper keeper : keeperList) {
                                            list.add(keeper.getName());
                                        }
                                        for (String name : list) {
                                            logMen.append(name + "、");
                                        }
                                        logMen.deleteCharAt(logMen.length() - 1);

                                        handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                        Log.e(TAG, logMen.toString());

                                    } else {
                                        handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                        Log.e(TAG, logMen.toString());

                                    }
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        } else {
            EventBus.getDefault().post(new FaceIdentityEvent());
            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
        }
    }

    private void syncData1() {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "updatePersion");
        map.put("persionType", String.valueOf(3));
        RetrofitGenerator.getHeBeiApi().GeneralPersionInfo(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        try {
                            Log.e("persionType = 3", s);
                            mdaoSession.getEmployerDao().deleteAll();
                            String[] idList = s.split("\\|");
                            if (idList.length > 0) {
                                for (String id : idList) {
                                    mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 3));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new FaceIdentityEvent());
                    }

                    @Override
                    public void onComplete() {
                        map.put("persionType", String.valueOf(2));
                        RetrofitGenerator.getHeBeiApi().GeneralPersionInfo(map)
                                .subscribeOn(Schedulers.io())
                                .unsubscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<String>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(String s) {
                                        try {
                                            Log.e("persionType = 2", s);
                                            String[] idList = s.split("\\|");
                                            if (idList.length > 0) {
                                                for (String id : idList) {
                                                    mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 2));
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        EventBus.getDefault().post(new FaceIdentityEvent());
                                    }

                                    @Override
                                    public void onComplete() {
                                        map.put("persionType", String.valueOf(1));
                                        RetrofitGenerator.getHeBeiApi().GeneralPersionInfo(map)
                                                .subscribeOn(Schedulers.io())
                                                .unsubscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Observer<String>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onNext(String s) {
                                                        try {
                                                            Log.e("persionType = 1", s);
                                                            String[] idList = s.split("\\|");
                                                            if (idList.length > 0) {
                                                                for (String id : idList) {
                                                                    mdaoSession.insertOrReplace(new Employer(id.toUpperCase(), 1));
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        EventBus.getDefault().post(new FaceIdentityEvent());
                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        try {
//                                                        Employer employer1 = new Employer("411222199104206028",1);
//                                                        Employer employer2 = new Employer("44128219830820403X",1);
//                                                        mdaoSession.insertOrReplace(employer1);
//                                                        mdaoSession.insertOrReplace(employer2);
                                                            List<Keeper> keeperList = mdaoSession.getKeeperDao().loadAll();
                                                            for (Keeper keeper : keeperList) {
                                                                try {
                                                                    mdaoSession.queryRaw(Employer.class, "where CARD_ID = '" + keeper.getCardID() + "'").get(0);
                                                                } catch (IndexOutOfBoundsException e) {
                                                                    mdaoSession.delete(keeper);
                                                                    FacePresenter.getInstance().FaceDeleteByUserName(keeper.getName());
                                                                }
                                                            }
                                                        } catch (SQLiteException e) {
                                                            Log.e(TAG, e.toString());
                                                        }
                                                        getPic1();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }


    private void getPic1() {
        logMen = new StringBuffer();
        count = 0;
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            for (Employer employer : employers) {
                RetrofitGenerator.getHeBeiApi().recentPicNew("recentPic", paramsMap.get("daid"), paramsMap.get("pass"), employer.getCardID())
                        .subscribeOn(Schedulers.single())
                        .unsubscribeOn(Schedulers.single())
                        .observeOn(Schedulers.single())
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                try {
                                    count++;
                                    JSONObject jsonObject = new JSONObject(responseBody.string());
                                    String result = jsonObject.getString("result");
                                    if (result.equals("true")) {
                                        String ps = jsonObject.getString("returnPic");
                                        String name = jsonObject.getString("personName");
                                        try {
                                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '" + employer.getCardID().toUpperCase() + "'").get(0);
                                            Log.e("ps_len", String.valueOf(ps.length()));
                                            Log.e("keeper_len", String.valueOf(keeper.getHeadphoto().replaceAll("\r|\n", "").length()));
                                            Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                            FacePresenter.getInstance().FaceUpdate(bitmap, name, new UserInfoManager.UserInfoListener() {
                                                public void updateImageSuccess(Bitmap bitmap) {
                                                    keeper.setHeadphoto(ps);
                                                    keeper.setHeadphotoBW(null);
                                                    mdaoSession.getKeeperDao().insertOrReplace(keeper);
                                                }

                                                public void updateImageFailure(String message) {
                                                    Log.e(TAG, message);
                                                }
                                            });
                                        } catch (IndexOutOfBoundsException e) {
                                            if (!TextUtils.isEmpty(ps)) {
                                                Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                                if (FacePresenter.getInstance().FaceRegByBase64(name, employer.getCardID(), ps)) {
                                                    User user = FacePresenter.getInstance().GetUserByUserName(name);
                                                    Keeper keeper = new Keeper(employer.getCardID().toUpperCase(),
                                                            name, ps, null, null,
                                                            user.getUserId(), user.getFeature());
                                                    mdaoSession.getKeeperDao().insertOrReplace(keeper);
                                                    Log.e("myface", name + "人脸特征已存");

                                                }
                                            }
                                        }
                                    }
                                    if (count == employers.size()) {
                                        EventBus.getDefault().post(new FaceIdentityEvent());
                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                        if (keeperList.size() > 0) {
                                            Set<String> list = new HashSet<>();
                                            for (Keeper keeper : keeperList) {
                                                list.add(keeper.getName());
                                            }
                                            for (String name : list) {
                                                logMen.append(name + "、");
                                            }
                                            logMen.deleteCharAt(logMen.length() - 1);

                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                            Log.e(TAG, logMen.toString());

                                        } else {
                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                            Log.e(TAG, logMen.toString());

                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                    if (count == employers.size()) {
                                        EventBus.getDefault().post(new FaceIdentityEvent());
                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                        if (keeperList.size() > 0) {
                                            Set<String> list = new HashSet<>();
                                            for (Keeper keeper : keeperList) {
                                                list.add(keeper.getName());
                                            }
                                            for (String name : list) {
                                                logMen.append(name + "、");
                                            }
                                            logMen.deleteCharAt(logMen.length() - 1);

                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                            Log.e(TAG, logMen.toString());

                                        } else {
                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                            Log.e(TAG, logMen.toString());

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                count++;
                                if (count == employers.size()) {
                                    EventBus.getDefault().post(new FaceIdentityEvent());
                                    List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
                                    if (keeperList.size() > 0) {
                                        Set<String> list = new HashSet<>();
                                        for (Keeper keeper : keeperList) {
                                            list.add(keeper.getName());
                                        }
                                        for (String name : list) {
                                            logMen.append(name + "、");
                                        }
                                        logMen.deleteCharAt(logMen.length() - 1);

                                        handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
                                        Log.e(TAG, logMen.toString());

                                    } else {
                                        handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
                                        Log.e(TAG, logMen.toString());

                                    }
                                }
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        } else {
            EventBus.getDefault().post(new FaceIdentityEvent());
            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
        }
    }


    private void SetServerAddress(String url, Callback callback) {
        if (AppInit.getInstrumentConfig().getClass().getName().equals(HuNanConfig.class.getName())) {
            new RetrofitGenerator().getHnmbyApi(url).withDataRs("testNet", config.getString("key"), null)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String s) {
                            try {
                                if (s.equals("true")) {
                                    config.put("ServerId", url);
                                    callback.Connected();
                                } else {
                                    callback.DisConnected();

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.showLong("服务器连接失败");
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else if (AppInit.getInstrumentConfig().getClass().getName().equals(XinWeiGuan_Config.class.getName()) ||
                AppInit.getInstrumentConfig().getClass().getName().equals(YanChengConfig.class.getName()) ||
                AppInit.getInstrumentConfig().getClass().getName().equals(NMGFB_NewConfig.class.getName())) {
            new RetrofitGenerator().getXinWeiGuanApi(url).noData("testNet", config.getString("key"))
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                String s = ParsingTool.extractMainContent(responseBody);
                                if (s.equals("true")) {
                                    config.put("ServerId", url);
                                    callback.Connected();
                                } else {
                                    callback.DisConnected();

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.showLong("服务器连接失败");
                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        } else if (AppInit.getInstrumentConfig().getClass().getName().equals(NMGYZB_Config.class.getName())) {
            HashMap<String, String> paramsMap = new HashMap<String, String>();
            SafeCheck safeCheck = new SafeCheck();
            safeCheck.setURL(config.getString("ServerId"));
            paramsMap.put("daid", config.getString("daid"));
            paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
            paramsMap.put("dataType", "test");
            RetrofitGenerator.getNMGYZBApi().GeneralUpdata(paramsMap)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String s) {
                            if (s.startsWith("true")) {
                                callback.Connected();
                            } else {
                                callback.DisConnected();

                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.showLong("服务器连接失败");
                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        } else if (AppInit.getInstrumentConfig().getClass().getName().equals(YZBYPT_Config.class.getName()) ||
                (AppInit.getInstrumentConfig().getClass().getName().equals(GZYZB_Config.class.getName()))) {
            new RetrofitGenerator().getYzbApi(url).withDataRs("testNet", config.getString("key"), null)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String s) {
                            if (s.startsWith("true")) {
                                callback.Connected();
                            } else {
                                callback.DisConnected();

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.showLong("服务器连接失败");

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            new ServerConnectionUtil().post(url + AppInit.getInstrumentConfig().getUpDataPrefix() + "daid=" + config.getString("daid") + "&dataType=test", url
                    , (response) -> {
                        if (response != null) {
                            if (response.startsWith("true")) {
                                config.put("ServerId", url);

                            } else {
                                ToastUtils.showLong("设备验证错误");
                            }
                        } else {
                            ToastUtils.showLong("服务器连接失败");
                        }
                    });
        }
    }

    interface Callback {
        void Connected();

        void DisConnected();
    }
}
