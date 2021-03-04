package cn.cbdi.hunaninstrument.Project_HuNan.MVPTest.Presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cbsd.mvphelper.mvplibrary.mvpforView.MVPBasePresenter;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.SceneKeeper;
import cn.cbdi.hunaninstrument.EventBus.PassEvent;
import cn.cbdi.hunaninstrument.Project_HuNan.MVPTest.Activity.HuNanMainActivity;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.Tool.MySocketHelper;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_Card.presenter.IDCardPresenter;
import cn.cbsd.cjyfunctionlib.Func_Card.view.IIDCardView;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.view.IFaceView;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Func_WebSocket.ServerManager;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_Score;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Identify_failed;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Identify_success;

public class HuNanMainPresenter extends MVPBasePresenter<HuNanMainActivity> implements IFaceView, IIDCardView {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String TAG = cn.cbdi.hunaninstrument.Project_HuNan.HuNanMainActivity.class.getSimpleName();

    private Disposable checkChange;

    private byte[] mFeatures;

    private DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    private OutputControlPresenter sp = OutputControlPresenter.getInstance();

    private FacePresenter fp = FacePresenter.getInstance();

    private IDCardPresenter idp = IDCardPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    private Intent intent;

    private SceneKeeper cg_User1 = new SceneKeeper();

    private SceneKeeper cg_User2 = new SceneKeeper();

    private SceneKeeper unknownUser = new SceneKeeper();

    private Bitmap Scene_Bitmap;

    private Bitmap Scene_headphoto;

    private Bitmap headphoto;

    private String faceScore;

    private String CompareScore;

