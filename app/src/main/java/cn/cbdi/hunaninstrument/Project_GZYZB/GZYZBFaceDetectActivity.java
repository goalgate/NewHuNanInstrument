package cn.cbdi.hunaninstrument.Project_GZYZB;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.components.RxActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.cbdi.hunaninstrument.EventBus.FaceDetectEvent;
import cn.cbdi.hunaninstrument.R;
import cn.cbdi.hunaninstrument.Tool.ActivityCollector;
import cn.cbdi.hunaninstrument.Tool.MediaHelper;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.CardInfoBean;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.view.IFaceView;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Reg_failed;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Reg_success;

public class GZYZBFaceDetectActivity extends RxActivity implements IFaceView {
    private String TAG = GZYZBFaceDetectActivity.class.getSimpleName();

    OutputControlPresenter sp = OutputControlPresenter.getInstance();

    FacePresenter fp = FacePresenter.getInstance();

    Disposable disposableTips;

    Disposable disposableTimer;

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

    CardInfoBean cardInfo = new CardInfoBean();

    Bitmap headBmp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.layout_reg);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        cardInfo.setCardID(bundle.getString("cardId"));
        cardInfo.setName(bundle.getString("name"));
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(30, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待用户操作..."))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));

        disposableTimer = RxTextView.textChanges(tv_timer)
                .debounce(60, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((charSequence) -> {
                    ToastUtils.showLong("抽取特征超时，你可以点击头像图片再次抽取人脸特征");
                    fp.PreviewCease(() -> GZYZBFaceDetectActivity.this.finish());
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(this, previewView, previewView1, textureView);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        MediaHelper.play(MediaHelper.Text.reg_model);
        fp.FacePresenterSetView(this);
        Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> fp.FaceReg(cardInfo.name(), cardInfo.cardId()));
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
        ActivityCollector.removeActivity(this);
        if (disposableTips != null) {
            disposableTips.dispose();
        }
        if (disposableTimer != null) {
            disposableTimer.dispose();
        }
    }

    @Override
    public void onText(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Reg_success)) {
            tv_info.setText(text);
            tv_timer.setText(text);
            sp.greenLight();
            ToastUtils.showLong("抽取特征成功");
            fp.PreviewCease(() -> GZYZBFaceDetectActivity.this.finish());
        } else if (resultType.equals(Reg_failed)) {
            tv_info.setText(text);
            tv_timer.setText(text);
            sp.redLight();
            ToastUtils.showLong("抽取特征失败，你可以点击头像图片再次抽取人脸特征");
            fp.PreviewCease(() -> GZYZBFaceDetectActivity.this.finish());

        }
    }

    @Override
    public void onUser(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, User user) {
        if (resultType.equals(Reg_success)) {
            headBmp = fp.getGlobalBitmap();

            EventBus.getDefault().post(new FaceDetectEvent(headBmp, user.getUserId()));

        }
    }

    @Override
    public void onBitmap(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, Bitmap bitmap) {

    }

    @Override
    public void onLivenessModel(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, LivenessModel model) {

    }
}
