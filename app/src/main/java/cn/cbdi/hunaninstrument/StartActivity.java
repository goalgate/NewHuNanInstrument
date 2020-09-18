package cn.cbdi.hunaninstrument;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.hunaninstrument.Config.NMGFB_NewConfig;
import cn.cbdi.hunaninstrument.Tool.AssetsUtils;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.presenter.FingerPrintPresenter;
import cn.cbsd.cjyfunctionlib.Tools.DESX;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by zbsz on 2017/12/8.
 */

public class StartActivity extends Activity {

    private String regEx = "^\\d{4}$";

    private SPUtils config = SPUtils.getInstance("config");

    Pattern pattern = Pattern.compile(regEx);

    private static final String TAG = StartActivity.class.getSimpleName() + ">>>>>";

    @BindView(R.id.dev_prefix)
    TextView dev_prefix;

    @BindView(R.id.devid_input)
    EditText dev_suffix;


    @OnClick(R.id.next)
    void next() {
        if (pattern.matcher(dev_suffix.getText().toString()).matches()) {


            config.put("firstStart", false);
            config.put("ServerId", AppInit.getInstrumentConfig().getServerId());
            config.put("daid", AppInit.getInstrumentConfig().getDev_prefix() + dev_suffix.getText().toString());

            ToastUtils.showLong("设备ID设置成功");
            AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib","wltlib");
            if(AppInit.getInstrumentConfig().getClass().getName().equals(NMGFB_NewConfig.class.getName())){

                if(("http://113.140.1.138:8890/".equals(config.getString("ServerId")))){
                    config.put("ServerId","http://113.140.1.138:8892/");
                }
                JSONObject jsonKey = new JSONObject();
                try {
                    jsonKey.put("daid", config.getString("daid"));
                    jsonKey.put("check", DESX.encrypt(config.getString("daid")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                config.put("key", DESX.encrypt(jsonKey.toString()));
            }
            if (AppInit.getInstrumentConfig().DoorMonitorChosen() && config.getBoolean("SetDoorMonitor", true)) {
                new AlertView("选择门感应方式", null, null, new String[]{"门磁", "红外对射"}, null, StartActivity.this, AlertView.Style.Alert, (o, position) -> {
                    if (position == 0) {
                        config.put("isHongWai", false);
                        AppInit.getInstrumentConfig().setHongWai(false);
                    } else if (position == 1) {
                        config.put("isHongWai", true);
                        AppInit.getInstrumentConfig().setHongWai(true);
                    }
                    config.put("SetDoorMonitor", false);
                    ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
                    StartActivity.this.finish();
                }).show();
            }else{
                ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getInstrumentConfig().getMainActivity());
                StartActivity.this.finish();
            }

        } else {
            ToastUtils.showLong("设备ID输入错误，请重试");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_form);
        ButterKnife.bind(this);
        dev_prefix.setText(AppInit.getInstrumentConfig().getDev_prefix());
    }


}
