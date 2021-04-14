package cn.cbdi.hunaninstrument.Project_Hebei;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;


import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cundong.utils.PatchUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.ReUploadWithBsBean;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.FaceIdentityEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.DoorState.WarehouseDoor;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.Tool.SafeCheck;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.hunaninstrument.greendao.ReUploadBeanDao;
import cn.cbdi.hunaninstrument.greendao.ReUploadWithBsBeanDao;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.ApkUtils;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.SignUtils;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.UpdateConstant;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.view.IOutputControlView;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;


public class HeBeiService extends Service implements IOutputControlView {

    private String TAG = HeBeiService.class.getSimpleName();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat url_timeformatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");

    HashMap<String, String> paramsMap = new HashMap<String, String>();

    OutputControlPresenter sp = OutputControlPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

    int last_mTemperature = 0;

    int last_mHumidity = 0;

    Disposable rx_delay;

    Disposable unlock_noOpen;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Md5", SignUtils.getSignMd5Str(AppInit.getInstance()));
        sp.SwitchPresenterSetView(this);
        EventBus.getDefault().register(this);
        mapInit();
        Observable.timer(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> syncData());
        reUpload();
        Observable.interval(40, 300, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> testNet());
        Observable.interval(0, AppInit.getInstrumentConfig().getCheckOnlineTime(), TimeUnit.MINUTES)
                .observeOn(Schedulers.io())
                .subscribe((l) -> checkOnline());
        if (AppInit.getInstrumentConfig().isTemHum()) {
            sp.readHum(5, true);
            Observable.interval(10, 3600, TimeUnit.SECONDS).observeOn(Schedulers.io())
                    .subscribe((l) -> StateRecord());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        Lock.getInstance().setState(Lock.LockState.STATE_Unlock);
        Lock.getInstance().doNext();
        if (!AppInit.getInstrumentConfig().isHongWai()) {
            Observable.timer(120, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            unlock_noOpen = d;
                        }

                        @Override
                        public void onNext(Long aLong) {
                            Lock.getInstance().setState(Lock.LockState.STATE_Lockup);
                            sp.buzz(IOutputControl.Hex.H0);
                            EventBus.getDefault().post(new LockUpEvent());
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }
    @Override
    public void onTemHum(int temperature, int humidity, String THSwitchValue) {
        EventBus.getDefault().post(new TemHumEvent(temperature, humidity));
        if ((Math.abs(temperature - last_mTemperature) > 3 || Math.abs(temperature - last_mTemperature) > 10)) {
            last_mTemperature = temperature;
            last_mHumidity = humidity;
            StateRecord();
        }
        last_mTemperature = temperature;
        last_mHumidity = humidity;
    }

    @Override
    public void onDoorState(Door.DoorState state) {
        if (AppInit.getInstrumentConfig().isHongWai()) {
            if (!WarehouseDoor.getInstance().getMdoorState().equals(state)) {
                WarehouseDoor.getInstance().setMdoorState(state);
                if (state.equals(Door.DoorState.State_Open)) {
                    if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
                        Lock.getInstance().doNext();
                        alarmRecord();
                    }
                }
            }

        } else {
            if (!WarehouseDoor.getInstance().getMdoorState().equals(state)) {
                if (state.equals(Door.DoorState.State_Open)) {
                    WarehouseDoor.getInstance().setMdoorState(state);
                    WarehouseDoor.getInstance().doNext();
                    if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
                        alarmRecord();
                    }
                    if (unlock_noOpen != null) {
                        unlock_noOpen.dispose();
                    }
                    if (rx_delay != null) {
                        rx_delay.dispose();
                    }
                } else {
                    WarehouseDoor.getInstance().setMdoorState(state);
                    WarehouseDoor.getInstance().doNext();
                    if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Unlock)) {
                        final String closeDoorTime = TimeUtils.getNowString();
                        Observable.timer(10, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        rx_delay = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        Lock.getInstance().setState(Lock.LockState.STATE_Lockup);
                                        sp.buzz(IOutputControl.Hex.H0);
                                        if (unlock_noOpen != null) {
                                            unlock_noOpen.dispose();
                                        }
                                        CloseDoorRecord(closeDoorTime);
                                        EventBus.getDefault().post(new LockUpEvent());
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    } else {
                        WarehouseDoor.getInstance().setMdoorState(state);
                    }
                }
            }
        }

    }

    @Override
    public void onSwitchValue(String Value) {

    }

    private Handler handler = new Handler();

    private void reUpload() {
        ReUploadWithBsBeanDao reUploadWithBsBeanDao = mdaoSession.getReUploadWithBsBeanDao();
        List<ReUploadWithBsBean> list = reUploadWithBsBeanDao.queryBuilder().list();
        for (final ReUploadWithBsBean bean : list) {
            if (bean.getContent() != null) {
                if (bean.getType_patrol() != 0) {
                    connectionUtil.post_SingleThread(config.getString("ServerId") + AppInit.getInstrumentConfig().getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("daid") + "&checkType=" + bean.getType_patrol(),
                            config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                            reUploadWithBsBeanDao.delete(bean);
                                        }
                                    }
                                }
                            });
                } else {
                    connectionUtil.post_SingleThread(config.getString("ServerId") + AppInit.getInstrumentConfig().getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("daid"),
                            config.getString("ServerId"), bean.getContent(), new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                            reUploadWithBsBeanDao.delete(bean);
                                        }
                                    }
                                }
                            });
                }
            } else {
                connectionUtil.post_SingleThread(config.getString("ServerId") + AppInit.getInstrumentConfig().getUpDataPrefix() + bean.getMethod() + "&daid=" + config.getString("daid"),
                        config.getString("ServerId"), new ServerConnectionUtil.Callback() {
                            @Override
                            public void onResponse(String response) {
                                if (response != null) {
                                    if (response.startsWith("true")) {
                                        Log.e("程序执行记录", "已执行删除" + bean.getMethod());
                                        reUploadWithBsBeanDao.delete(bean);
                                    }
                                }
                            }
                        });
            }
        }
        ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list1 = reUploadBeanDao.queryBuilder().list();
        for (final ReUploadBean bean : list1) {
            RetrofitGenerator.getHeBeiApi().faceUpload("faceUpload", paramsMap.get("daid"), paramsMap.get("pass"), bean.getContent())
                    .subscribeOn(Schedulers.single())
                    .unsubscribeOn(Schedulers.single())
                    .observeOn(Schedulers.single())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull String s) {
                            Log.e("信息提示", bean.getMethod());
                            reUploadBeanDao.delete(bean);


                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e("信息提示error", bean.getMethod());

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        }
    }


    private void syncData() {
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
                                                        getPic();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    StringBuffer logMen;

    private void getPic() {
        logMen = new StringBuffer();
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            CountDownLatch latch = new CountDownLatch(employers.size());
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
                                    JSONObject jsonObject = new JSONObject(responseBody.string());
                                    String result = jsonObject.getString("result");
                                    if (result.equals("true")) {
                                        String ps = jsonObject.getString("returnPic");
                                        String name = jsonObject.getString("personName");
                                        try {
                                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '" + employer.getCardID().toUpperCase() + "'").get(0);
                                            if (!TextUtils.isEmpty(ps) && keeper.getHeadphoto().length() != ps.length()) {
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
                                            }
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
                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                } finally {
                                    latch.countDown();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        } else {
            EventBus.getDefault().post(new FaceIdentityEvent());
            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
        }
    }
//    private void getPic() {
//        logMen = new StringBuffer();
//        count = 0;
//        List<Employer> employers = mdaoSession.loadAll(Employer.class);
//        if (employers.size() > 0) {
//            for (Employer employer : employers) {
//                RetrofitGenerator.getHeBeiApi().recentPicNew("recentPic", paramsMap.get("daid"), paramsMap.get("pass"), employer.getCardID())
//                        .subscribeOn(Schedulers.single())
//                        .unsubscribeOn(Schedulers.single())
//                        .observeOn(Schedulers.single())
//                        .subscribe(new Observer<ResponseBody>() {
//                            @Override
//                            public void onSubscribe(Disposable d) {
//
//                            }
//
//                            @Override
//                            public void onNext(ResponseBody responseBody) {
//                                try {
//                                    count++;
//                                    JSONObject jsonObject = new JSONObject(responseBody.string());
//
//                                    String result = jsonObject.getString("result");
//                                    if (result.equals("true")) {
//                                        String ps = jsonObject.getString("returnPic");
//                                        String name = jsonObject.getString("personName");
//                                        try {
//                                            Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '" + employer.getCardID().toUpperCase() + "'").get(0);
//                                            if (!TextUtils.isEmpty(ps) && keeper.getHeadphoto().length() != ps.length()) {
//                                                Log.e("ps_len", String.valueOf(ps.length()));
//                                                Log.e("keeper_len", String.valueOf(keeper.getHeadphoto().replaceAll("\r|\n", "").length()));
//                                                Bitmap bitmap = FileUtils.base64ToBitmap(ps);
//                                                FacePresenter.getInstance().FaceUpdate(bitmap, name, new UserInfoManager.UserInfoListener() {
//                                                    public void updateImageSuccess(Bitmap bitmap) {
//                                                        keeper.setHeadphoto(ps);
//                                                        keeper.setHeadphotoBW(null);
//                                                        mdaoSession.getKeeperDao().insertOrReplace(keeper);
//                                                    }
//                                                    public void updateImageFailure(String message) {
//                                                        Log.e(TAG, message);
//                                                    }
//                                                });
//                                            }
//                                        } catch (IndexOutOfBoundsException e) {
//                                            if (!TextUtils.isEmpty(ps)) {
//                                                Bitmap bitmap = FileUtils.base64ToBitmap(ps);
//                                                if (FacePresenter.getInstance().FaceRegByBase64(name, employer.getCardID(), ps)) {
//                                                    User user = FacePresenter.getInstance().GetUserByUserName(name);
//                                                    Keeper keeper = new Keeper(employer.getCardID().toUpperCase(),
//                                                            name, ps, null, null,
//                                                            user.getUserId(), user.getFeature());
//                                                    mdaoSession.getKeeperDao().insertOrReplace(keeper);
//                                                    Log.e("myface", name + "人脸特征已存");
//
//                                                }
//                                            }
//                                        }
//                                    }
//                                    if (count == employers.size()) {
//                                        EventBus.getDefault().post(new FaceIdentityEvent());
//                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
//                                        if (keeperList.size() > 0) {
//                                            Set<String> list = new HashSet<>();
//                                            for (Keeper keeper : keeperList) {
//                                                list.add(keeper.getName());
//                                            }
//                                            for (String name : list) {
//                                                logMen.append(name + "、");
//                                            }
//                                            logMen.deleteCharAt(logMen.length() - 1);
//
//                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
//                                            Log.e(TAG, logMen.toString());
//
//                                        } else {
//                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
//                                            Log.e(TAG, logMen.toString());
//
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                    Log.e(TAG, e.toString());
//                                    if (count == employers.size()) {
//                                        EventBus.getDefault().post(new FaceIdentityEvent());
//                                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
//                                        if (keeperList.size() > 0) {
//                                            Set<String> list = new HashSet<>();
//                                            for (Keeper keeper : keeperList) {
//                                                list.add(keeper.getName());
//                                            }
//                                            for (String name : list) {
//                                                logMen.append(name + "、");
//                                            }
//                                            logMen.deleteCharAt(logMen.length() - 1);
//
//                                            handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
//                                            Log.e(TAG, logMen.toString());
//
//                                        } else {
//                                            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
//                                            Log.e(TAG, logMen.toString());
//
//                                        }
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                count++;
//                                if (count == employers.size()) {
//                                    EventBus.getDefault().post(new FaceIdentityEvent());
//                                    List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
//                                    if (keeperList.size() > 0) {
//                                        Set<String> list = new HashSet<>();
//                                        for (Keeper keeper : keeperList) {
//                                            list.add(keeper.getName());
//                                        }
//                                        for (String name : list) {
//                                            logMen.append(name + "、");
//                                        }
//                                        logMen.deleteCharAt(logMen.length() - 1);
//
//                                        handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));
//                                        Log.e(TAG, logMen.toString());
//
//                                    } else {
//                                        handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
//                                        Log.e(TAG, logMen.toString());
//
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onComplete() {
//
//                            }
//                        });
//            }
//        } else {
//            EventBus.getDefault().post(new FaceIdentityEvent());
//            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
//        }
//    }

    private void mapInit() {
        SafeCheck safeCheck = new SafeCheck();
        safeCheck.setURL(config.getString("ServerId"));
        paramsMap.put("daid", config.getString("daid"));
        paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
    }

    private void testNet() {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "test");
        RetrofitGenerator.getHeBeiApi().GeneralUpdata(map)
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
                            EventBus.getDefault().post(new NetworkEvent(true));
                        } else {
                            EventBus.getDefault().post(new NetworkEvent(false));
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new NetworkEvent(false));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void checkOnline() {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "checkOnline");
        RetrofitGenerator.getHeBeiApi().GeneralUpdata(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void CloseDoorRecord(String time) {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "closeDoor");
        map.put("time", time);
        RetrofitGenerator.getHeBeiApi().GeneralUpdata(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mdaoSession.insert(new ReUploadWithBsBean(null, "dataType=closeDoor" + "&time=" + url_timeformatter.format(new Date(System.currentTimeMillis())), null, 0));

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void alarmRecord() {
        EventBus.getDefault().post(new AlarmEvent());
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "alarm");
        map.put("alarmType", String.valueOf(1));
        map.put("time", formatter.format(new Date(System.currentTimeMillis())));
        RetrofitGenerator.getHeBeiApi().GeneralUpdata(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mdaoSession.insert(new ReUploadWithBsBean(null, "dataType=alarm&alarmType=1" + "&time=" + url_timeformatter.format(new Date(System.currentTimeMillis())), null, 0));
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void StateRecord() {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "temHum");
        map.put("tem", String.valueOf(last_mTemperature));
        map.put("hum", String.valueOf(last_mHumidity));
        map.put("time", formatter.format(new Date(System.currentTimeMillis())));
        RetrofitGenerator.getHeBeiApi().GeneralUpdata(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
