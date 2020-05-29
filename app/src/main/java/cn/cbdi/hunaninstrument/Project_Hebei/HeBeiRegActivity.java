package cn.cbdi.hunaninstrument.Project_Hebei;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Project_HuNan.HuNanRegActivity;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.Tool.SafeCheck;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_Card.presenter.IDCardPresenter;
import cn.cbsd.cjyfunctionlib.Func_Card.view.IIDCardView;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.view.IFaceView;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_False;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.IMG_MATCH_IMG_True;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Reg_failed;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Reg_success;

public class HeBeiRegActivity extends RxActivity implements IFaceView, IIDCardView {

    private String TAG = HeBeiRegActivity.class.getSimpleName();

    private SPUtils config = SPUtils.getInstance("config");

    OutputControlPresenter sp = OutputControlPresenter.getInstance();

    FacePresenter fp = FacePresenter.getInstance();

    IDCardPresenter idp = IDCardPresenter.getInstance();

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    HashMap<String, String> paramsMap = new HashMap<String, String>();

    Disposable disposableTips;

    Disposable disposableTimer;

    ICardInfo global_cardInfo;

    Bitmap cardBitmap;

    Bitmap headphotoIR;

    @BindView(R.id.preview_view)
    AutoTexturePreviewView previewView;

    @BindView(R.id.preview_view1)
    AutoTexturePreviewView previewView1;

    @BindView(R.id.texture_view)
    TextureView textureView;

    @BindView(R.id.tv_info)
    TextView tv_info;

