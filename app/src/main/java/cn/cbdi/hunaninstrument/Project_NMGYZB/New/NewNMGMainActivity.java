package cn.cbdi.hunaninstrument.Project_NMGYZB.New;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;

import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Alert.Alert_IP;
import cn.cbdi.hunaninstrument.Alert.Alert_Message;
import cn.cbdi.hunaninstrument.Alert.Alert_Password;
import cn.cbdi.hunaninstrument.Alert.Alert_Server;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.SceneKeeper;
import cn.cbdi.hunaninstrument.EventBus.FaceIdentityEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.DoorState.WarehouseDoor;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.Tool.UDPState;
import cn.cbdi.hunaninstrument.UI.NormalWindow;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Score;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Identify_failed;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Identify_success;
import static cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door.DoorState.State_Close;
import static cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door.DoorState.State_Open;


public class NewNMGMainActivity extends BaseActivity implements NormalWindow.OptionTypeListener {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat url_timeformatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");

    private Disposable disposableTips;

    private Intent intent;

    private NormalWindow normalWindow;


    Alert_Message alert_message = new Alert_Message(this);

    Alert_Server alert_server = new Alert_Server(this);

    Alert_IP alert_ip = new Alert_IP(this);

    Alert_Password alert_password = new Alert_Password(this);

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    GestureLibrary mGestureLib;

    @OnClick(R.id.lay_setting)
    void setting() {
        alert_password.show();
    }

    @OnClick(R.id.lay_network)
    void showMessage() {
        alert_message.showMessage();
    }

    @OnClick(R.id.lay_lock)
    void showoff() {
        List<ReUploadBean> reUploadList = mdaosession.loadAll(ReUploadBean.class);
        Log.e("sdsdsadsad",String.valueOf(reUploadList.size()));
        try {
            StringBuffer logMen = new StringBuffer();
            List<Keeper> keeperList = mdaosession.loadAll(Keeper.class);
            if (keeperList.size() > 0) {
                Set<String> list = new HashSet<>();
                for (Keeper keeper : keeperList) {
                    list.add(keeper.getName());
                }
                for (String name : list) {
                    logMen.append(name + "、");
                }
                logMen.deleteCharAt(logMen.length() - 1);
                ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕");
                Log.e(TAG, logMen.toString());

            } else {
                ToastUtils.showLong("该设备没有可使用的人脸特征");
                Log.e(TAG, logMen.toString());

            }
        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    Bitmap Scene_Bitmap;

    Bitmap Scene_headphoto;

    Bitmap headphoto;

    String faceScore;

    String CompareScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmain);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        UIReady();
        openService();
        Observable.interval(10, 300, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe((l) -> {
                    UDPState udp = new UDPState();
                    //设置通用参数：服务器地址，端口，设备ID，接口URL
                    udp.setPar("124.172.232.89", 8059, config.getString("daid"), "http://129.204.110.143:8031/");
                    float cpu = CJYHelper.getInstance(this).readCPUTem(0);
                    float gpu = CJYHelper.getInstance(this).readCPUTem(1);
                    if (WarehouseDoor.getInstance().getMdoorState().equals(Door.DoorState.State_Open)) {
                        udp.setState(0, (float) last_mTemperature, (float) last_mHumidity, cpu, gpu);
                    } else {
                        udp.setState(1, (float) last_mTemperature, (float) last_mHumidity, cpu, gpu);
                    }
                    udp.send();
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        fp.FaceIdentify_model();
    }


    @Override
    public void onResume() {
        super.onResume();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj));
        tv_daid.setText(config.getString("daid"));
        tv_info.setText("等待用户操作...");
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe((l) -> tv_time.setText(formatter.format(new Date(System.currentTimeMillis()))));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposableTips != null) {
            disposableTips.dispose();
        }
        stopService(intent);
    }

