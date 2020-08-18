package cn.cbdi.hunaninstrument;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.ui.Activation;

import com.bigkoo.alertview.AlertView;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.Config.YanChengConfig;
import cn.cbdi.hunaninstrument.Tool.AssetsUtils;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.presenter.FingerPrintPresenter;
import cn.cbsd.cjyfunctionlib.R;
import cn.cbsd.cjyfunctionlib.Tools.DESX;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import cn.cbsd.cjyfunctionlib.Tools.NetInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import jxl.Sheet;
import jxl.Workbook;


public class FaceInitActivity extends RxActivity {

    private Handler handler = new Handler(Looper.getMainLooper());

    Activation activation;

    private SPUtils config = SPUtils.getInstance("config");

    String daid = new NetInfo().getMacId();

//    String daid = "042162-079043-230210";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faceinit);

        Intent intent = new Intent(FaceInitActivity.this, AppInit.getInstrumentConfig().getUpdateService());
        startService(intent);

        try {
            File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
            String txtForKey = FileUtils.readFile2String(key);
            copyToClipboard(this, txtForKey);
            CheckLicenseKey("excel" + File.separator + "授权激活码替换列表.xls", txtForKey);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dissDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                activation.dismissActivationDialog();
            }
        });
    }

    public static void SaveTxt(String str) {
        try {
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory() + File.separator + "key.txt");//SD卡中的路径
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


    private void appStart() {
        if (AppInit.getInstrumentConfig().getDev_prefix().startsWith("800")) {
            if (config.getBoolean("firstStart", true)) {
                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".StartActivity");
                return;
            } else {
                if (AppInit.getInstrumentConfig().DoorMonitorChosen()) {
                    if (config.getBoolean("isHongWai", true)) {
                        AppInit.getInstrumentConfig().setHongWai(true);
                    } else {
                        AppInit.getInstrumentConfig().setHongWai(false);
                    }
                }
                ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
                return;
            }
        } else {
            if (AppInit.getInstrumentConfig().getClass().getName().equals(YanChengConfig.class.getName())) {
                JSONObject jsonKey = new JSONObject();
                try {
                    jsonKey.put("daid", daid);
                    jsonKey.put("check", DESX.encrypt(daid));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                config.put("daid", daid);
                config.put("key", DESX.encrypt(jsonKey.toString()));
                config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
            }
            if (config.getBoolean("firstStart", true)) {
                JSONObject jsonKey = new JSONObject();
                try {
                    jsonKey.put("daid", daid);
                    jsonKey.put("check", DESX.encrypt(daid));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                config.put("firstStart", false);
                config.put("daid", daid);
                config.put("key", DESX.encrypt(jsonKey.toString()));
                config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
                AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib", "wltlib");
            }

            if (AppInit.getInstrumentConfig().DoorMonitorChosen() && config.getBoolean("SetDoorMonitor", true)) {
                new AlertView("选择门感应方式", null, null, new String[]{"门磁", "红外对射"}, null, FaceInitActivity.this, AlertView.Style.Alert, (o, position) -> {
                    if (position == 0) {
                        config.put("isHongWai", false);
                        AppInit.getInstrumentConfig().setHongWai(false);
                    } else if (position == 1) {
                        config.put("isHongWai", true);
                        AppInit.getInstrumentConfig().setHongWai(true);
                    }
                    config.put("SetDoorMonitor", false);
                    if (AppInit.getInstrumentConfig().fingerprint()) {
                        FingerPrintPresenter.getInstance().fpInit(AppInit.getContext());
                        FingerPrintPresenter.getInstance().fpOpen();
                        Observable.timer(3, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .compose(FaceInitActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                                .subscribe((l) -> ActivityUtils.startActivity(getPackageName(),
                                        getPackageName() + AppInit.getInstrumentConfig().getMainActivity()));
                    } else {
                        ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
                    }
                }).show();
            } else {
                if (AppInit.getInstrumentConfig().DoorMonitorChosen()) {
                    if (config.getBoolean("isHongWai", true)) {
                        AppInit.getInstrumentConfig().setHongWai(true);
                    } else {
                        AppInit.getInstrumentConfig().setHongWai(false);
                    }
                }


                if (AppInit.getInstrumentConfig().fingerprint()) {
                    FingerPrintPresenter.getInstance().fpInit(AppInit.getContext());
                    FingerPrintPresenter.getInstance().fpOpen();
                    Observable.timer(3, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .compose(FaceInitActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                            .subscribe((l) -> ActivityUtils.startActivity(getPackageName(),
                                    getPackageName() + AppInit.getInstrumentConfig().getMainActivity()));
                } else {
                    ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());

                }
            }
        }

    }

    private void CheckLicenseKey(String xlsName, String licenseKey) {
        Observable.just(xlsName).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                AssetManager assetManager = getAssets();
                try {
                    Workbook workbook = Workbook.getWorkbook(assetManager.open(s));
                    for (Sheet sheet : workbook.getSheets()) {
                        int sheetRows = sheet.getRows();
                        for (int i = 0; i < sheetRows; i++) {
                            if (licenseKey.equals(sheet.getCell(1, i).getContents())) {
                                workbook.close();
                                return Observable.just(sheet.getCell(2, i).getContents());
                            }
                        }
                    }
                    workbook.close();
                    return Observable.just("读写完毕");
                } catch (Exception e) {
                    return Observable.just("读写失败");
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((s -> {
                    String result = (String) s;
                    if (s.startsWith("读写")) {
                        FacePresenter.getInstance().FaceInit(this, new SdkInitListener() {
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
                                                        appStart();
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
                                            appStart();
                                        });
                            }

                            @Override
                            public void initModelFail(int errorCode, String msg) {

                            }
                        });

                    } else {
                        if (!isNetworkOnline()) {
                            FacePresenter.getInstance().FaceInit(this, new SdkInitListener() {
                                @Override
                                public void initStart() {

                                }

                                @Override
                                public void initLicenseSuccess() {

                                }

                                @Override
                                public void initLicenseFail(int errorCode, String msg) {
                                    // 如果授权失败，跳转授权页面
                                    handler.post(() -> ToastUtils.showLong("设备未联网导致人脸号更新失败"));

//                                    activation = new Activation(FaceInitActivity.this);
//                                    activation.show();
//                                    activation.setActivationCallback(new Activation.ActivationCallback() {
//                                        @Override
//                                        public void callback(int code, String response, String licenseKey) {
//                                            if (code == 0) {
//                                                Log.e("FaceSDK", "授权成功");
//                                                SaveTxt(licenseKey);
//                                                dissDialog();
//                                                Observable.timer(3, TimeUnit.SECONDS)
//                                                        .observeOn(AndroidSchedulers.mainThread())
//                                                        .subscribe((l) -> {
//                                                            appStart();
//                                                        });
//                                                return;
//                                            } else {
//                                                Log.e("FaceSDK", "授权失败:" + response);
//                                            }
//                                        }
//                                    });
                                }

                                @Override
                                public void initModelSuccess() {
                                    Observable.timer(3, TimeUnit.SECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe((l) -> {
                                                appStart();
                                            });
                                }

                                @Override
                                public void initModelFail(int errorCode, String msg) {

                                }
                            });
                        } else {
                            String real_licenseKey = result;
                            handler.post(() -> ToastUtils.showLong("人脸激活号正在转换，请稍候..."));
                            activation = new Activation(this);
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
                                                    appStart();
                                                });
                                        return;
                                    } else {
                                        Log.e("FaceSDK", "授权失败:" + response);
                                    }
                                }
                            });
                            activation.keyEt.setText(real_licenseKey);
                            activation.activateBtn.performClick();
                        }
                    }
                }));
    }

    public static boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 114.114.114.114");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

}
