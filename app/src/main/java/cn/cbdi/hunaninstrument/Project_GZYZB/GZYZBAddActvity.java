package cn.cbdi.hunaninstrument.Project_GZYZB;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Alert.Alarm;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.FingerprintUser;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.Tool.MyObserver;
import cn.cbdi.hunaninstrument.greendao.DaoSession;

import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.presenter.FingerPrintPresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.view.IFingerPrintView;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class GZYZBAddActvity extends Activity implements IFingerPrintView {

    private String TAG = GZYZBAddActvity.class.getSimpleName();

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    private SPUtils fingerprintBooks = SPUtils.getInstance("fingerprintBooks");

    private SPUtils fingerprintBooksRevert = SPUtils.getInstance("fingerprintBooksRevert");

    String alertTitle = "请选择接下来的操作";

    String userFaceID = "EMPTY";

    String fingerprintID = "EMPTY";

    FingerprintUser user = new FingerprintUser();

    boolean FingerReady = false;

    @BindView(R.id.iv_userPic)
    ImageView iv_userPic;

    @BindView(R.id.iv_finger)
    ImageView iv_finger;

    @BindView(R.id.et_finger)
    EditText et_finger;

    @BindView(R.id.et_idcard)
    EditText et_idcard;

    @BindView(R.id.btn_query)
    Button query;


    @OnClick(R.id.iv_userPic)
    void getPic() {
        try {
            if (!userFaceID.equals("EMPTY")) {
                FacePresenter.getInstance().FaceDeleteByUserId(userFaceID);

            }
            if (user.getCardId()!= null) {
                FaceDetect(user.getCardId().toUpperCase(), user.getName());
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
        ActivityUtils.startActivity(bundle, getPackageName(), getPackageName() + ".Project_GZYZB.GZYZBFaceDetectActivity");
    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        new AlertView(alertTitle, null, null, new String[]{"重置并继续录入信息", "退出至主桌面"}, null, GZYZBAddActvity.this, AlertView.Style.Alert, new OnItemClickListener() {
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
                    query.setClickable(true);
                    query.setText("校验人员信息");
                    et_idcard.setHint("请填写身份证信息");
                    et_idcard.setText(null);
                    et_finger.setText("需选择人员获得指纹编号");
                    user = new FingerprintUser();
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
            Keeper keeper = new Keeper(user.getCardId().toUpperCase(), user.getName(),
                    null, null, null, userFaceID, null);
            mdaoSession.insertOrReplace(keeper);
            Employer employer = new Employer(user.getCardId().toUpperCase(),Integer.valueOf(user.getCourType()));
            mdaoSession.insertOrReplace(employer);
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else if (!fingerprintID.equals("EMPTY")) {
            String uid = UUID.randomUUID().toString();
            fingerprintBooks.put(fingerprintID, uid);
            fingerprintBooksRevert.put(uid, fingerprintID);
            Keeper keeper = new Keeper(user.getCardId().toUpperCase(), user.getName(),
                    null, null, null, uid, null);
            mdaoSession.insertOrReplace(keeper);
            Employer employer = new Employer(user.getCardId().toUpperCase(),Integer.valueOf(user.getCourType()));
            mdaoSession.insertOrReplace(employer);
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else if (!userFaceID.equals("EMPTY")) {
            Keeper keeper = new Keeper(user.getCardId().toUpperCase(), user.getName(),
                    null, null, null, userFaceID, null);
            mdaoSession.insertOrReplace(keeper);
            Employer employer = new Employer(user.getCardId().toUpperCase(),Integer.valueOf(user.getCourType()));
            mdaoSession.insertOrReplace(employer);
            userFaceID = "EMPTY";
            fingerprintID = "EMPTY";
            ToastUtils.showLong("人员插入成功");
            alertTitle = "人员插入成功,请选择接下来的操作";
            cancel();
        } else {
            Alarm.getInstance(GZYZBAddActvity.this,null).messageAlarm("您还有信息未登记，如需退出请按取消");
        }
    }

    @OnClick(R.id.btn_query)
    void queryPerson() {
        if (!TextUtils.isEmpty(et_idcard.getText().toString().toUpperCase())) {
            RetrofitGenerator.getYzbApi().queryPersonInfo("queryPersonInfo", config.getString("key"), et_idcard.getText().toString().toUpperCase())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ResponseBody>(this) {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                        new TypeToken<HashMap<String, String>>() {
                                        }.getType());
                                if (infoMap.size() > 0) {
//                                    if (infoMap.get("status").equals(String.valueOf(0))) {
                                    if (infoMap.get("status").equals(String.valueOf(0)) || infoMap.get("status").equals(String.valueOf(1))) {
                                        fingerprintID = String.valueOf(fpp.fpGetEmptyID());
                                        iv_finger.setClickable(false);
                                        fpp.fpEnroll(fingerprintID);
                                        user.setCardId(et_idcard.getText().toString().toUpperCase());
                                        user.setName(infoMap.get("name"));
                                        user.setFingerprintId(fingerprintID );
                                        user.setCourIds(infoMap.get("courIds"));
                                        user.setCourType(infoMap.get("courType"));
                                        query.setText(infoMap.get("name") + ",欢迎您！");
                                        query.setClickable(false);
                                    } else {
                                        Alarm.getInstance(GZYZBAddActvity.this,null).messageAlarm("您的身份有误，如有疑问请联系客服处理");
                                    }
                                } else {
                                    Alarm.getInstance(GZYZBAddActvity.this,null).messageAlarm("系统未能查询到该人员信息，如有疑问请联系客服处理");
                                }
                            } catch (IOException e) {
                                Log.e(TAG, e.toString());
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
        } else {
            ToastUtils.showLong("身份证号为空，请输入身份证号");
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_add_person);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        fpp.FingerPrintPresenterSetView(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        fpp.fpCancel(true);
        fpp.FingerPrintPresenterSetView(null);
    }


    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Alarm.getInstance(this,null).release();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onText(String msg) {
        if (!msg.equals("Canceled")) {
            et_finger.setText(msg);
        }
        if (msg.endsWith("录入成功")) {
            FingerReady = true;
            iv_userPic.setClickable(true);
//            ToastUtils.showLong("您现在可以点击人像捕捉人脸信息。");
        }
        if (msg.endsWith("点我重试")) {
            iv_finger.setClickable(true);
        }
    }

    @Override
    public void onSetImg(Bitmap bmp) {
        iv_finger.setImageBitmap(bmp);
    }

    @Override
    public void onFpSucc(String msg) {


    }

    int count = 0;



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceDetectEvent(FaceDetectEvent event) {
        iv_userPic.setImageBitmap(event.getBitmap());
        userFaceID = event.getUserId();
        if (!FingerReady) {
            et_finger.setText("点击指纹图片录入指纹");
            iv_finger.setClickable(true);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {
        final JSONObject OpenDoorjson = new JSONObject();
        try {
            OpenDoorjson.put("datetime", TimeUtils.getNowString());
            OpenDoorjson.put("state", "n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getYzbApi().withDataRr("openDoorRecord", config.getString("key"), OpenDoorjson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorjson.toString()));

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }




}
