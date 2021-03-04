package cn.cbsd.cjyfunctionlib.Func_WebSocket;

import android.content.Context;
import android.util.Log;


import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;


import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Tools.NetInfo;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SocketHelperImpl extends SocketHelper {

    Context mContext;

    JSONArray dataArray;

    public SocketHelperImpl(Context mContext) {
        this.mContext = mContext;
    }

    JSONObject jsonObject;

    JSONObject data;

    JSONObject getData;

    @Override
    public void dealData(WebSocket conn, int code, JSONObject jmessage) {
        try {
            switch (code) {
                case cnt_getDaid:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getDaid);
                    data.put("param", "8001008888");
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
                    data.put("param", "192.168.12.211");
                    jsonObject.put("data", data);
                    conn.send(jsonObject.toString());
                    break;
                case cnt_getServerId:
                    jsonObject = new JSONObject();
                    data = new JSONObject();
                    jsonObject.put("code", cnt_getServerId);
                    data.put("param", "https://gdmb.wxhxp.cn:8009/");
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
//                    SetServerAddress(newServerId, new Callback() {
//                        @Override
//                        public void Connected() {
//                            try {
//                                jsonObject = new JSONObject();
//                                data = new JSONObject();
//                                jsonObject.put("code", cnt_CommonBack);
//                                data.put("errCode", 0);
//                                data.put("errMsg", "Success");
//                                jsonObject.put("data", data);
//                                conn.send(jsonObject.toString());
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void DisConnected() {
//                            try {
//                                jsonObject = new JSONObject();
//                                data = new JSONObject();
//                                jsonObject.put("code", cnt_CommonBack);
//                                data.put("errCode", 1);
//                                data.put("errMsg", "Failed");
//                                jsonObject.put("data", data);
//                                conn.send(jsonObject.toString());
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
                    break;
                case cnt_getCGUser:
//                    employers = mdaoSession.loadAll(Employer.class);
//                    dataArray = new JSONArray();
//                    for (Employer employer : employers) {
//                        if (employer.getType() == 1) {
//                            JSONObject json = new JSONObject();
//                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '"
//                                    + employer.getCardID().toUpperCase() + "'").get(0);
//                            json.put("ID", employer.getCardID());
//                            json.put("name", keeper.getName());
//                            dataArray.put(json);
//                        }
//                    }
//
//                    if (dataArray.length() > 0) {
//                        jsonObject = new JSONObject();
//                        jsonObject.put("code", cnt_getCGUser);
//                        jsonObject.put("data", dataArray);
//                        conn.send(jsonObject.toString());
//                    } else {
//                        jsonObject = new JSONObject();
//                        jsonObject.put("code", cnt_getCGUser);
//                        conn.send(jsonObject.toString());
//                    }
                    break;
                case cnt_getXJUser:
//                    employers = mdaoSession.loadAll(Employer.class);
//                    dataArray = new JSONArray();
//                    for (Employer employer : employers) {
//                        if (employer.getType() == 2) {
//                            JSONObject json = new JSONObject();
//                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '"
//                                    + employer.getCardID().toUpperCase() + "'").get(0);
//                            json.put("ID", employer.getCardID());
//                            json.put("name", keeper.getName());
//                            dataArray.put(json);
//                        }
//                    }
//                    if (dataArray.length() > 0) {
//                        jsonObject = new JSONObject();
//                        jsonObject.put("code", cnt_getCGUser);
//                        jsonObject.put("data", dataArray);
//                        conn.send(jsonObject.toString());
//                    } else {
//                        jsonObject = new JSONObject();
//                        jsonObject.put("code", cnt_getCGUser);
//                        conn.send(jsonObject.toString());
//                    }
                    break;
            }

        } catch (JSONException e) {
            Log.e("Exception",e.toString());
        } catch (Exception e) {
            Log.e("Exception",e.toString());
        }
    }




//    private void SetServerAddress(String url, Callback callback) {
//        if (AppInit.getInstrumentConfig().getClass().getName().equals(HuNanConfig.class.getName())) {
//            new RetrofitGenerator().getHnmbyApi(url).withDataRs("testNet", config.getString("key"), null)
//                    .subscribeOn(Schedulers.io())
//                    .unsubscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<String>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(String s) {
//                            try {
//                                if (s.equals("true")) {
//                                    config.put("ServerId", url);
//                                    callback.Connected();
//                                } else {
//                                    callback.DisConnected();
//
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            ToastUtils.showLong("服务器连接失败");
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//        } else if (AppInit.getInstrumentConfig().getClass().getName().equals(XinWeiGuan_Config.class.getName()) ||
//                AppInit.getInstrumentConfig().getClass().getName().equals(YanChengConfig.class.getName()) ||
//                AppInit.getInstrumentConfig().getClass().getName().equals(NMGFB_NewConfig.class.getName())) {
//            new RetrofitGenerator().getXinWeiGuanApi(url).noData("testNet", config.getString("key"))
//                    .subscribeOn(Schedulers.io())
//                    .unsubscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<ResponseBody>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(ResponseBody responseBody) {
//                            try {
//                                String s = ParsingTool.extractMainContent(responseBody);
//                                if (s.equals("true")) {
//                                    config.put("ServerId", url);
//                                    callback.Connected();
//                                } else {
//                                    callback.DisConnected();
//
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            ToastUtils.showLong("服务器连接失败");
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//
//        } else if (AppInit.getInstrumentConfig().getClass().getName().equals(NMGYZB_Config.class.getName())) {
//            HashMap<String, String> paramsMap = new HashMap<String, String>();
//            SafeCheck safeCheck = new SafeCheck();
//            safeCheck.setURL(config.getString("ServerId"));
//            paramsMap.put("daid", config.getString("daid"));
//            paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
//            paramsMap.put("dataType", "test");
//            RetrofitGenerator.getNMGYZBApi().GeneralUpdata(paramsMap)
//                    .subscribeOn(Schedulers.io())
//                    .unsubscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<String>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(String s) {
//                            if (s.startsWith("true")) {
//                                callback.Connected();
//                            } else {
//                                callback.DisConnected();
//
//                            }
//
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            ToastUtils.showLong("服务器连接失败");
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//
//        } else if (AppInit.getInstrumentConfig().getClass().getName().equals(YZBYPT_Config.class.getName()) ||
//                (AppInit.getInstrumentConfig().getClass().getName().equals(GZYZB_Config.class.getName()))) {
//            new RetrofitGenerator().getYzbApi(url).withDataRs("testNet", config.getString("key"), null)
//                    .subscribeOn(Schedulers.io())
//                    .unsubscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<String>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(String s) {
//                            if (s.startsWith("true")) {
//                                callback.Connected();
//                            } else {
//                                callback.DisConnected();
//
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            ToastUtils.showLong("服务器连接失败");
//
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//        } else {
//            new ServerConnectionUtil().post(url + AppInit.getInstrumentConfig().getUpDataPrefix() + "daid=" + config.getString("daid") + "&dataType=test", url
//                    , (response) -> {
//                        if (response != null) {
//                            if (response.startsWith("true")) {
//                                config.put("ServerId", url);
//
//                            } else {
//                                ToastUtils.showLong("设备验证错误");
//                            }
//                        } else {
//                            ToastUtils.showLong("服务器连接失败");
//                        }
//                    });
//        }
//    }

    interface Callback {
        void Connected();

        void DisConnected();
    }
}