    @BindView(R.id.tv_timer)
    TextView tv_timer;




//    @OnClick(R.id.preview_view)
//    void change() {
//        fp.PreviewCease(() -> HuNanRegActivity.this.finish());
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.layout_reg);
        ButterKnife.bind(this);
        mapInit();
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(30, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));

        disposableTimer = RxTextView.textChanges(tv_timer)
                .debounce(120, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((charSequence) -> fp.PreviewCease(() -> HeBeiRegActivity.this.finish()));

    }


    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(AppInit.getContext(), previewView, previewView1, textureView);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        MediaHelper.play(MediaHelper.Text.reg_model);
        idp.IDCardPresenterSetView(this);
        fp.FaceSetNoAction();
        fp.useRGBCamera(true);
        Observable.timer(2, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    AppInit.getInstrumentConfig().readCard();
                });
        fp.FacePresenterSetView(this);
        fp.FaceIdentifyReady();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        fp.FacePresenterSetView(null);
        idp.IDCardPresenterSetView(null);
        AppInit.getInstrumentConfig().stopReadCard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        if (disposableTips != null) {
            disposableTips.dispose();
        }
        if (disposableTimer != null) {
            disposableTimer.dispose();
        }
    }

    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
        if (cardInfo.getUid().equals(AppInit.The_IC_UID)) {
            fp.PreviewCease(() -> HeBeiRegActivity.this.finish());
        } else {
            ToastUtils.showLong("非法IC卡");
            sp.redLight();
        }
    }


    @Override
    public void onsetCardInfo(ICardInfo cardInfo) {
        global_cardInfo = cardInfo;
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        cardBitmap = bmp;
        if (bmp != null) {
            try {
                mdaosession.queryRaw(Employer.class, "where CARD_ID = '" + global_cardInfo.cardId().toUpperCase() + "'").get(0);
                tv_info.setText("等待人证比对结果返回");
                tv_timer.setText("等待人证比对结果返回");
                MediaHelper.play(MediaHelper.Text.waiting);
                can_recentPic = true;
                natural = true;
                fp.FaceVerifyAndReg(global_cardInfo.name(), global_cardInfo.cardId(), bmp);
            } catch (IndexOutOfBoundsException e) {
                HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
                map.put("dataType", "queryPersion");
                map.put("id", global_cardInfo.cardId());
                RetrofitGenerator.getHeBeiApi().GeneralPersionInfo(map)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new MyObserver<String>(this) {

                            @Override
                            public void onNext(String s) {
                                try {
                                    if (s.equals("false")) {
                                        tv_info.setText("系统查无此人");
                                        tv_timer.setText("系统查无此人");
                                        MediaHelper.play(MediaHelper.Text.man_non);
                                        sp.redLight();
                                    } else if (s.startsWith("true")) {
                                        mdaosession.insertOrReplace(new Employer(global_cardInfo.cardId(), Integer.valueOf(s.split("\\|")[1])));
                                        tv_info.setText("等待人证比对结果返回");
                                        tv_timer.setText("等待人证比对结果返回");
                                        MediaHelper.play(MediaHelper.Text.waiting);
                                        can_recentPic = true;
                                        natural = true;
                                        fp.FaceVerifyAndReg(global_cardInfo.name(), global_cardInfo.cardId(), bmp);
                                    } else if (s.equals("noUnitId")) {
                                        tv_info.setText("系统还没有绑定该设备");
                                        tv_timer.setText("系统还没有绑定该设备");
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
                                ToastUtils.showLong("无法连接服务器，请检查网络");
                            }
                        });
            } catch (NullPointerException e) {
                tv_info.setText("刷卡时间太短，无法获得身份证照片数据");
                tv_timer.setText("刷卡时间太短，无法获得身份证照片数据");
            }
        } else {
            tv_info.setText("刷卡时间太短，无法获得身份证照片数据");
            tv_timer.setText("刷卡时间太短，无法获得身份证照片数据");

        }
    }


    @Override
    public void onSetText(String Msg) {

    }

    boolean can_recentPic = true;

    boolean natural = true;

    @Override
    public void onText(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(IMG_MATCH_IMG_True)) {
            tv_info.setText(text);
            tv_timer.setText(text);
        } else if (resultType.equals(IMG_MATCH_IMG_False)) {
            sp.redLight();
            if (can_recentPic) {
                tv_info.setText("人证比对分数过低，正在调取系统上最新的人员照片");
                tv_timer.setText("人证比对分数过低，正在调取系统上最新的人员照片");
                recentPic();
            } else {
                tv_info.setText(text);
                tv_timer.setText(text);
            }
        } else if (resultType.equals(Reg_success)) {
            tv_info.setText("人员数据已成功录入");
            tv_timer.setText("人员数据已成功录入");
            sp.greenLight();
        } else if (resultType.equals(Reg_failed)) {
            tv_info.setText(text);
            tv_timer.setText(text);
            sp.redLight();
        }
    }

    @Override
    public void onUser(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, User user) {
        if(resultType.equals(Reg_success)){
            Keeper keeper = new Keeper(global_cardInfo.cardId().toUpperCase(),
                    global_cardInfo.name(), FileUtils.bitmapToBase64(headphotoIR), null, null,
                    user.getUserId(), user.getFeature());
            mdaosession.getKeeperDao().insertOrReplace(keeper);
        }
    }

    @Override
    public void onBitmap(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        if(resultType.equals(headphotoIR)){
            headphotoIR = bitmap;
        }
    }

    private void mapInit() {
        SafeCheck safeCheck = new SafeCheck();
        safeCheck.setURL(config.getString("ServerId"));
        paramsMap.put("daid", config.getString("daid"));
        paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
    }

    private void faceUpload() {
        JSONObject jsonObject = new JSONObject();
        try {
            Keeper keeper = AppInit.getInstance().getDaoSession().queryRaw(Keeper.class,
                    "where CARD_ID = '" + global_cardInfo.cardId().toUpperCase() + "'").get(0);
            jsonObject.put("cardID", keeper.getCardID());
            jsonObject.put("name", keeper.getName());
            jsonObject.put("headphoto", keeper.getHeadphoto());
            jsonObject.put("headphotoRGB", keeper.getHeadphotoRGB());
            jsonObject.put("headphotoBW", keeper.getHeadphotoBW());
            jsonObject.put("feature", Base64.encodeToString(keeper.getFeature(), Base64.DEFAULT));
            jsonObject.put("naturalFace", natural ? "true" : "false");
        } catch (Exception e) {
            e.printStackTrace();
        }

        RetrofitGenerator.getHeBeiApi().faceUpload("faceUpload", paramsMap.get("daid"), paramsMap.get("pass"), jsonObject.toString())
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
                            tv_info.setText("已存入系统人脸数据库中");
                        } else {
                            tv_info.setText("人脸保存数据库出错，请联系客服处理");
                            mdaosession.insert(new ReUploadBean(null, "faceUpload", jsonObject.toString()));
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        mdaosession.insert(new ReUploadBean(null, "faceUpload", jsonObject.toString()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void recentPic() {
        can_recentPic = false;
        natural = false;
        idp.StopReadID();
        RetrofitGenerator.getHeBeiApi().recentPicNew("recentPic", paramsMap.get("daid"), paramsMap.get("pass"), global_cardInfo.cardId())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            String result = jsonObject.getString("result");
                            if (result.equals("true")) {
                                String ps = jsonObject.getString("returnPic");
                                if (!TextUtils.isEmpty(ps)) {
                                    Bitmap bitmap = FileUtils.base64ToBitmap(ps);
                                    fp.FaceVerifyAndReg(global_cardInfo.name(),global_cardInfo.cardId(),bitmap);
                                } else {
                                    tv_info.setText("该人员尚未在系统提交最新照片");
                                    tv_timer.setText("该人员尚未在系统提交最新照片");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
//                        try {
//                            JSONObject jsonObject = new JSONObject(s);
//                            String ps = jsonObject.getString("result");
//                            if (!TextUtils.isEmpty(ps)) {
//                                Bitmap bitmap = FileUtils.base64ToBitmap(ps);
//                                fp.IMG_to_IMG(cardBitmap, bitmap, true);
//                            } else {
//                                tv_info.setText("该人员尚未在系统提交最新照片");
//                                tv_timer.setText("该人员尚未在系统提交最新照片");
//                            }
//                        } catch (Exception e) {
//                            Lg.e(TAG, e.toString());
//                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        tv_timer.setText("服务器连接失败,无法获取最新照片");
                        idp.ReadID();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        idp.ReadID();
                    }
                });
    }

    @Override
    public void onLivenessModel(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, LivenessModel model) {

    }

    @Override
    public void onBackPressed() {

    }
}
