package cn.cbdi.hunaninstrument.Project_YanCheng;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.FaceIdentityEvent;
import cn.cbdi.hunaninstrument.EventBus.FingerPrintIdentityEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Project_XinWeiGuan.ParsingTool;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.DoorState.WarehouseDoor;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbdi.hunaninstrument.greendao.ReUploadBeanDao;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.SignUtils;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.presenter.FingerPrintPresenter;
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

import static cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door.DoorState.State_Open;

public class YanChengService extends Service implements IOutputControlView {
    private String TAG = YanChengService.class.getSimpleName();

    OutputControlPresenter sp = OutputControlPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    int last_mTemperature = 0;

    int last_mHumidity = 0;

    String THSwitchValue;

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
        Observable.timer(20, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> syncData());
        reUpload();

        Observable.interval(0, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe((l) -> testNet());

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
                if (state.equals(State_Open)) {
                    if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
                        Lock.getInstance().doNext();
                        alarmRecord();
                    }
                }
            }

        } else {
            if (!WarehouseDoor.getInstance().getMdoorState().equals(state)) {
                if (state.equals(State_Open)) {
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
    int max_Reupload = 200;
    int count_Reupload = 0;

    private void reUpload() {
        final ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        count_Reupload = list.size();
        for (final ReUploadBean bean : list) {
            RetrofitGenerator.getXinWeiGuanApi().withDataRr(bean.getMethod(), config.getString("key"), bean.getContent())
                    .subscribeOn(Schedulers.single())
                    .unsubscribeOn(Schedulers.single())
                    .observeOn(Schedulers.single())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull ResponseBody responseBody) {
                            Log.e("信息提示", bean.getMethod());
                            reUploadBeanDao.delete(bean);


                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e("信息提示error", bean.getMethod());
                            if (count_Reupload > max_Reupload) {
                                count_Reupload--;
                                reUploadBeanDao.delete(bean);
                                Log.e("信息提示error", bean.getMethod());

                            }

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        }
    }

    private void syncData() {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("personType", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("updatePerson", config.getString("key"), obj.toString())
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

                            mdaoSession.getEmployerDao().deleteAll();
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
                        try {
                            obj.put("personType", "1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RetrofitGenerator.getXinWeiGuanApi().withDataRr("updatePerson", config.getString("key"), obj.toString())
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

    int count = 0;

    StringBuffer logMen;

    private void getPic() {
//        if (config.getBoolean("wzwPic", true)) {
//            mdaoSession.insertOrReplace(new Employer("441302199308100538", 1));
//            Bitmap wzwbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user);
//            if (FacePresenter.getInstance().FaceRegByBase64("王振文","441302199308100538",FileUtils.bitmapToBase64(wzwbitmap))) {
//                User user = FacePresenter.getInstance().GetUserByUserName("王振文");
//                Keeper keeper = new Keeper("441302199308100538",
//                        "王振文", null, FileUtils.bitmapToBase64(wzwbitmap), null,
//                        user.getUserId(), user.getFeature());
//                mdaoSession.getKeeperDao().insertOrReplace(keeper);
//
//            }
//
//            mdaoSession.insertOrReplace(new Employer("44128219830820403X", 1));
//            Bitmap songBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.song);
//            if (FacePresenter.getInstance().FaceRegByBase64("彭艺煊","44128219830820403X",FileUtils.bitmapToBase64(songBitmap))) {
//                User user = FacePresenter.getInstance().GetUserByUserName("彭艺煊");
//                Keeper keeper = new Keeper("44128219830820403X",
//                        "彭艺煊", null, FileUtils.bitmapToBase64(songBitmap), null,
//                        user.getUserId(), user.getFeature());
//                mdaoSession.getKeeperDao().insertOrReplace(keeper);
//
//            }
//        }


        logMen = new StringBuffer();
        count = 0;
        List<Employer> employers = mdaoSession.loadAll(Employer.class);
        if (employers.size() > 0) {
            for (Employer employer : employers) {
                RetrofitGenerator.getXinWeiGuanApi().queryPersonInfo("recentPic", config.getString("key"), employer.getCardID())
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
                                    String s = ParsingTool.extractMainContent(responseBody);
                                    JSONObject jsonObject = new JSONObject(s);
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

    private void testNet() {
        RetrofitGenerator.getXinWeiGuanApi().noData("testNet", config.getString("key"))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String s = ParsingTool.extractMainContent(responseBody);
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


    private void CloseDoorRecord(String time) {
        JSONObject CloseDoorRecordJson = new JSONObject();
        try {
            CloseDoorRecordJson.put("datetime", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("closeDoorRecord", config.getString("key"), CloseDoorRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "closeDoorRecord", CloseDoorRecordJson.toString()));

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
            alarmRecordJson.put("datetime", TimeUtils.getNowString());// 报警时间
            alarmRecordJson.put("alarmType", String.valueOf(1));  //报警类型
            alarmRecordJson.put("alarmValue", String.valueOf(0));  //报警值
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("alarmRecord", config.getString("key"), alarmRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "alarmRecord", alarmRecordJson.toString()));
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
            if (Door.getInstance().getMdoorState().equals(State_Open)) {
                jsonObject.put("state", "0");
            } else {
                jsonObject.put("state", "1");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getXinWeiGuanApi().withDataRr("stateRecord", config.getString("key"), jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == 0x123) {
//                syncData();
//                Log.e("result", "0x123");
//            } else if (msg.what == 0x234) {
//                Log.e("result", "0x234");
//                try {
//                    String filepath = Environment.getExternalStorageDirectory() + File.separator + "offline_data";
//                    File file_dir = new File(filepath);
//                    String excel_filepath = filepath + File.separator + "人员数据表.xls";
//                    File excel_file = new File(excel_filepath);
//                    if (!excel_file.exists()) {
//                        ToastUtils.showLong("本地没有找到离线数据包");
//                        EventBus.getDefault().post(new FaceIdentityEvent());
//                        return;
//                    }
//                    FileInputStream inputStream = new FileInputStream(excel_file);
//                    Workbook workbook = Workbook.getWorkbook(inputStream);
//                    mdaoSession.deleteAll(Employer.class);
//                    for (Sheet sheet : workbook.getSheets()) {
//                        int sheetRows = sheet.getRows();
//                        for (int i = 1; i < sheetRows; i++) {
//                            String idnum = sheet.getCell(1, i).getContents();
//                            String name = sheet.getCell(2, i).getContents();
//                            String type = sheet.getCell(3, i).getContents();
//                            String photo_path = sheet.getCell(4, i).getContents();
//                            Bitmap photo = BitmapFactory.decodeFile(filepath + File.separator + photo_path);
//                            String ps = FileUtils.bitmapToBase64(photo);
//                            mdaoSession.insertOrReplace(new Employer(idnum.toUpperCase(), Integer.parseInt(type)));
//                            try {
//                                Keeper keeper = mdaoSession.queryRaw(Keeper.class, "where CARD_ID = '" + idnum.toUpperCase() + "'").get(0);
//                                if (!TextUtils.isEmpty(ps) && keeper.getHeadphoto().length() != ps.length()) {
//
//                                    FacePresenter.getInstance().FaceUpdate(photo, name, new UserInfoManager.UserInfoListener() {
//                                        public void updateImageSuccess(Bitmap bitmap) {
//                                            keeper.setHeadphoto(ps);
//                                            keeper.setHeadphotoBW(null);
//                                            mdaoSession.getKeeperDao().insertOrReplace(keeper);
//                                        }
//
//                                        public void updateImageFailure(String message) {
//                                            Log.e(TAG, message);
//                                        }
//                                    });
//                                }
//                            } catch (IndexOutOfBoundsException e) {
//                                if (!TextUtils.isEmpty(ps)) {
//                                    if (FacePresenter.getInstance().FaceRegByBase64(name, idnum.toUpperCase(), ps)) {
//                                        User user = FacePresenter.getInstance().GetUserByUserName(name);
//                                        Keeper keeper = new Keeper(idnum.toUpperCase(),
//                                                name, ps, null, null,
//                                                user.getUserId(), user.getFeature());
//                                        mdaoSession.getKeeperDao().insertOrReplace(keeper);
//                                        Log.e("myface", name + "人脸特征已存");
//
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//                    workbook.close();
//                    com.blankj.utilcode.util.FileUtils.deleteDir(file_dir);
//                    EventBus.getDefault().post(new FaceIdentityEvent());
//                    try {
//                        StringBuffer logMen = new StringBuffer();
//
//                        List<Keeper> keeperList = mdaoSession.loadAll(Keeper.class);
//                        if (keeperList.size() > 0) {
//                            Set<String> list = new HashSet<>();
//                            for (Keeper keeper : keeperList) {
//                                list.add(keeper.getName());
//                            }
//                            for (String name : list) {
//                                logMen.append(name + "、");
//                            }
//                            logMen.deleteCharAt(logMen.length() - 1);
//                            ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕");
//                            Log.e(TAG, logMen.toString());
//
//                        } else {
//                            ToastUtils.showLong("该设备没有可使用的人脸特征");
//                            Log.e(TAG, logMen.toString());
//
//                        }
//                    }catch (Exception e){
//                        ToastUtils.showLong(e.toString());
//                    }
//
//                } catch (Exception e) {
//                    ToastUtils.showLong("无法解析离线数据包，请按照格式制作离线格式包");
//                    EventBus.getDefault().post(new FaceIdentityEvent());
//                }
//            }
//            super.handleMessage(msg);
//        }
//    };
//
//    public void testTcp(String ServerId) {
//        int firstColon = ServerId.indexOf(":");
//        int lastColon = ServerId.lastIndexOf(":");
//        int lastSlash = ServerId.lastIndexOf("/");
//        String ip = ServerId.substring(firstColon + 3, lastColon);
//        int port = Integer.valueOf(ServerId.substring(lastColon + 1, lastSlash));
//        Log.e("ip", ip);
//        Log.e("port", String.valueOf(port));
//
//
//        new Thread(() -> {
//            Socket connect = new Socket();
//            try {
//                if (ip == null || ip.trim().equals("")) {
//                    handler.sendEmptyMessage(0x123);
//                }
//                connect.connect(new InetSocketAddress(ip, port), 100);
//                if (connect.isConnected()) {
//                    handler.sendEmptyMessage(0x123);
//                } else {
//                    handler.sendEmptyMessage(0x234);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    connect.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }


}