    private void UIReady() {

        setGestures();
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(5, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
        alert_ip.IpviewInit();
        alert_server.serverInit(() -> iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.iv_wifi)));
        alert_message.messageInit();
        alert_password.PasswordViewInit(() -> {
            normalWindow = new NormalWindow(NewNMGMainActivity.this);
            normalWindow.setOptionTypeListener(NewNMGMainActivity.this);
            normalWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content),
                    Gravity.CENTER, 0, 0);
        });
    }

    void openService() {
        intent = new Intent(NewNMGMainActivity.this, AppInit.getInstrumentConfig().getServiceName());
        startService(intent);
    }


    private void setGestures() {
        gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
        gestures.setGestureVisible(false);
        gestures.addOnGesturePerformedListener((overlayView, gesture) -> {
            ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
            if (predictions.size() > 0) {
                Prediction prediction = (Prediction) predictions.get(0);
                // 匹配的手势
                if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
                    if (prediction.name.equals("setting")) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }
                }
            }
        });
        if (mGestureLib == null) {
            mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
            mGestureLib.load();
        }
    }

    @Override
    public void onOptionType(Button view, int type) {
        normalWindow.dismiss();
        if (type == 1) {
            alert_server.show();
        } else if (type == 2) {
            if(Build.DEVICE.startsWith("Apollo7")){
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }else {
                alert_ip.show();
            }
        }
    }

    @Override
    public void onSetText(String Msg) {
        if (Msg.startsWith("SAM")) {
            ToastUtils.showLong(Msg);
        }
    }

    @Override
    public void onSetInfoAndImg(ICardInfo cardInfo, Bitmap bmp) {

    }



    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }


    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
        if (alert_message.Showing()) {
            alert_message.setICCardText(cardInfo.getUid());
            return;
        }
    }

    @Override
    public void onsetCardInfo(ICardInfo cardInfo) {
        if (alert_message.Showing()) {
            alert_message.setICCardText(cardInfo.cardId());
            return;
        }
        try {
            tv_info.setText(cardInfo.name() + "刷卡中，请稍后");
            Employer employer = mdaosession.queryRaw(Employer.class, "where CARD_ID = '" + cardInfo.cardId().toUpperCase() + "'").get(0);
            if (employer.getType()==1) {
                if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                    sp.greenLight();
                    cg_User1.setKeeper(new Keeper(null,cardInfo.cardId().toUpperCase(),cardInfo.name(),null,null,null,null,null));
                    cg_User1.setScenePhoto(fp.getGlobalBitmap());
                    tv_info.setText("仓管员" + cg_User1.getKeeper().getName() + "刷卡成功,等待第二位仓管员");
                    Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                            .compose(NewNMGMainActivity.this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    checkChange = d;
                                }

                                @Override
                                public void onNext(Long aLong) {
                                    checkRecord(cg_User1,2);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                    DoorOpenOperation.getInstance().doNext();
                } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                    cg_User2.setKeeper(new Keeper(null,cardInfo.cardId().toUpperCase(),cardInfo.name(),null,null,null,null,null));
                    cg_User2.setScenePhoto(fp.getGlobalBitmap());
                    if (cg_User1.getKeeper().getCardID().equals(cg_User2.getKeeper().getCardID())) {
                        sp.redLight();
                        tv_info.setText("请不要连续输入相同的管理员信息");
                        return;
                    } else {
                        if (checkChange != null) {
                            checkChange.dispose();
                        }
                        tv_info.setText("仓管员" + cg_User2.getKeeper().getName() + "刷卡成功,仓库已撤防");
                        sp.greenLight();
                        DoorOpenOperation.getInstance().doNext();
                        EventBus.getDefault().post(new PassEvent());
                        if (AppInit.getInstrumentConfig().isHongWai()) {
                            EventBus.getDefault().post(new OpenDoorEvent(true));
                        }
                        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj1));

                    }
                }  else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.TwoUnlock)) {
                    if (AppInit.getInstrumentConfig().isHongWai()) {
                        Lock.getInstance().setState(Lock.LockState.STATE_Lockup);
                        String closeDoorTime = formatter.format(new Date(System.currentTimeMillis()));
                        CloseDoorRecord(closeDoorTime);
                        EventBus.getDefault().post(new LockUpEvent());
                        Door.getInstance().setMdoorState(State_Close);
                    } else {
                        tv_info.setText("仓库门已解锁");
                    }

                    return;
                }
            } else if (employer.getType()==2) {
                xg_User.setKeeper(new Keeper(null,cardInfo.cardId().toUpperCase(),cardInfo.name(),null,null,null,null,null));
                xg_User.setScenePhoto(fp.getGlobalBitmap());
                checkRecord(xg_User,2);
            }

        } catch (IndexOutOfBoundsException e) {
            unknownUser.setKeeper(new Keeper(null,cardInfo.cardId().toUpperCase(),cardInfo.name(),null,null,null,null,null));
            unknownPeople(fp.getGlobalBitmap());
        }
    }


    @Override
    public void onBitmap(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        if (resultType.equals(Identify_success)) {
            Scene_Bitmap = bitmap;
        } else if (resultType.equals(FacePresenter.FaceResultType.headphotoIR)) {
            Scene_headphoto = bitmap;
        }

    }

    @Override
    public void onUser(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, User user) {
        if (resultType.equals(FacePresenter.FaceResultType.Identify_success)) {
            try {
                Keeper keeper = mdaosession.queryRaw(Keeper.class,
                        "where CARD_ID = '" + user.getUserInfo().toUpperCase() + "'").get(0);
                Employer employer = mdaosession.queryRaw(Employer.class,
                        "where CARD_ID = '" + user.getUserInfo().toUpperCase() + "'").get(0);
                if (employer.getType() == 1) {
                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                        cg_User1.setKeeper(keeper);
                        cg_User1.setScenePhoto(Scene_Bitmap);
                        cg_User1.setFaceRecognition(Integer.parseInt(faceScore));
                        cg_User1.setSceneHeadPhoto(Scene_headphoto);
                        tv_info.setText("仓管员" + cg_User1.getKeeper().getName() + "操作成功,请继续仓管员操作");
                        sp.greenLight();
                        DoorOpenOperation.getInstance().doNext();
                        Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .compose(NewNMGMainActivity.this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        checkChange = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        checkRecord(cg_User1,2);
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                        if (!keeper.getCardID().equals(cg_User1.getKeeper().getCardID())) {
//                        if (keeper.getCardID().equals(cg_User1.getKeeper().getCardID())) {
                            if (checkChange != null) {
                                checkChange.dispose();
                            }
                            cg_User2.setKeeper(keeper);
                            cg_User2.setScenePhoto(Scene_Bitmap);
                            cg_User2.setSceneHeadPhoto(Scene_headphoto);
                            cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
                            tv_info.setText("仓管员" + cg_User2.getKeeper().getName() + "操作成功,请等待...");
                            fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(), false, true);
                        } else {
                            sp.redLight();
                            tv_info.setText("请不要连续输入相同的管理员信息");
                            return;
                        }
                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.TwoUnlock)) {
                        if (AppInit.getInstrumentConfig().isHongWai()) {
                            Lock.getInstance().setState(Lock.LockState.STATE_Lockup);
                            String closeDoorTime = formatter.format(new Date(System.currentTimeMillis()));
                            CloseDoorRecord(closeDoorTime);
                            EventBus.getDefault().post(new LockUpEvent());
                            Door.getInstance().setMdoorState(State_Close);
                        } else {
                            tv_info.setText("仓库门已解锁");
                        }
                    }
                } else if (employer.getType() == 2) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    if (AppInit.getInstrumentConfig().XungengCanOpen()) {
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            if (!keeper.getCardID().equals(cg_User1.getKeeper().getCardID())) {
                                sp.greenLight();
                                cg_User2.setKeeper(keeper);
                                cg_User2.setScenePhoto(Scene_Bitmap);
                                cg_User2.setSceneHeadPhoto(Scene_headphoto);
                                cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
                                tv_info.setText("巡检员" + cg_User2.getKeeper().getName() + "操作成功,请等待...");
                                fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(), false, true);
                            } else {
                                sp.redLight();
                                tv_info.setText("请不要连续输入相同的管理员信息");
                                return;
                            }
                        } else {
                            xg_User.setKeeper(keeper);
                            xg_User.setScenePhoto(Scene_Bitmap);
                            checkRecord(xg_User,2);
                        }
                    } else {
                        xg_User.setKeeper(keeper);
                        xg_User.setScenePhoto(Scene_Bitmap);
                        checkRecord(xg_User,2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onText(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Identify_failed)) {
            tv_info.setText(text);
            sp.redLight();
        } else if (resultType.equals(Identify_success)) {
            faceScore = text;
        } else if (resultType.equals(IMG_MATCH_IMG_Score)) {
            CompareScore = text;
            OutputControlPresenter.getInstance().buzz(IOutputControl.Hex.H0);
            sp.greenLight();
            DoorOpenOperation.getInstance().doNext();
            EventBus.getDefault().post(new PassEvent());
            if (AppInit.getInstrumentConfig().isHongWai()) {
                fp.FaceSetNoAction();
                tv_info.setText("信息处理完毕,仓库门已解锁,20秒后才可重新上锁");
                Door.getInstance().setMdoorState(State_Open);
                Door.getInstance().doNext();
                Observable.timer(20, TimeUnit.SECONDS)
                        .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                fp.FaceIdentify_model();

                            }
                        });
            } else {
                tv_info.setText("信息处理完毕,仓库门已解锁");

            }
            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj1));
        }
    }

    @Override
    public void onLivenessModel(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, LivenessModel model) {
        if (resultType.equals(Identify_success)) {
            try {
                Keeper keeper = mdaosession.queryRaw(Keeper.class,
                        "where CARD_ID = '" + model.getUser().getUserInfo().toUpperCase() + "'").get(0);
//                if (keeper.getHeadphotoBW() == null) {
//                    keeper.setHeadphotoBW(FileUtils.bitmapToBase64(Scene_headphoto));
//                    mdaosession.insertOrReplace(keeper);
//                    fp.FaceRegOrUpdateByFeature(keeper.getName(), keeper.getCardID(), model.getFeature(), false);
//                }
            } catch (Exception e) {
                ToastUtils.showLong(e.toString());
            }
        }
    }


    private void checkRecord(SceneKeeper user, final int type) {
        OutputControlPresenter.getInstance().on12V_Alarm(false);
        final JSONObject checkRecordJson = new JSONObject();
        try {
            checkRecordJson.put("id", user.getKeeper().getCardID());
            checkRecordJson.put("name", user.getKeeper().getName());
            checkRecordJson.put("checkType", type);
            checkRecordJson.put("datetime", TimeUtils.getNowString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getNewNMGApi().withDataRr("checkRecord", config.getString("key"), checkRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String s = ParsingTool.extractMainContent(responseBody);
                            if (s.equals("true")) {
                                tv_info.setText("巡检员" + user.getKeeper().getName() + "巡检成功");
                            } else if (s.equals("false")) {
                                tv_info.setText("巡检失败");
                            } else if (s.equals("dataErr")) {
                                tv_info.setText("上传巡检数据失败");
                            } else if (s.equals("dataErr")) {
                                tv_info.setText("数据库操作有错");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接服务器,请检查网络,离线数据已保存");
                        mdaosession.insert(new ReUploadBean(null, "checkRecord", checkRecordJson.toString()));

                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }

                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cg_User1 = new SceneKeeper();
                        cg_User2 = new SceneKeeper();
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }

                    }
                });
    }


    private void unknownPeople(Bitmap bmp) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        final JSONObject unknownPeopleJson = new JSONObject();
        try {
            unknownPeopleJson.put("visitIdcard", unknownUser.getKeeper().getCardID());
            unknownPeopleJson.put("visitName", unknownUser.getKeeper().getName());
            unknownPeopleJson.put("photos", FileUtils.bitmapToBase64(bmp));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getNewNMGApi().withDataRr("saveVisit", config.getString("key"), unknownPeopleJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                        try {
                            String s = ParsingTool.extractMainContent(responseBody);
                            if (s.equals("true")) {
                                tv_info.setText("访问人" + unknownUser.getKeeper().getName() + "数据上传成功");
                            } else if (s.equals("false")) {
                                tv_info.setText("访问人上传失败");
                            } else if (s.equals("dataErr")) {
                                tv_info.setText("上传访问人数据失败");
                            } else if (s.equals("dbErr")) {
                                tv_info.setText("数据库操作有错");
                            }
                            unknownUser = new SceneKeeper();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        tv_info.setText("无法连接服务器,请检查网络,离线数据已保存");
                        unknownUser = new SceneKeeper();
                        mdaosession.insert(new ReUploadBean(null, "saveVisit", unknownPeopleJson.toString()));
                    }
                });

    }


    @Override
    public void OpenDoor() {
        final JSONObject OpenDoorJson = new JSONObject();
        try {
            OpenDoorJson.put("courIds1", "");
            OpenDoorJson.put("courIds2", "");
            OpenDoorJson.put("id1", cg_User1.getKeeper().getCardID());
            OpenDoorJson.put("id2", cg_User2.getKeeper().getCardID());
            OpenDoorJson.put("name1", cg_User1.getKeeper().getName());
            OpenDoorJson.put("name2", cg_User2.getKeeper().getName());
            OpenDoorJson.put("photo1", FileUtils.bitmapToBase64(cg_User1.getScenePhoto()));
            OpenDoorJson.put("photo2", FileUtils.bitmapToBase64(cg_User2.getScenePhoto()));
            OpenDoorJson.put("datetime", TimeUtils.getNowString());
            OpenDoorJson.put("state", "y");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getNewNMGApi().withDataRr("openDoorRecord", config.getString("key"), OpenDoorJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String s = ParsingTool.extractMainContent(responseBody);
                            if (s.equals("true")) {
                                try {
                                    if (OpenDoorJson.getString("state").equals("y")) {
                                        tv_info.setText("正常开门数据上传成功");
                                    } else {
                                        tv_info.setText("非法开门数据上传成功");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else if (s.equals("false")) {
                                tv_info.setText("开门数据上传失败");
                            } else if (s.equals("dataErr")) {
                                tv_info.setText("上传的json数据有错");
                            } else if (s.equals("dbErr")) {
                                tv_info.setText("数据库操作有错");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接服务器,请检查网络,离线数据已保存");
                        mdaosession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorJson.toString()));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cg_User1 = new SceneKeeper();
                        cg_User2 = new SceneKeeper();
                    }
                });
    }

    private void CloseDoorRecord(String time) {
        JSONObject CloseDoorRecordJson = new JSONObject();
        try {
            CloseDoorRecordJson.put("datetime", formatter.format(new Date(System.currentTimeMillis())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getNewNMGApi().withDataRr("closeDoorRecord", config.getString("key"), CloseDoorRecordJson.toString())
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
                        mdaosession.insert(new ReUploadBean(null, "closeDoorRecord", CloseDoorRecordJson.toString()));

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetFaceIdentityEvent(FaceIdentityEvent event) {
        if (isForeground(AppInit.getContext(), NewNMGMainActivity.class.getName())) {
            fp.FaceIdentify_model();
        }

    }

    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className))
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName()))
                return true;
        }
        return false;
    }
}