    @Override
    public void onCreate() {
        super.onCreate();
        sp.Open();
        idp.idCardOpen(AppInit.getContext());
        openService();
        ServerManager.getInstance().Start(4545, new MySocketHelper(getV()));


    }

    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), getV().previewView, getV().previewView1, getV().textureView);

    }

    @Override
    public void onResume() {
        super.onResume();
        idp.IDCardPresenterSetView(this);
        fp.useRGBCamera(false);
        Observable.timer(1, TimeUnit.SECONDS)
                .compose(getV().<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    AppInit.getInstrumentConfig().readCard();
                });
        fp.FacePresenterSetView(this);

        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
        getV().iv_lock.setImageBitmap(BitmapFactory.decodeResource(getV().getResources(), R.drawable.iv_mj));
        getV().infoSet("等待用户操作...");

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getV().<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe((l) -> {
                    getV().tv_time.setText(formatter.format(new Date(System.currentTimeMillis())));
                });
    }

    @Override
    public void onRestart() {
        super.onRestart();
        FacePresenter.getInstance().FaceIdentify_model();
    }

    @Override
    public void onPause() {
        super.onPause();
        fp.FacePresenterSetView(null);
        idp.IDCardPresenterSetView(null);
        AppInit.getInstrumentConfig().stopReadCard();
        sp.WhiteLight(false);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sp.WhiteLight(false);
        idp.idCardClose();
        ServerManager.getInstance().Stop();
    }

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
        if (getV().getAlert_message().Showing()) {
            getV().getAlert_message().setICCardText(cardInfo.cardId());
            return;
        }
        try {
            mdaosession.queryRaw(Employer.class, "where CARD_ID = '" + cardInfo.cardId().toUpperCase() + "'").get(0);
//            try {
//                mdaosession.queryRaw(Keeper.class, "where CARD_ID = '" + cardInfo.cardId().toUpperCase() + "'").get(0);
//                tv_info.setText("等待人脸比对结果返回");
//                MediaHelper.play(MediaHelper.Text.waiting);
//                fp.FaceIdentify();
//            } catch (IndexOutOfBoundsException e) {
//                tv_info.setText("该人员尚未登记人脸信息");
//                sp.redLight();
//            }
        } catch (IndexOutOfBoundsException e) {
            RetrofitGenerator.getHnmbyApi().queryPersonInfo("queryPersion", config.getString("key"), cardInfo.cardId())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ResponseBody>(getV()) {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                String s = responseBody.string().toString();
                                if (s.equals("false")) {
                                    Keeper inside_keeper = new Keeper();
                                    inside_keeper.setName(cardInfo.name());
                                    inside_keeper.setCardID(cardInfo.cardId());
                                    unknownUser.setKeeper(inside_keeper);
                                    unknownPeople(fp.getGlobalBitmap());
                                    getV().infoSet("系统查无此人");
                                    MediaHelper.play(MediaHelper.Text.man_non);
                                    sp.redLight();
                                } else if (s.startsWith("true")) {
                                    String type = s.substring(5, s.length());
                                    mdaosession.insertOrReplace(new Employer(cardInfo.cardId(), Integer.valueOf(type)));
                                    getV().infoSet("该人员尚未登记人脸信息");
                                    sp.redLight();
                                } else if (s.equals("noUnitId")) {
                                    getV().infoSet("该设备还未在系统上备案");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                getV().infoSet("Exception");
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
                            getV().infoSet("系统查无此人");
                            MediaHelper.play(MediaHelper.Text.man_non);
                            sp.redLight();
                        }
                    });
        }
    }


    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
        if (getV().getAlert_message().Showing()) {
            getV().getAlert_message().setICCardText(cardInfo.getUid());
            return;
        }
        if (cardInfo.getUid().equals(AppInit.The_IC_UID)) {
            fp.PreviewCease(() -> ActivityUtils.startActivity(getV().getPackageName(), getV().getPackageName() + AppInit.getInstrumentConfig().getAddActivity()));
        } else {
            ToastUtils.showShort("非法IC卡");
            sp.redLight();
        }
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto = bmp;
    }


    @Override
    public void onSetText(String Msg) {
        if (Msg.startsWith("SAM")) {
            ToastUtils.showLong(Msg);
        }
    }

    @Override
    public void onUser(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, User user) {
        if (resultType.equals(Identify_success)) {
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
                        cg_User1.setFaceFeature(mFeatures);

                        getV().infoSet("仓管员" + cg_User1.getKeeper().getName() + "操作成功,请继续仓管员操作");
                        sp.greenLight();
                        DoorOpenOperation.getInstance().doNext();
                        Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .compose(getV().<Long>bindUntilEvent(ActivityEvent.PAUSE))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        checkChange = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        checkRecord(String.valueOf(2), keeper, Scene_Bitmap);
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
                            if (checkChange != null) {
                                checkChange.dispose();
                            }
                            cg_User2.setKeeper(keeper);
                            cg_User2.setScenePhoto(Scene_Bitmap);
                            cg_User2.setSceneHeadPhoto(Scene_headphoto);
                            cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
                            cg_User2.setFaceFeature(mFeatures);
                            getV().infoSet("仓管员" + cg_User2.getKeeper().getName() + "操作成功,请等待...");
                            fp.Feature_to_Feature(cg_User1.getFaceFeature(), cg_User2.getFaceFeature());
//                            fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(), false, true);
                        } else {
                            sp.redLight();
                            getV().infoSet("请不要连续输入相同的管理员信息");
                            return;
                        }
                    } else if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.TwoUnlock)) {
                        getV().infoSet("仓库门已解锁");
                    }
                } else if (employer.getType() == 2) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
                        if (AppInit.getInstrumentConfig().XungengCanOpen()) {
                            if (!keeper.getCardID().equals(cg_User1.getKeeper().getCardID())) {
                                sp.greenLight();
                                cg_User2.setKeeper(keeper);
                                cg_User2.setScenePhoto(Scene_Bitmap);
                                cg_User2.setSceneHeadPhoto(Scene_headphoto);
                                cg_User2.setFaceRecognition(Integer.parseInt(faceScore));
                                cg_User2.setFaceFeature(mFeatures);
                                getV().infoSet("巡检员" + cg_User2.getKeeper().getName() + "操作成功,请等待...");
                                fp.Feature_to_Feature(cg_User1.getFaceFeature(), cg_User2.getFaceFeature());
//                                fp.IMG_to_IMG(cg_User1.getSceneHeadPhoto(), cg_User2.getSceneHeadPhoto(), false, true);
                            } else {
                                sp.redLight();
                                getV().infoSet("请不要连续输入相同的管理员信息");
                                return;
                            }
                        } else {
                            checkRecord("2", keeper, Scene_Bitmap);
                        }
                    } else {
                        checkRecord("2", keeper, Scene_Bitmap);
                    }
                } else if (employer.getType() == 3) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    checkRecord("3", keeper, Scene_Bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    public void onText(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Identify_failed)) {
            getV().infoSet(text);
            sp.redLight();
//            unknownPeopleNoCard(fp.getGlobalBitmap());
        } else if (resultType.equals(Identify_success)) {
            faceScore = text;
        } else if (resultType.equals(IMG_MATCH_IMG_Score)) {
            CompareScore = text;
            OutputControlPresenter.getInstance().buzz(IOutputControl.Hex.H0);
            sp.greenLight();
            getV().runOnUiThread(() -> getV().infoSet("信息处理完毕,仓库门已解锁"));
            DoorOpenOperation.getInstance().doNext();
            EventBus.getDefault().post(new PassEvent());
            getV().iv_lock.setImageBitmap(BitmapFactory.decodeResource(getV().getResources(), R.drawable.iv_mj1));
        }
    }

    @Override
    public void onLivenessModel(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, LivenessModel model) {
        if (resultType.equals(Identify_success)) {
            try {
                Keeper keeper = mdaosession.queryRaw(Keeper.class,
                        "where CARD_ID = '" + model.getUser().getUserInfo().toUpperCase() + "'").get(0);
                mFeatures = model.getFeature();
//                if (keeper.getHeadphotoBW() == null) {
//                    keeper.setHeadphotoBW(FileUtils.bitmapToBase64(Scene_headphoto));
//                    mdaosession.insertOrReplace(keeper);
//                    fp.FaceRegOrUpdateByFeature(keeper.getName(), keeper.getCardID(), model.getFeature(), false);
//                }
            } catch (Exception e) {
                ToastUtils.showLong(e.toString());
            }
        } else if (resultType.equals(Identify_failed)) {
            if (model.getIrLivenessScore() > 0.01f) {
                unknownPeopleNoCard(fp.getGlobalBitmap());
                Log.e("model_IrScore", String.valueOf(model.getIrLivenessScore()));
            } else {
                Log.e("model_IrScore", String.valueOf(model.getIrLivenessScore()));

            }
        }
    }

    private void checkRecord(String type, Keeper keeper, Bitmap bitmap) {
        OutputControlPresenter.getInstance().on12V_Alarm(false);
        final JSONObject checkRecordJson = new JSONObject();
        try {
            checkRecordJson.put("id", keeper.getCardID());
            checkRecordJson.put("name", keeper.getName());
            checkRecordJson.put("photos", FileUtils.bitmapToBase64(bitmap));
            checkRecordJson.put("checkType", type);
            checkRecordJson.put("datetime", TimeUtils.getNowString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("saveVisit", config.getString("key"), checkRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(getV()) {
                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            sp.greenLight();
                            getV().infoSet("巡检员" + keeper.getName() + "巡检成功");
                        } else if (s.equals("false")) {
                            getV().infoSet("巡检失败");
                        } else if (s.equals("dataErr")) {
                            getV().infoSet("上传巡检数据失败");
                        } else if (s.equals("dataErr")) {
                            getV().infoSet("数据库操作有错");
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        getV().infoSet("无法连接服务器,请检查网络,离线数据已保存");
                        mdaosession.insert(new ReUploadBean(null, "saveVisit", checkRecordJson.toString()));
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
        final JSONObject unknownPeopleJson = new JSONObject();
        try {
            unknownPeopleJson.put("visitIdcard", unknownUser.getKeeper().getCardID());
            unknownPeopleJson.put("visitName", unknownUser.getKeeper().getName());
            unknownPeopleJson.put("photos", FileUtils.bitmapToBase64(bmp));
            unknownPeopleJson.put("photoSfz", FileUtils.bitmapToBase64(headphoto));
            unknownPeopleJson.put("datetime", TimeUtils.getNowString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("persionRecord", config.getString("key"),
                unknownPeopleJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(getV()) {

                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            getV().infoSet("访问人" + unknownUser.getKeeper().getName() + "数据上传成功");
                        } else if (s.equals("false")) {
                            getV().infoSet("访问人上传失败");
                        } else if (s.equals("dataErr")) {
                            getV().infoSet("上传访问人数据失败");
                        } else if (s.equals("dbErr")) {
                            getV().infoSet("数据库操作有错");
                        }
                        unknownUser = new SceneKeeper();

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        getV().infoSet("无法连接服务器,请检查网络,离线数据已保存");
                        unknownUser = new SceneKeeper();
                        mdaosession.insert(new ReUploadBean(null, "persionRecord", unknownPeopleJson.toString()));
                    }
                });
    }


    int unknownPeopleNum = 30;

    private void unknownPeopleNoCard(Bitmap bmp) {
        final JSONObject unknownPeopleJson = new JSONObject();
        try {
            unknownPeopleJson.put("photos", FileUtils.bitmapToBase64(bmp));
            unknownPeopleJson.put("datetime", TimeUtils.getNowString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("persionRecord", config.getString("key"),
                unknownPeopleJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(getV()) {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            getV().infoSet("未知人员来访信息上传成功");
                        } else if (s.equals("false")) {
                            getV().infoSet("访问人上传失败");
                        } else if (s.equals("dataErr")) {
                            getV().infoSet("上传访问人数据失败");
                        } else if (s.equals("dbErr")) {
                            getV().infoSet("数据库操作有错");
                        }
                        unknownUser = new SceneKeeper();

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        getV().infoSet("无法连接服务器,请检查网络,离线数据已保存");
                        unknownUser = new SceneKeeper();
                        mdaosession.insert(new ReUploadBean(null, "persionRecord", unknownPeopleJson.toString()));
                        List<ReUploadBean> reUploadBeanList = mdaosession.loadAll(ReUploadBean.class);
                        List<ReUploadBean> unknownUserList = new ArrayList<ReUploadBean>();
                        for (ReUploadBean reUploadBean : reUploadBeanList) {
                            if (reUploadBean.getMethod().equals("persionRecord")) {
                                unknownUserList.add(reUploadBean);
                            }
                        }
                        if (unknownUserList.size() > unknownPeopleNum) {
                            for (int i = 0; i < unknownUserList.size() - unknownPeopleNum; i++) {
                                mdaosession.delete(unknownUserList.get(i));
                            }
                        }

                    }
                });
    }

    public void OpenDoorRecord(boolean leagl) {
        if (checkChange != null) {
            checkChange.dispose();
        }
        final JSONObject OpenDoorJson = new JSONObject();
        if (leagl) {
            try {
                OpenDoorJson.put("id1", cg_User1.getKeeper().getCardID());
                OpenDoorJson.put("id2", cg_User2.getKeeper().getCardID());
                OpenDoorJson.put("name1", cg_User1.getKeeper().getName());
                OpenDoorJson.put("name2", cg_User2.getKeeper().getName());
                OpenDoorJson.put("photo1", FileUtils.bitmapToBase64(cg_User1.getScenePhoto()));
                OpenDoorJson.put("photo2", FileUtils.bitmapToBase64(cg_User2.getScenePhoto()));
                OpenDoorJson.put("faceRecognition1", cg_User1.getFaceRecognition());
                OpenDoorJson.put("faceRecognition2", cg_User2.getFaceRecognition());
                OpenDoorJson.put("faceRecognition3", CompareScore);
                OpenDoorJson.put("datetime", TimeUtils.getNowString());
                OpenDoorJson.put("state", "y");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return;
            //            try {
            //                OpenDoorJson.put("datetime", TimeUtils.getNowString());
            //                OpenDoorJson.put("state", "n");
            //            } catch (JSONException e) {
            //                e.printStackTrace();
            //            }
        }
        RetrofitGenerator.getHnmbyApi().withDataRs("openDoorRecord", config.getString("key"), OpenDoorJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(getV()) {
                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            try {
                                if (OpenDoorJson.getString("state").equals("y")) {
                                    getV().infoSet("正常开门数据上传成功");
                                } else {
                                    getV().infoSet("非法开门数据上传成功");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (s.equals("false")) {
                            getV().infoSet("开门数据上传失败");
                        } else if (s.equals("dataErr")) {
                            getV().infoSet("上传的json数据有错");
                        } else if (s.equals("dbErr")) {
                            getV().infoSet("数据库操作有错");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        getV().infoSet("无法连接服务器,请检查网络,离线数据已保存");
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

    void openService() {
        intent = new Intent(getV(), AppInit.getInstrumentConfig().getServiceName());
        getV().startService(intent);
    }

    public void LockOn() {
        cg_User1 = new SceneKeeper();
        cg_User2 = new SceneKeeper();
    }

}
