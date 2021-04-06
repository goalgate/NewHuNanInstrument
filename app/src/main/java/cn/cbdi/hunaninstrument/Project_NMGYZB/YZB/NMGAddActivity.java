package cn.cbdi.hunaninstrument.Project_NMGYZB.YZB;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.model.User;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.view.RxView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.Tool.SafeCheck;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.CardInfoBean;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.presenter.FingerPrintPresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.view.IFingerPrintView;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class NMGAddActivity extends Activity implements IFingerPrintView {

    private ArrayAdapter<String> adapter;

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    private SPUtils config = SPUtils.getInstance("config");

    ICardInfo choose_cardInfo = new CardInfoBean();

    String alertTitle = "请选择接下来的操作";

    String userFaceID = "EMPTY";

    String fingerprintID = "EMPTY";

    boolean FingerReady = false;

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    @BindView(R.id.iv_userPic)
    ImageView iv_userPic;

    @BindView(R.id.iv_finger)
    ImageView iv_finger;

    @BindView(R.id.tv_finger)
    TextView tv_finger;

    private SPUtils fingerprintBooks = SPUtils.getInstance("fingerprintBooks");

    private SPUtils fingerprintBooksRevert = SPUtils.getInstance("fingerprintBooksRevert");

    HashMap<String, String> paramsMap = new HashMap<String, String>();

    @OnClick(R.id.iv_userPic)
    void getPic() {
        try {
            if (!userFaceID.equals("EMPTY")) {
                FacePresenter.getInstance().FaceDeleteByUserId(userFaceID);

            }
            if (choose_cardInfo.cardId() != null) {
                FaceDetect(choose_cardInfo.cardId(), choose_cardInfo.name());
            } else {
                ToastUtils.showLong("没有可供录入的人员信息");
            }

        } catch (Exception e) {
            ToastUtils.showLong(e.toString());
        }
    }

    void FaceDetect(String cardId, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("cardId", cardId);
        bundle.putString("name", name);
        ActivityUtils.startActivity(bundle, getPackageName(), getPackageName() + ".Project_NMGYZB.NMGFaceDetectActivity");
    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        new AlertView(alertTitle, null, null, new String[]{"重置并继续录入信息", "退出至主桌面"}, null, NMGAddActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    alertTitle = "请选择接下来的操作";
                    if (!userFaceID.equals("EMPTY")) {
                        try {
                            FacePresenter.getInstance().FaceDeleteByUserId(userFaceID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (FingerReady) {
                        fpp.fpRemoveTmpl(fingerprintID);
                    }
                    FingerReady = false;
                    userFaceID = "EMPTY";
                    fingerprintID = "EMPTY";
                    tv_finger.setText("需选择人员获得指纹编号");
                    iv_finger.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.zw_icon));
                    iv_userPic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.user_icon));
                } else {
                    if (!userFaceID.equals("EMPTY")) {
                        try {
                            FacePresenter.getInstance().FaceDeleteByUserId(userFaceID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!fingerprintID.equals("EMPTY")) {
                        fpp.fpRemoveTmpl(fingerprintID);
                    }
                    FacePresenter.getInstance().FaceIdentify_model();
                    finish();
                }
            }
        }).show();
    }

    @OnClick(R.id.btn_commit)
    void commit() {
        if ((!userFaceID.equals("EMPTY")) && (!fingerprintID.equals("EMPTY"))) {
            fingerprintBooks.put(fingerprintID, userFaceID);
            fingerprintBooksRevert.put(userFaceID, fingerprintID);
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else if (!fingerprintID.equals("EMPTY")) {
            String uid = UUID.randomUUID().toString();
            fingerprintBooks.put(fingerprintID, uid);
            fingerprintBooksRevert.put(uid, fingerprintID);
            Keeper keeper = new Keeper(choose_cardInfo.cardId(), choose_cardInfo.name(),
                    null, null, null, uid, null);
            mdaoSession.insertOrReplace(keeper);
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else if (!userFaceID.equals("EMPTY")) {
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else {
            Alarm.getInstance(NMGAddActivity.this,null).messageAlarm("您还有信息未登记，如需退出请按取消");
        }
    }

    @BindView(R.id.et_idcard)
    EditText et_idcard;

    @OnClick(R.id.btn_query) void query(){
        HashMap<String, String> map = (HashMap<String, String>) paramsMap.clone();
        map.put("dataType", "queryPersion");
        map.put("id", et_idcard.getText().toString());
        RetrofitGenerator.getNMGYZBApi().GeneralPersionInfo(map)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<String>(this) {

                    @Override
                    public void onNext(String s) {
                        try {
                            if (s.equals("false")) {
                                Alarm.getInstance(NMGAddActivity.this,null).messageAlarm("系统未能查询到该人员信息，如有疑问请联系客服处理");
                                OutputControlPresenter.getInstance().redLight();
                            } else if (s.startsWith("true")) {


                            } else if (s.equals("noUnitId")) {
                                Alarm.getInstance(NMGAddActivity.this,null).messageAlarm("系统还没有绑定该设备");
                                OutputControlPresenter.getInstance().redLight();
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ToastUtils.showLong("无法连接服务器，请检查网络");
                    }
                });
    }

    private void mapInit() {
        SafeCheck safeCheck = new SafeCheck();
        safeCheck.setURL(config.getString("ServerId"));
        paramsMap.put("daid", config.getString("daid"));
        paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_person_add_face_fingerprint);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        RxView.clicks(iv_finger).throttleFirst(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((o) -> {
                    fingerprintID = String.valueOf(fpp.fpGetEmptyID());
                    fpp.fpEnroll(fingerprintID);
                    iv_finger.setClickable(false);
                });
        iv_finger.setClickable(false);

        mapInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Alarm.getInstance(this,null).release();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onFpSucc(String msg) {

    }

    @Override
    public void onSetImg(Bitmap bmp) {
        iv_finger.setImageBitmap(bmp);

    }

    @Override
    public void onText(String msg) {
        if (!msg.equals("Canceled")) {
            tv_finger.setText(msg);
        }
        if (msg.endsWith("录入成功")) {
            FingerReady = true;
            iv_userPic.setClickable(true);
            ToastUtils.showLong("您现在可以点击人像获取人脸信息。");
        }
        if (msg.endsWith("点我重试")) {
            iv_finger.setClickable(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceDetectEvent(FaceDetectEvent event) {
        iv_userPic.setImageBitmap(event.getBitmap());
        userFaceID = event.getUserId();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {


    }


}
