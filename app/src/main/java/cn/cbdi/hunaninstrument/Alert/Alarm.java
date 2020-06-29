package cn.cbdi.hunaninstrument.Alert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;

import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.State.DoorState.WarehouseDoor;
import cn.cbdi.hunaninstrument.State.LockState.Lock;

import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class Alarm {

    private TextView alarmText;

    private AlertView alert;

    private ViewGroup alarmView;

    private Context context;

    private boolean networkIsKnown = false;

    private static Alarm instance = null;

    public static Alarm getInstance(Context context, OnItemClickListener onItemClickListener) {
        if (instance == null) {
            instance = new Alarm(context, onItemClickListener);
        }
        return instance;
    }

    private Alarm(Context context, OnItemClickListener onItemClickListener) {
        this.context = context;
        alarmView = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.alarm_text, null);
        alarmText = (TextView) alarmView.findViewById(R.id.alarmText);
        alert = new AlertView("", null, null, new String[]{"确定"}, null, context, AlertView.Style.Alert, onItemClickListener);
        alert.addExtView(alarmView);
    }


    public void networkAlarm(boolean networkState, networkCallback callback) {
        if (!networkState) {
            if (!networkIsKnown) {
                OutputControlPresenter.getInstance().buzz(IOutputControl.Hex.H0);
                alarmText.setText("设备服务器连接失败,请检查网络,点击确定可继续使用");
                callback.onTextBack("设备服务器连接失败,请检查网络,点击确定可继续使用");
                alert.show();
            } else {
                callback.onIsKnown();
            }
        } else {
            callback.onIsKnown();
        }
    }


    public void messageAlarm(String msg) {
        alarmText.setText(msg);
        alert.show();
    }

    public void messageDelay(String msg) {
        alarmText.setText(msg);
        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> alert.show());

    }

    public void setKnown(boolean known) {
        networkIsKnown = known;
    }

    public interface doorCallback {
        void onTextBack(String msg);

        void onSucc();
    }

    public interface networkCallback {
        void onIsKnown();

        void onTextBack(String msg);
    }

    public void doorAlarm(doorCallback callback) {
        if (WarehouseDoor.getInstance().getMdoorState().equals(Door.DoorState.State_Open)) {
            if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Unlock)) {
//        if (WarehouseDoor.getInstance().getDoorState().getClass().getName().equals(State_Open.class.getName())) {
//            if (Lock.getInstance().getLockState().getClass().getName().equals(State_Unlock.class.getName())) {
                alarmText.setText("门磁已打开,如需撤防请先闭合门磁");
                callback.onTextBack("门磁已打开,如需撤防请先闭合门磁");
            } else {
                alarmText.setText("仓库已设防,但门磁未闭合,请检查门磁状态");
                callback.onTextBack("仓库已设防,但门磁未闭合,请检查门磁状态");
            }
            OutputControlPresenter.getInstance().buzz(IOutputControl.Hex.H0);
            alert.show();
        } else {
            callback.onSucc();
        }
    }

    public void release() {
        instance = null;
    }
}
