package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;
import com.baidu.idl.main.facesdk.utils.FileUitls;
import com.baidu.idl.main.facesdk.utils.ImageUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.view.IFaceView;
import cn.cbsd.cjyfunctionlib.R;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Identify_failed;
import static cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter.FaceResultType.Identify_success;


public class FaceDetectActivity extends Activity implements IFaceView {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

    public FacePresenter fp = FacePresenter.getInstance();

    Button btn_faceReg;

    Button btn_faceBmpReg;

    Button btn_faceDelete;

    Button btn_faceVerify;

    Button btn_faceIdentifyModel;

    Button btn_faceIdentify;

    Button btn_faceNormal;

    Button btn_faceCount;

    AutoTexturePreviewView previewView;

    AutoTexturePreviewView previewView1;

    TextureView textureView;

    TextView tv_info;

    ImageView iv_headphotoRGB;

    ImageView iv_headphotoIR;

    String userName = "王振文";

    String userInfo = "441302199308100538";

    Bitmap userBmp;

    Bitmap songBmp;

    private Disposable disposableTips;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facedetect_activity);
        userBmp = BitmapFactory.decodeResource(getResources(), R.drawable.user);
        songBmp = BitmapFactory.decodeResource(getResources(),R.drawable.song);
        btn_faceReg = (Button) findViewById(R.id.btn_faceReg);
        btn_faceReg.setOnClickListener(mOnClickListener);
        btn_faceBmpReg = (Button) findViewById(R.id.btn_faceBmpReg);
        btn_faceBmpReg.setOnClickListener(mOnClickListener);
        btn_faceDelete = (Button) findViewById(R.id.btn_faceDelete);
        btn_faceDelete.setOnClickListener(mOnClickListener);
        btn_faceCount = (Button) findViewById(R.id.btn_faceCount);
        btn_faceCount.setOnClickListener(mOnClickListener);
        btn_faceIdentify = (Button) findViewById(R.id.btn_faceIdentify);
        btn_faceIdentify.setOnClickListener(mOnClickListener);
        btn_faceIdentifyModel = (Button) findViewById(R.id.btn_faceIdentifyModel);
        btn_faceIdentifyModel.setOnClickListener(mOnClickListener);
        btn_faceVerify = (Button) findViewById(R.id.btn_faceVerify);
        btn_faceVerify.setOnClickListener(mOnClickListener);
        btn_faceNormal = (Button) findViewById(R.id.btn_faceNormal);
        btn_faceNormal.setOnClickListener(mOnClickListener);
        previewView = (AutoTexturePreviewView) findViewById(R.id.preview_view);
        previewView1 = (AutoTexturePreviewView) findViewById(R.id.preview_view1);
        textureView = (TextureView) findViewById(R.id.texture_view);
        tv_info = (TextView) findViewById(R.id.tv_info);
        iv_headphotoIR = (ImageView) findViewById(R.id.iv_headphotoIR);
        iv_headphotoRGB = (ImageView) findViewById(R.id.iv_headphotoRGB);
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(3, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("信息展示"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
    }

    @Override
    protected void onStart() {
        super.onStart();
        fp.CameraPreview(this, previewView, previewView1, textureView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fp.FacePresenterSetView(this);
        fp.FaceIdentifyReady();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fp.FacePresenterSetView(null);
        fp.FaceSetNoAction();
//        fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposableTips != null) {
            disposableTips.dispose();
        }
    }

    @Override
    public void onBackPressed() {
        fp.PreviewCease(() -> finish());
    }

    View.OnClickListener mOnClickListener = view -> {
        int vid = view.getId();
        if (vid == R.id.btn_faceReg) {
            fp.FaceVerifyAndReg(userName, userInfo, userBmp);
        } else if (vid == R.id.btn_faceBmpReg) {
            new Thread(()->{
                if (fp.FaceRegByBase64(userName,userInfo , FileUtils.bitmapToBase64(userBmp))) {
                    runOnUiThread(()->{
                        tv_info.setText("人脸特征存储成功");
                    });
                } else {
                    runOnUiThread(()->{
                        tv_info.setText("人脸特征存储失败");
                    });
                }
            }).start();
        } else if (vid == R.id.btn_faceDelete) {
            fp.FaceDeleteByUserName(userName);
        } else if (vid == R.id.btn_faceVerify) {
            fp.FaceVerify(userName, userInfo, userBmp);
        } else if (vid == R.id.btn_faceIdentifyModel) {
            fp.FaceIdentify_model();
        } else if (vid == R.id.btn_faceIdentify) {
            fp.FaceIdentify();
        } else if (vid == R.id.btn_faceNormal) {
            new Thread(()->{
                fp.FaceUpdate(songBmp,userName,new UserInfoManager.UserInfoListener(){
                    public void updateImageSuccess(Bitmap bitmap) {
                        runOnUiThread(()->{
                            tv_info.setText("人脸特征更新成功");
                        });
                    }

                    public void updateImageFailure(String message) {
                        runOnUiThread(()->{
                            tv_info.setText("人脸特征更新失败");

                        });
                    }
                });
            }).start();
        } else if (vid == R.id.btn_faceCount) {
            fp.FaceIdentifyReady();
        }
    };

    @Override
    public void onText(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, String text) {
        switch (resultType) {
            case IMG_MATCH_IMG_Score:
                break;
            case Identify_failed:
                Bitmap bitmap = fp.getGlobalBitmap();
//                File faceDir = FileUitls.getFaceDirectory();
//                if (faceDir != null) {
//                    String imageName = "IMG_" + formatter.format(new Date(System.currentTimeMillis())) + ".png";
//                    File file = new File(faceDir, imageName);
//                    if (!file.exists()) {
//                        try {
//                            file.createNewFile();
//                            FileOutputStream fos = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                            fos.flush();
//                            fos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    ImageUtils.resize(bitmap, file, 300, 300);
//                } else {
//                    Toast.makeText(this, "人脸目录未找到", Toast.LENGTH_SHORT).show();
//                }
                tv_info.setText(text);

                break;
            default:
                tv_info.setText(text);
                break;
        }
    }

    @Override
    public void onBitmap(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        if (resultType == FacePresenter.FaceResultType.headphotoRGB) {
            iv_headphotoRGB.setImageBitmap(bitmap);
            Observable.timer(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> iv_headphotoRGB.setImageBitmap(null));
        } else if (resultType == FacePresenter.FaceResultType.headphotoIR) {
            iv_headphotoIR.setImageBitmap(bitmap);
            Observable.timer(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> iv_headphotoIR.setImageBitmap(null));
        }

    }

    @Override
    public void onUser(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, User user) {

    }


    @Override
    public void onLivenessModel(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, LivenessModel model) {
        if (resultType.equals(Identify_failed)) {
            fp.FaceRegOrUpdateByFeature(userName,userInfo,model.getFeature(),false);

        }
    }
}
