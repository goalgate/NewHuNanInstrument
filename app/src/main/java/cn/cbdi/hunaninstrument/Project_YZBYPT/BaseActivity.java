package cn.cbdi.hunaninstrument.Project_YZBYPT;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.trello.rxlifecycle2.components.RxActivity;


import butterknife.BindView;
import cn.cbdi.hunaninstrument.AppInit;

import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;


public abstract class BaseActivity extends RxActivity  {

    private String TAG = BaseActivity.class.getSimpleName();

    public FacePresenter fp = FacePresenter.getInstance();

    public OutputControlPresenter sp = OutputControlPresenter.getInstance();

    SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        sp.Open();
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        MediaHelper.play(MediaHelper.Text.normal_model);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        AppInit.getInstrumentConfig().stopReadCard();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaHelper.mediaRealese();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }
}
