package cn.cbdi.hunaninstrument.Alert;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Config.BaseConfig;
import cn.cbdi.hunaninstrument.Config.GDYZB_Config;
import cn.cbdi.hunaninstrument.Config.GZYZB_Config;
import cn.cbdi.hunaninstrument.Config.HuNanConfig;
import cn.cbdi.hunaninstrument.Config.NMGFB_NewConfig;
import cn.cbdi.hunaninstrument.Config.NMGYZB_Config;
import cn.cbdi.hunaninstrument.Config.XinWeiGuan_Config;
import cn.cbdi.hunaninstrument.Config.YZBYPT_Config;
import cn.cbdi.hunaninstrument.Config.YanChengConfig;
import cn.cbdi.hunaninstrument.EventBus.NetworkEvent;
import cn.cbdi.hunaninstrument.Project_XinWeiGuan.ParsingTool;
import cn.cbdi.hunaninstrument.Retrofit.RetrofitGenerator;
import cn.cbdi.hunaninstrument.Tool.DAInfo;
import cn.cbdi.hunaninstrument.R;

import cn.cbdi.hunaninstrument.Tool.SafeCheck;
import cn.cbdi.hunaninstrument.Tool.ServerConnectionUtil;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class Alert_Server {

    private Context context;

    private SPUtils config = SPUtils.getInstance("config");

    BaseConfig ins_type = AppInit.getInstrumentConfig();

    int count = 5;

    String url;
    private AlertView inputServerView;
    private EditText etName;
    private ImageView QRview;
    private Button connect;

    public Alert_Server(Context context) {
        this.context = context;
    }

    public void serverInit(final Server_Callback callback) {
        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.inputserver_form, null);
        etName = (EditText) extView1.findViewById(R.id.server_input);
        QRview = (ImageView) extView1.findViewById(R.id.QRimage);
        connect = (Button) extView1.findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etName.getText().toString().replaceAll(" ", "").endsWith("/")) {
                    url = etName.getText().toString() + "/";
                } else {
                    url = etName.getText().toString();
                }
                if (AppInit.getInstrumentConfig().getClass().getName().equals(HuNanConfig.class.getName())) {
                    new RetrofitGenerator().getHnmbyApi(url).withDataRs("testNet", config.getString("key"), null)
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<String>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(String s) {
                                    try {
                                        if (s.equals("true")) {
                                            config.put("ServerId", url);
                                            ToastUtils.showLong("连接服务器成功,请点击确定立即启用");
                                            callback.setNetworkBmp();
                                        } else {
                                            ToastUtils.showLong("连接服务器失败");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    ToastUtils.showLong("服务器连接失败");
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                } else if (AppInit.getInstrumentConfig().getClass().getName().equals(XinWeiGuan_Config.class.getName()) ||
                        AppInit.getInstrumentConfig().getClass().getName().equals(YanChengConfig.class.getName()) ||
                        AppInit.getInstrumentConfig().getClass().getName().equals(NMGFB_NewConfig.class.getName()) ||
                        AppInit.getInstrumentConfig().getClass().getName().equals(GDYZB_Config.class.getName())) {
                    new RetrofitGenerator().getXinWeiGuanApi(url).noData("testNet", config.getString("key"))
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<ResponseBody>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(ResponseBody responseBody) {
                                    try {
                                        String s = ParsingTool.extractMainContent(responseBody);
                                        if (s.equals("true")) {
                                            config.put("ServerId", url);
                                            ToastUtils.showLong("连接服务器成功,请点击确定立即启用");
                                            callback.setNetworkBmp();
                                        } else {
                                            ToastUtils.showLong("连接服务器失败");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    ToastUtils.showLong("服务器连接失败");
                                }

                                @Override
                                public void onComplete() {

                                }
                            });

                } else if (AppInit.getInstrumentConfig().getClass().getName().equals(NMGYZB_Config.class.getName())) {
                    HashMap<String, String> paramsMap = new HashMap<String, String>();
                    SafeCheck safeCheck = new SafeCheck();
                    safeCheck.setURL(config.getString("ServerId"));
                    paramsMap.put("daid", config.getString("daid"));
                    paramsMap.put("pass", safeCheck.getPass(config.getString("daid")));
                    paramsMap.put("dataType", "test");
                    RetrofitGenerator.getNMGYZBApi().GeneralUpdata(paramsMap)
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<String>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(String s) {
                                    if (s.startsWith("true")) {
                                        config.put("ServerId", url);
                                        ToastUtils.showLong("连接服务器成功,请点击确定立即启用");
                                        callback.setNetworkBmp();
                                    } else {
                                        ToastUtils.showLong("服务器连接失败");
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    ToastUtils.showLong("服务器连接失败");
                                }

                                @Override
                                public void onComplete() {

                                }
                            });

                } else if (AppInit.getInstrumentConfig().getClass().getName().equals(YZBYPT_Config.class.getName()) ||
                        (AppInit.getInstrumentConfig().getClass().getName().equals(GZYZB_Config.class.getName()))) {
                    new RetrofitGenerator().getYzbApi(url).withDataRs("testNet", config.getString("key"), null)
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<String>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(String s) {
                                    if (s.startsWith("true")) {
                                        config.put("ServerId", url);
                                        ToastUtils.showLong("连接服务器成功,请点击确定立即启用");
                                        callback.setNetworkBmp();
                                    } else {
                                        ToastUtils.showLong("服务器连接失败");
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    ToastUtils.showLong("服务器连接失败");

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                } else {
                    new ServerConnectionUtil().post(url + AppInit.getInstrumentConfig().getUpDataPrefix() + "daid=" + config.getString("daid") + "&dataType=test", url
                            , new ServerConnectionUtil.Callback() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null) {
                                        if (response.startsWith("true")) {
                                            config.put("ServerId", url);
                                            ToastUtils.showLong("连接服务器成功");
                                            callback.setNetworkBmp();
                                        } else {
                                            ToastUtils.showLong("设备验证错误");
                                        }
                                    } else {
                                        ToastUtils.showLong("服务器连接失败");
                                    }
                                }
                            });
                }

            }
        });
        inputServerView = new AlertView("服务器设置", null, "取消", new String[]{"确定"}, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    Observable.interval(0, 1, TimeUnit.SECONDS)
                            .take(count + 1)
                            .map(new Function<Long, Long>() {
                                @Override
                                public Long apply(@NonNull Long aLong) throws Exception {
                                    return count - aLong;
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onNext(@NonNull Long aLong) {
                                    ToastUtils.showLong(aLong + "秒后重新开机保存设置");
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    CJYHelper.getInstance(context).reboot();
                                }
                            });
                }
            }
        });
        inputServerView.addExtView(extView1);
    }


    public void show() {
        Bitmap mBitmap = null;
        etName.setText(config.getString("ServerId"));
        DAInfo di = new DAInfo();
        try {
//                di.setId(config.getString("devid"));
            di.setId(config.getString("daid"));
            di.setName(ins_type.getName());
            di.setModel(ins_type.getModel());
            di.setPower(ins_type.getPower());
            di.setSoftwareVer(AppUtils.getAppVersionName());
            di.setProject(ins_type.getProject());
            File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
            di.setLicence(FileIOUtils.readFile2String(key));
            mBitmap = di.daInfoBmp();
        } catch (Exception ex) {
        }
        if (mBitmap != null) {
            QRview.setImageBitmap(mBitmap);
        }
        inputServerView.show();
    }

    public interface Server_Callback {
        void setNetworkBmp();
    }
}
