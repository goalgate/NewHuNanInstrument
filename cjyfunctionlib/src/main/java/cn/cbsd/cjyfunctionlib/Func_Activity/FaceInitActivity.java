package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import com.baidu.idl.main.facesdk.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.ui.Activation;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;

import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.FaceImpl2;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.R;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class FaceInitActivity extends Activity {

    private Handler handler = new Handler(Looper.getMainLooper());

    Activation activation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faceinit);
        try {
            File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
            copyToClipboard(this, FileUtils.readFile2String(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        FacePresenter.getInstance().FaceInit(new FaceImpl2(),this, new SdkInitListener() {
            @Override
            public void initStart() {

            }

            @Override
            public void initLicenseSuccess() {

            }

            @Override
            public void initLicenseFail(int errorCode, String msg) {
                // 如果授权失败，跳转授权页面
                activation = new Activation(FaceInitActivity.this);
                activation.show();
                activation.setActivationCallback(new Activation.ActivationCallback() {
                    @Override
                    public void callback(int code, String response, String licenseKey) {
                        if (code == 0) {
                            Log.e("FaceSDK", "授权成功");
                            SaveTxt(licenseKey);
                            dissDialog();
                            Observable.timer(3, TimeUnit.SECONDS)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe((l) -> {
                                        Intent intent = new Intent(FaceInitActivity.this, FaceDetectActivity.class);
                                        startActivity(intent);

                                    });
                            return;
                        } else {
                            Log.e("FaceSDK", "授权失败:" + response);
                        }
                    }
                });
            }

            @Override
            public void initModelSuccess() {
                Observable.timer(3, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((l) -> {
                            Intent intent = new Intent(FaceInitActivity.this, FaceDetectActivity.class);
                            startActivity(intent);
                        });
            }

            @Override
            public void initModelFail(int errorCode, String msg) {

            }
        });
    }

    private void dissDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                activation.dismissActivationDialog();
            }
        });
    }
    public static void SaveTxt(String str){
        try {
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory()+ File.separator + "key.txt");//SD卡中的路径
            fw.flush();
            fw.write(str);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }



}
