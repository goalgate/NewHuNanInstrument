package cn.cbdi.hunaninstrument.Project_YanCheng;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.Bean.ReUploadWithBsBean;
import cn.cbdi.hunaninstrument.Bean.SceneKeeper;
import cn.cbdi.hunaninstrument.EventBus.AlarmEvent;
import cn.cbdi.hunaninstrument.EventBus.LockUpEvent;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.EventBus.TemHumEvent;
import cn.cbdi.hunaninstrument.EventBus.USBCopyEvent;
import cn.cbdi.hunaninstrument.Project_XinWeiGuan.XinWeiGuanMainActivity;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.State.OperationState.DoorOpenOperation;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.view.IFaceView;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends RxActivity implements IFaceView {

    protected String TAG = YanChengMainActivity.class.getSimpleName();

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    SPUtils config = SPUtils.getInstance("config");

    SceneKeeper cg_User1 = new SceneKeeper();

    SceneKeeper cg_User2 = new SceneKeeper();

    SceneKeeper unknownUser = new SceneKeeper();

    Disposable checkChange;

    @BindView(R.id.tv_info)
    public TextView tv_info;

    @BindView(R.id.iv_network)
    public ImageView iv_network;

    @BindView(R.id.iv_setting)
    public ImageView iv_setting;

    @BindView(R.id.tv_time)
    public TextView tv_time;

    @BindView(R.id.tv_daid)
    public TextView tv_daid;

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

    public OutputControlPresenter sp = OutputControlPresenter.getInstance();

    public FacePresenter fp = FacePresenter.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        sp.Open();
        firstUse();
        Log.e(TAG, "onCreate");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        fp.useRGBCamera(false);
        fp.FacePresenterSetView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        fp.FacePresenterSetView(null);
        fp.FaceSetNoAction();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sp.WhiteLight(false);
        MediaHelper.mediaRealese();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }


    private void firstUse() {
        if (config.getBoolean("firstUse_ver20", true)) {
            try {
                mdaosession.deleteAll(ReUploadWithBsBean.class);
                mdaosession.deleteAll(ReUploadBean.class);
                mdaosession.deleteAll(Employer.class);
                mdaosession.deleteAll(Keeper.class);
                config.put("firstUse_ver20", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        int temp = 0;
        if (event.getTem() > 10) temp = event.getTem() - 10;
        tv_temperature.setText(temp + "℃");
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
        OpenDoor(event.getLegal());
        if (checkChange != null) {
            checkChange.dispose();
        }
        if (DoorOpenOperation.getInstance().getmDoorOpenOperation().equals(DoorOpenOperation.DoorOpenState.OneUnlock)) {
            DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetLockUpEvent(LockUpEvent event) {
        tv_info.setText("仓库已重新上锁");
        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_mj));
        cg_User1 = new SceneKeeper();
        cg_User2 = new SceneKeeper();
        DoorOpenOperation.getInstance().setmDoorOpenOperation(DoorOpenOperation.DoorOpenState.Locking);

    }


    private ProgressDialog progressDialog;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetUSBCopyEvent(USBCopyEvent event) {
        if(event.getStatus()==1){
            fp.FaceSetNoAction();
            progressDialog = new ProgressDialog(BaseActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
            progressDialog.setMessage("已找到相应数据，正在复制...");
            progressDialog.show();
        }else if(event.getStatus()==2){
            progressDialog.dismiss();
            fp.FaceIdentify_model();
        }

    }


    public abstract void OpenDoor(boolean status);
}
