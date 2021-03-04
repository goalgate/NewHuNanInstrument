package cn.cbdi.hunaninstrument.Project_HuNan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Bean.ReUploadBean;
import cn.cbdi.hunaninstrument.EventBus.USBCopyEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.Tool.MySocketHelper;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_Card.presenter.IDCardPresenter;
import cn.cbsd.cjyfunctionlib.Func_Card.view.IIDCardView;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.view.IFaceView;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Func_WebSocket.ServerManager;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class BaseActivity extends RxActivity implements IFaceView, IIDCardView {

    protected String TAG = HuNanMainActivity.class.getSimpleName();

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    SPUtils config = SPUtils.getInstance("config");

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

    @BindView(R.id.tv_krq)
    public TextView tv_krq;

    public OutputControlPresenter sp = OutputControlPresenter.getInstance();

    public FacePresenter fp = FacePresenter.getInstance();

    public IDCardPresenter idp = IDCardPresenter.getInstance();

    String ver = "sync1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        firstUse();
        idp.idCardOpen(AppInit.getContext());
        sp.Open();
        if(AppInit.getInstrumentConfig().Remote()){
            ServerManager.getInstance().Start(4545, new MySocketHelper(this));
        }
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
        idp.IDCardPresenterSetView(this);
        fp.useRGBCamera(false);
//        MediaHelper.play(MediaHelper.Text.normal_model);
        Observable.timer(1, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    AppInit.getInstrumentConfig().readCard();
                });
        fp.FacePresenterSetView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        fp.FacePresenterSetView(null);
        idp.IDCardPresenterSetView(null);
        AppInit.getInstrumentConfig().stopReadCard();
        sp.WhiteLight(false);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FacePresenter.getInstance().FaceIdentify_model();
        Log.e("Activity", "restart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sp.WhiteLight(false);
        idp.idCardClose();
        MediaHelper.mediaRealese();
        if(AppInit.getInstrumentConfig().Remote()){
            ServerManager.getInstance().Stop();
        }
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }


    private void firstUse() {
        if (config.getBoolean("firstUse_ver19", true)) {
            try {
                mdaosession.deleteAll(ReUploadBean.class);
                mdaosession.deleteAll(Employer.class);
                mdaosession.deleteAll(Keeper.class);
                config.put("firstUse_ver19", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ProgressDialog progressDialog;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetUSBCopyEvent(USBCopyEvent event) {
        if (event.getStatus() == 1) {
            fp.FaceSetNoAction();
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
            progressDialog.setMessage("已找到相应数据，正在复制...");
            progressDialog.show();
        } else if (event.getStatus() == 2) {
            progressDialog.dismiss();
            fp.FaceIdentify_model();
        }

    }

}
