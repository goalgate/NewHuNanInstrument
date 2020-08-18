package cn.cbdi.hunaninstrument.Project_HuNan;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.RebootEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.DoorState.WarehouseDoor;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;
import cn.cbdi.hunaninstrument.Tool.UDPRun;
import cn.cbdi.hunaninstrument.Tool.UDPState;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.hunaninstrument.greendao.ReUploadBeanDao;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.SignUtils;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.CardInfoBean;
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


public class HuNanService extends Service implements IOutputControlView {

    private String TAG = HuNanService.class.getSimpleName();

    OutputControlPresenter sp = OutputControlPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    int last_mTemperature = 0;

    int last_mHumidity = 0;

    String THSwitchValue;

    Disposable rx_delay;

    Disposable unlock_noOpen;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Md5", SignUtils.getSignMd5Str(AppInit.getInstance()));
        sp.SwitchPresenterSetView(this);
        EventBus.getDefault().register(this);
        Observable.timer(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> syncData());
        reUpload();
        sp.readHum(5, true);
        Observable.interval(40, 600, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> testNet());
        Observable.interval(0, AppInit.getInstrumentConfig().getCheckOnlineTime(), TimeUnit.MINUTES)
                .observeOn(Schedulers.io())
                .subscribe((l) -> CheckOnline());
        Observable.interval(10, 600, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> StateRecord());

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDoorState(Door.DoorState state) {
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

    @Override
    public void onTemHum(int temperature, int humidity, String THSwitchValue) {
        this.THSwitchValue = THSwitchValue;
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
    public void onSwitchValue(String Value) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        Lock.getInstance().setState(Lock.LockState.STATE_Unlock);
        Lock.getInstance().doNext();
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

    private Handler handler = new Handler();


    private void reUpload() {
        final ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        for (final ReUploadBean bean : list) {
            RetrofitGenerator.getHnmbyApi().withDataRs(bean.getMethod(), config.getString("key"), bean.getContent())
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
        RetrofitGenerator.getHnmbyApi().syncPersonInfo("updatePersion", config.getString("key"), 3)
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
                        FacePresenter.getInstance().FaceIdentify_model();

                    }

                    @Override
                    public void onComplete() {
                        RetrofitGenerator.getHnmbyApi().syncPersonInfo("updatePersion", config.getString("key"), 2)
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
                                        FacePresenter.getInstance().FaceIdentify_model();

                                    }

                                    @Override
                                    public void onComplete() {
                                        RetrofitGenerator.getHnmbyApi().syncPersonInfo("updatePersion", config.getString("key"), 1)
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
                                                        FacePresenter.getInstance().FaceIdentify_model();
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

    private void getPic() {
//        if (config.getBoolean("wzwPic", true)) {
//            mdaoSession.insertOrReplace(new Employer("441302199308100538", 1));
//            Bitmap wzwbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wzw);
//            if (FacePresenter.getInstance().FaceRegByBase64("王振文", "441302199308100538", FileUtils.bitmapToBase64(wzwbitmap))) {
//                User user = FacePresenter.getInstance().GetUserByUserName("王振文");
//                Keeper keeper = new Keeper("441302199308100538",
//                        "王振文", FileUtils.bitmapToBase64(wzwbitmap), null, null,
//                        user.getUserId(), user.getFeature());
//                mdaoSession.getKeeperDao().insertOrReplace(keeper);
//                Log.e("myface", "王振文" + "人脸特征已存");
//
//            }
//        }
        logMen = new StringBuffer();
        count = 0;
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            for (Employer employer : employers) {
                RetrofitGenerator.getHnmbyApi()
                        .recentPic("recentPic", config.getString("key"), employer.getCardID())
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
                                    if (count == employers.size()) {
                                        FacePresenter.getInstance().FaceIdentify_model();
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
                                        FacePresenter.getInstance().FaceIdentify_model();
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
                                    FacePresenter.getInstance().FaceIdentify_model();
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
            FacePresenter.getInstance().FaceIdentify_model();
            handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));
        }
    }


    boolean REUP = false;

    private void testNet() {
        RetrofitGenerator.getHnmbyApi().withDataRs("testNet", config.getString("key"), null)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            EventBus.getDefault().post(new NetworkEvent(true));
                            if (!REUP) {
                                reUpload();
                                REUP = true;
                            }
                        } else {
                            EventBus.getDefault().post(new NetworkEvent(false));
                            REUP = false;

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


    private void CheckOnline() {
        RetrofitGenerator.getHnmbyApi().withDataRs("checkOnline", config.getString("key"), null)
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
        final JSONObject CloseDoorRecordJson = new JSONObject();
        try {
            CloseDoorRecordJson.put("datetime", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("closeDoorRecord", config.getString("key"), CloseDoorRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "closeDoorRecord", CloseDoorRecordJson.toString()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void StateRecord() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("datetime", TimeUtils.getNowString());
            jsonObject.put("switching", THSwitchValue);
            jsonObject.put("temperature", last_mTemperature);
            jsonObject.put("humidity", last_mHumidity);
            if (WarehouseDoor.getInstance().getMdoorState().equals(Door.DoorState.State_Open)) {
                jsonObject.put("state", "0");
            } else {
                jsonObject.put("state", "1");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("stateRecord", config.getString("key"), jsonObject.toString())
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

    private void alarmRecord() {
        EventBus.getDefault().post(new AlarmEvent());
        final JSONObject alarmRecordJson = new JSONObject();
        try {
            alarmRecordJson.put("datetime", TimeUtils.getNowString());
            alarmRecordJson.put("alarmType", String.valueOf(1));
            alarmRecordJson.put("alarmValue", String.valueOf(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RetrofitGenerator.getHnmbyApi().withDataRs("alarmRecord", config.getString("key"), alarmRecordJson.toString())
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull String s) {

            }

            @Override
            public void onError(@NonNull Throwable e) {
                mdaoSession.insert(new ReUploadBean(null, "alarmRecord", alarmRecordJson.toString()));
            }

            @Override
            public void onComplete() {

            }
        });
    }

}
