package cn.cbdi.hunaninstrument.Project_HeNan;

import android.content.Intent;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;

import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import cn.cbdi.hunaninstrument.Bean.DataFlow.UpCheckRecordData;
import cn.cbdi.hunaninstrument.Bean.DataFlow.UpOpenDoorData;
import cn.cbdi.hunaninstrument.Bean.DataFlow.UpPersonRecordData;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadWithBsBean;
import cn.cbdi.hunaninstrument.Bean.SceneKeeper;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.Tool.SafeCheck;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;
import cn.cbdi.hunaninstrument.UI.NormalWindow;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_False;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Score;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_True;
import static cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door.DoorState.State_Close;

public class HNMainActivity extends BaseActivity implements NormalWindow.OptionTypeListener {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private SimpleDateFormat url_timeformatter = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");

    private Disposable disposableTips;

    HashMap<String, String> paramsMap = new HashMap<String, String>();

    private Intent intent;

    private NormalWindow normalWindow;

    ServerConnectionUtil connectionUtil = new ServerConnectionUtil();

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
    void ds() {
//        if (AppInit.getInstrumentConfig().MenKongSuo()) {
//            OutputControlPresenter.getInstance().onElectricLock(IOutputControl.Hex.HA, true);
//        }

    }


    Bitmap headphoto;

