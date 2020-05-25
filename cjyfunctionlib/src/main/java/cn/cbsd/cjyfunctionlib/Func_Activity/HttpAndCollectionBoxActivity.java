package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.INetDaSocketEvent;
import cn.cbsd.cjyfunctionlib.Func_CollectionBox.DataBuilder;
import cn.cbsd.cjyfunctionlib.Func_CollectionBox.SocketBuilder;
import cn.cbsd.cjyfunctionlib.Func_HttpConnect.NetworkHelper;
import cn.cbsd.cjyfunctionlib.R;
import cn.cbsd.cjyfunctionlib.Tools.DESX;
import cn.cbsd.cjyfunctionlib.Tools.NetInfo;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HttpAndCollectionBoxActivity extends Activity {

    TextView tv_InternetStatus;

    TextView tv_ServerStatus;

    TextView tv_liquidValue;

    TextView tv_gasValue;

    String key;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.http_activity);
        tv_InternetStatus = (TextView) findViewById(R.id.tv_InternetStatus);
        tv_ServerStatus = (TextView) findViewById(R.id.tv_ServerStatus);
        tv_liquidValue = (TextView) findViewById(R.id.tv_liquidValue);
        tv_gasValue = (TextView) findViewById(R.id.tv_gasValue);
        HttpOption();
        CollectionBoxOption();
    }

    private void HttpOption() {
        JSONObject jsonKey = new JSONObject();
        try {
            jsonKey.put("daid", new NetInfo().getMacId());
            jsonKey.put("check", DESX.encrypt(new NetInfo().getMacId()));
//                                    jsonKey.put("daid", "000224-076000-001145");
//                                    jsonKey.put("check", DESX.encrypt("000224-076000-001145"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        key = DESX.encrypt(jsonKey.toString());


        Observable.interval(0, 30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    NetworkHelper.getInternetStatus(this, (status -> {
                        switch (status) {
                            case connected:
                                tv_InternetStatus.setText("设备与外网通信成功");
                                break;
                            case Network_disconnected:
                                tv_InternetStatus.setText("连接网络失败，请检查网线连接状态");
                                break;
                            case Internet_disconnected:
                                tv_InternetStatus.setText("设备网口正常，外网无法通信，请检查网络");
                                break;
                            case Internet_connecting:
                                tv_InternetStatus.setText("等待外网联通结果");
                                break;
                            default:
                                break;
                        }
                    }));
                });
        Observable.interval(2, 60, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> testNet());
    }

    private void testNet() {
        NetworkHelper.testApi().withDataRs("testNet", key, null)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (s.equals("true")) {
                            tv_ServerStatus.setText("服务器通信成功");
                        } else {
                            tv_ServerStatus.setText("服务器通信失败");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private SocketBuilder netDa = null;

    private DataBuilder daData = new DataBuilder();

    private void CollectionBoxOption() {
        netDa = new SocketBuilder()
                .setBuilderNumber(1)
                .setBuilderDATime(1000)
                .builder_open("192.168.12.232", 10000)
                .setBuilderEvent(new INetDaSocketEvent() {
                    @Override
                    public void onOpen(int num, int state) {
                        if (state == 0) {
                            tv_liquidValue.setText("数据采集断开");
                            tv_gasValue.setText("数据采集断开");
                        }
                    }

                    @Override
                    public void onCmd(int num, int cmdType, byte value) {

                    }

                    @Override
                    public void onAI(int num, int cmdType, int[] value) {

                    }
                });

        //设置液位
        daData.getAI(0)
                .setBuilderEnable(true)
                .setBuilderName("液位:")
                .setBuilderUnit("米")
                .setBuilderMinVal(4)
                .setBuilderMaxVal(20)
                .setBuilderMinRange(0)
                .setBuilderMaxRange(5)
                .setSensorAIBuilderPrecision(2)
                .setSensorAIBuilderAlarmMinVal(-1)
                .setSensorAIBuilderAlarmMaxVal(-1)
                .setDataCallback((sensorAI) -> {
                    tv_liquidValue.setText(sensorAI.getName() + sensorAI.getVal() + sensorAI.getUnit());
                });
        //                    0,液位:,米,4,20,0,5,2,-1,-1,-1||1,气体浓度:,%,4,20,0,100,1,-1,20,-1


        //设置有害气体浓度
        daData.getAI(1)
                .setBuilderEnable(true)
                .setBuilderName("有害气体浓度:")
                .setBuilderUnit("%")
                .setBuilderMinVal(4)
                .setBuilderMaxVal(20)
                .setBuilderMinRange(0)
                .setBuilderMaxRange(100)
                .setSensorAIBuilderPrecision(1)
                .setSensorAIBuilderAlarmMinVal(-1)
                .setSensorAIBuilderAlarmMaxVal(20)
                .setDataCallback((sensorAI) ->
                        tv_gasValue.setText(sensorAI.getName() + sensorAI.getVal() + sensorAI.getUnit())
                );

        netDa.bindNetDAM0888Data(daData);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (netDa != null) netDa.close();
    }


}
