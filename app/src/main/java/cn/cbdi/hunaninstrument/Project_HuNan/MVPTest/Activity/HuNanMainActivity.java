package cn.cbdi.hunaninstrument.Project_HuNan.MVPTest.Activity;

import android.content.Intent;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cbsd.mvphelper.mvplibrary.mvpforView.MVPBaseActivity;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Alert.Alert_IP;
import cn.cbdi.hunaninstrument.Alert.Alert_Message;
import cn.cbdi.hunaninstrument.Alert.Alert_Password;
import cn.cbdi.hunaninstrument.Alert.Alert_Server;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.Project_HuNan.MVPTest.Presenter.HuNanMainPresenter;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.State.DoorState.WarehouseDoor;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.UDPState;
import cn.cbdi.hunaninstrument.UI.NormalWindow;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HuNanMainActivity extends MVPBaseActivity<HuNanMainPresenter> implements NormalWindow.OptionTypeListener {

    protected String TAG = cn.cbdi.hunaninstrument.Project_HuNan.HuNanMainActivity.class.getSimpleName();

    private DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    private Disposable disposableTips;

    private GestureLibrary mGestureLib;

    private NormalWindow normalWindow;

    private Intent intent;

    SPUtils config = SPUtils.getInstance("config");

    private int last_mTemperature;

    private int last_mHumidity;

    private Alert_Message alert_message = new Alert_Message(this);

    private Alert_Server alert_server = new Alert_Server(this);

    private Alert_IP alert_ip = new Alert_IP(this);

    private Alert_Password alert_password = new Alert_Password(this);

    @BindView(R.id.tv_info)
    public TextView tv_info;

    @BindView(R.id.iv_network)
    public ImageView iv_network;

    @BindView(R.id.iv_setting)
    public ImageView iv_setting;

    @BindView(R.id.tv_time)
    public TextView tv_time;

    @BindView(R.id.iv_lock)
    public ImageView iv_lock;

    @BindView(R.id.tv_temperature)
    public TextView tv_temperature;

    @BindView(R.id.tv_humidity)
    public TextView tv_humidity;

    @BindView(R.id.preview_view)
    public AutoTexturePreviewView previewView;

    @BindView(R.id.preview_view1)
    public AutoTexturePreviewView previewView1;

    @BindView(R.id.texture_view)
    public TextureView textureView;

    @BindView(R.id.tv_daid)
    public TextView tv_daid;

    @BindView(R.id.gestures_overlay)
    public GestureOverlayView gestures;

    @OnClick(R.id.lay_setting)
    public void setting() {
        alert_password.show();
    }

    @OnClick(R.id.lay_network)
    public void showMessage() {
        alert_message.showMessage();
    }

    @OnClick(R.id.lay_lock)
    public void showPeople() {
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

    @Override
    public int getLayoutId() {
        return R.layout.activity_newmain;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        tv_daid.setText(config.getString("daid"));
        setGestures();
        UIReady();
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(5, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
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
    public int getOptionsMenuId() {
        return 0;
    }

    @Override
    public HuNanMainPresenter newP() {
        return new HuNanMainPresenter();
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

    void openService() {
        intent = new Intent(this, AppInit.getInstrumentConfig().getServiceName());
        startService(intent);
    }

    private void UIReady() {
        alert_ip.IpviewInit();
        alert_server.serverInit(() -> iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.iv_wifi)));
        alert_message.messageInit();
        alert_password.PasswordViewInit(() -> {
            normalWindow = new NormalWindow(this);
            normalWindow.setOptionTypeListener(this);
            normalWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content),
                    Gravity.CENTER, 0, 0);
        });
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        last_mTemperature = event.getTem();
        last_mHumidity = event.getHum();
        tv_temperature.setText(event.getTem() + "℃");
        tv_humidity.setText(event.getHum() + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if (event.getNetwork_state()) {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_wifi));
        } else {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_wifi1));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event) {
        tv_info.setText("门磁打开报警,请检查门磁情况");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {
        getP().OpenDoorRecord(event.getLegal());

        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetLockUpEvent(LockUpEvent event) {
        tv_info.setText("仓库已重新上锁");
        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj));
        getP().LockOn();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
    }

    public Alert_Message getAlert_message() {
        return alert_message;
    }

    public Alert_Password getAlert_password() {
        return alert_password;
    }

    public void infoSet(String txt) {
        tv_info.setText(txt);
    }
}