    String CompareScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmain);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        UIReady();
        openService();
    }

    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
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
        mapInit();
        setGestures();
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(15, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
        alert_ip.IpviewInit();
        alert_server.serverInit(() -> iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.iv_wifi)));
        alert_message.messageInit();
        alert_password.PasswordViewInit(() -> {
            normalWindow = new NormalWindow(HNMainActivity.this);
            normalWindow.setOptionTypeListener(HNMainActivity.this);
            normalWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content),
                    Gravity.CENTER, 0, 0);
        });
    }

    void openService() {
        intent = new Intent(HNMainActivity.this, AppInit.getInstrumentConfig().getServiceName());
        startService(intent);
    }

    private void mapInit() {
        SafeCheck safeCheck = new SafeCheck();
        safeCheck.setURL(config.getString("ServerId"));
        paramsMap.put("daid", config.getString("daid"));
        paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
        Log.e("pass", paramsMap.get("pass"));

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
            alert_ip.show();
        }
    }

    @Override
    public void onSetText(String Msg) {
        if (Msg.startsWith("SAM")) {
            ToastUtils.showLong(Msg);
        }
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }

    @Override
    public void onSetInfoAndImg(ICardInfo cardInfo, Bitmap bmp) {
        try {
            mdaosession.queryRaw(Employer.class, "where CARD_ID = '" + cardInfo.cardId().toUpperCase() + "'").get(0);
            cardOperation(cardInfo.cardId().toUpperCase(), cardInfo.name(), bmp);
        } catch (IndexOutOfBoundsException e) {
            HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
            map.put("dataType", "queryPersion");
            map.put("id", cardInfo.cardId());
            RetrofitGenerator.getHeNanApi().GeneralPersionInfo(AppInit.getInstrumentConfig().getPersonInfoPrefix()
                    .substring(0, AppInit.getInstrumentConfig().getPersonInfoPrefix().length() - 1), map)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<String>(this) {
                        @Override
                        public void onNext(String s) {
                            try {
                                if (s.equals("false")) {
                                    Keeper inside_keeper = new Keeper();
                                    inside_keeper.setName(cardInfo.name());
                                    inside_keeper.setCardID(cardInfo.cardId());
                                    unknownUser.setKeeper(inside_keeper);
                                    unknownPeople(fp.getGlobalBitmap());
                                    tv_info.setText("系统查无此人");
                                    sp.redLight();
                                } else if (s.startsWith("true")) {
                                    if (s.split("\\|").length > 1) {
                                        String type = s.split("\\|")[1];
                                        mdaosession.insertOrReplace(new Employer(cardInfo.cardId(), Integer.valueOf(type)));
                                        cardOperation(cardInfo.cardId().toUpperCase(), cardInfo.name(), bmp);
                                    }
                                } else if (s.equals("noUnitId")) {
                                    sp.redLight();
                                    tv_info.setText("该设备还未在系统上备案");
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                tv_info.setText("Exception");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            Keeper inside_keeper = new Keeper();
                            inside_keeper.setName(cardInfo.name());
                            inside_keeper.setCardID(cardInfo.cardId());
                            unknownUser.setKeeper(inside_keeper);
                            unknownPeople(fp.getGlobalBitmap());
                            tv_info.setText("系统查无此人");
                            sp.redLight();
                        }
                    });
        }
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

    }

    private void cardOperation(String id, String name, Bitmap bmp) {
        try {
            Keeper mKeeper = new Keeper();
            Employer employer = mdaosession.queryRaw(Employer.class,
                    "where CARD_ID = '" + id + "'").get(0);

//            bmp = BitmapFactory.decodeResource(getResources(), cn.cbsd.cjyfunctionlib.R.drawable.user1);
            if (employer.getType() == 1) {
                if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                    mKeeper.setCardID(id);
                    mKeeper.setName(name);
                    cg_User1.setKeeper(mKeeper);
                    cg_User1.setScenePhoto(FacePresenter.getInstance().getGlobalBitmap());
                    tv_info.setText("仓管员" + cg_User1.getKeeper().getName() + "打卡,正在进行人脸认证");
                    FacePresenter.getInstance().FaceVerify(bmp);
                } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                    if (!id.equals(cg_User1.getKeeper().getCardID())) {
                        if (checkChange != null) {
                            checkChange.dispose();
                        }
                        mKeeper.setCardID(id);
                        mKeeper.setName(name);
                        cg_User2.setKeeper(mKeeper);
                        cg_User2.setScenePhoto(FacePresenter.getInstance().getGlobalBitmap());
                        tv_info.setText("仓管员" + cg_User2.getKeeper().getName() + "打卡,正在进行人脸认证");
                        FacePresenter.getInstance().FaceVerify(bmp);
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
                        if (!id.equals(cg_User1.getKeeper().getCardID())) {
                            mKeeper.setCardID(id);
                            mKeeper.setName(name);
                            cg_User2.setKeeper(mKeeper);
                            cg_User2.setScenePhoto(FacePresenter.getInstance().getGlobalBitmap());
                            tv_info.setText("巡检员" + cg_User2.getKeeper().getName() + "打卡,正在进行人脸认证");
                            FacePresenter.getInstance().FaceVerify(bmp);

                        } else {
                            sp.redLight();
                            tv_info.setText("请不要连续输入相同的管理员信息");
                            return;
                        }
                    } else {
                        mKeeper.setCardID(id);
                        mKeeper.setName(name);
                        cg_User1.setKeeper(mKeeper);
                        cg_User1.setScenePhoto(FacePresenter.getInstance().getGlobalBitmap());
                        checkRecord(2);
                    }
                } else {
                    mKeeper.setCardID(id);
                    mKeeper.setName(name);
                    cg_User1.setKeeper(mKeeper);
                    cg_User1.setScenePhoto(FacePresenter.getInstance().getGlobalBitmap());
                    checkRecord(2);
                }
            } else if (employer.getType() == 3) {
                if (checkChange != null) {
                    checkChange.dispose();
                }
                mKeeper.setCardID(id);
                mKeeper.setName(name);
                cg_User1.setKeeper(mKeeper);
                cg_User1.setScenePhoto(FacePresenter.getInstance().getGlobalBitmap());
                checkRecord(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBitmap(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        if (resultType.equals(IMG_MATCH_IMG_True)) {
            if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                cg_User1.setSceneHeadPhoto(bitmap);
            } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                cg_User2.setSceneHeadPhoto(bitmap);
            }
        }
    }

    @Override
    public void onUser(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, User user) {

    }

    @Override
    public void onText(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(IMG_MATCH_IMG_True)) {
            if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                tv_info.setText(cg_User1.getKeeper().getName() + "人脸认证通过");
                DoorOpenOperation.getInstance().doNext();
                Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                        .compose(HNMainActivity.this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                checkChange = d;
                            }

                            @Override
                            public void onNext(Long aLong) {
                                checkRecord(2);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                tv_info.setText(cg_User2.getKeeper().getName() + "人脸认证通过");
                fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(), true, true);
            }
        } else if (resultType.equals(IMG_MATCH_IMG_False)) {
            tv_info.setText(text);
        } else if (resultType.equals(FacePresenter.FaceResultType.verify_failed)) {
            tv_info.setText(text);

        } else if (resultType.equals(IMG_MATCH_IMG_Score)) {
            if (action.equals(FacePresenter.FaceAction.IMG_TO_IMG)) {
                CompareScore = text;
                if (AppInit.getInstrumentConfig().MenKongSuo()) {
                    OutputControlPresenter.getInstance().onElectricLock(IOutputControl.Hex.HA, true);
                }
                OutputControlPresenter.getInstance().buzz(IOutputControl.Hex.H0);
                sp.greenLight();
                DoorOpenOperation.getInstance().doNext();
                EventBus.getDefault().post(new PassEvent());
                tv_info.setText("信息处理完毕,仓库门已解锁");
                iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj1));
            } else {
                if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.Locking)) {
                    cg_User1.setFaceRecognition(Integer.parseInt(text));
                } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                    cg_User2.setFaceRecognition(Integer.parseInt(text));
                }
            }
        }
    }

    @Override
    public void onLivenessModel(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, LivenessModel model) {

    }

    private void checkRecord(int type) {
        OutputControlPresenter.getInstance().on12V_Alarm(false);
        connectionUtil.post(config.getString("ServerId")
                        + AppInit.getInstrumentConfig().getUpDataPrefix()
                        + "dataType=checkRecord"
                        + "&daid=" + config.getString("daid")
                        + "&checkType=" + type,
                config.getString("ServerId"),
                new UpCheckRecordData().toCheckRecordData(cg_User1.getKeeper().getCardID(), cg_User1.getScenePhoto(), cg_User1.getKeeper().getName()).toByteArray(),
                (response) -> {
                    if (response != null) {
                        if (response.startsWith("true")) {
                            tv_info.setText("巡检员" + cg_User1.getKeeper().getName() + "巡检成功");
                            sp.greenLight();
                        } else {
                            tv_info.setText("巡检数据上传失败");
                        }

                        cg_User1 = new SceneKeeper();
                        cg_User2 = new SceneKeeper();
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }
                    } else {
                        tv_info.setText("巡检数据上传失败,请检查网络,离线数据已保存");
                        mdaosession.insert(new ReUploadWithBsBean(null, "dataType=checkRecord", new UpCheckRecordData().toCheckRecordData(cg_User1.getKeeper().getCardID(), cg_User1.getScenePhoto(), cg_User1.getKeeper().getName()).toByteArray(),
                                type));
                        sp.redLight();
                        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
                        }
                    }
                });

    }

    UpPersonRecordData upPersonRecordData = new UpPersonRecordData();

    private void unknownPeople(Bitmap bmp) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        headphoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        upPersonRecordData.setPic(outputStream.toByteArray());
        connectionUtil.post(config.getString("ServerId") + AppInit.getInstrumentConfig().getUpDataPrefix() + "dataType=persionRecord" + "&daid=" + config.getString("daid"),
                config.getString("ServerId"),
                upPersonRecordData.toPersonRecordData(unknownUser.getKeeper().getCardID(), bmp, unknownUser.getKeeper().getName()).toByteArray(),
                (response) -> {
                    if (response != null) {
                        if (response.startsWith("true")) {
                            tv_info.setText("访问人" + unknownUser.getKeeper().getName() + "数据上传成功");
                        } else {
                            tv_info.setText("访问人上传失败");
                        }
                    } else {
                        tv_info.setText("访问人上传失败,请检查网络,离线数据已保存");
                        mdaosession.insert(new ReUploadWithBsBean(null, "dataType=persionRecord", upPersonRecordData.toPersonRecordData(unknownUser.getKeeper().getCardID(), bmp, unknownUser.getKeeper().getName()).toByteArray(), 0));
                        unknownUser = new SceneKeeper();
                    }
                });

    }

    @Override
    public void OpenDoor() {
        connectionUtil.post(config.getString("ServerId")
                        + AppInit.getInstrumentConfig().getUpDataPrefix()
                        + "dataType=openDoor"
                        + "&daid=" + config.getString("daid")
                        + "&faceRecognition1="
                        + (cg_User1.getFaceRecognition() + 100)
                        + "&faceRecognition2="
                        + (cg_User2.getFaceRecognition() + 100)
                        + "&faceRecognition3="
                        + (CompareScore + 100),
                config.getString("ServerId"),
                new UpOpenDoorData().toOpenDoorData((byte) 0x01,
                        cg_User1.getKeeper().getCardID(),
                        cg_User1.getKeeper().getName(),
                        cg_User1.getScenePhoto(),
                        cg_User2.getKeeper().getCardID(),
                        cg_User2.getKeeper().getName(),
                        cg_User2.getScenePhoto()).toByteArray(),
                (s) -> {
                    if (s != null) {
                        tv_info.setText("开门记录已上传到服务器");
                        sp.greenLight();
                    } else {
                        tv_info.setText("开门记录无法上传,请检查网络,离线数据已保存");
                        sp.redLight();
                        mdaosession.insertOrReplace(new ReUploadWithBsBean(null, "dataType=openDoor"
                                + "&daid=" + config.getString("daid")
                                + "&faceRecognition1="
                                + (cg_User1.getFaceRecognition() + 100)
                                + "&faceRecognition2="
                                + (cg_User2.getFaceRecognition() + 100)
                                + "&faceRecognition3="
                                + (CompareScore + 100), new UpOpenDoorData().toOpenDoorData((byte) 0x01,
                                cg_User1.getKeeper().getCardID(),
                                cg_User1.getKeeper().getName(),
                                cg_User1.getScenePhoto(),
                                cg_User2.getKeeper().getCardID(),
                                cg_User2.getKeeper().getName(),
                                cg_User2.getScenePhoto()).toByteArray(), 0));
                    }
                    cg_User1 = new SceneKeeper();
                    cg_User2 = new SceneKeeper();
                });
    }

    private void CloseDoorRecord(String time) {
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "closeDoor");
        map.put("time", time);
        RetrofitGenerator.getHeNanApi().GeneralUpdata(AppInit.getInstrumentConfig().getUpDataPrefix()
                .substring(0, AppInit.getInstrumentConfig().getUpDataPrefix().length() - 1), map)
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
                        mdaosession.insert(new ReUploadWithBsBean(null, "dataType=closeDoor" + "&time=" + url_timeformatter.format(new Date(System.currentTimeMillis())), null, 0));

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


}
