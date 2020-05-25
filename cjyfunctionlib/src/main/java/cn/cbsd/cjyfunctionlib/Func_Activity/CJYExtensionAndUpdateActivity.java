package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.ApkUtils;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.Incremental_Update;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.UpdateConstant;
import cn.cbsd.cjyfunctionlib.R;
import cn.cbsd.cjyfunctionlib.Tools.NetInfo;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CJYExtensionAndUpdateActivity extends Activity {

    Button btn_reboot;

    Button btn_static;

    Button btn_dhcp;

    Button btn_eth0;

    Button btn_update;

    TextView tv_mac;

    boolean eth0 = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cjyextension_activity);
        btn_reboot = (Button) findViewById(R.id.btn_reboot);
        btn_reboot.setOnClickListener(mOnClickListener);
        btn_static = (Button) findViewById(R.id.btn_static);
        btn_static.setOnClickListener(mOnClickListener);
        btn_dhcp = (Button) findViewById(R.id.btn_dhcp);
        btn_dhcp.setOnClickListener(mOnClickListener);
        btn_eth0 = (Button) findViewById(R.id.btn_eth0);
        btn_eth0.setOnClickListener(mOnClickListener);
        btn_update = (Button) findViewById(R.id.btn_update);
        btn_update.setOnClickListener(mOnClickListener);
        tv_mac = (TextView) findViewById(R.id.tv_mac);
        tv_mac.setText("WIFIMac:" + new NetInfo().getWifiMac() + "\n" + "ethMac" + new NetInfo().getMac());
        Incremental_Update.CopySourceFile(this);

    }

    View.OnClickListener mOnClickListener = view -> {
        int vid = view.getId();
        if (vid == R.id.btn_reboot) {
            CJYHelper.getInstance(this).reboot();
        } else if (vid == R.id.btn_static) {
            CJYHelper.getInstance(this).setStaticEthIPAddress("192.168.1.122", "192.168.1.1", "255.255.255.0", "192.168.1.1", "192" +
                    ".168.1.1");
        } else if (vid == R.id.btn_dhcp) {
            CJYHelper.getInstance(this).setDhcpIpAddress();
        } else if (vid == R.id.btn_eth0) {
            CJYHelper.getInstance(this).ethEnabled(eth0);
            eth0 = !eth0;
        } else if (vid == R.id.btn_update) {
            Incremental_Update.Update(this);
        }
    };


}
