package cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.ys.myapi.MyManager;

import cn.cbsd.cjyfunctionlib.Tools.FileUtils;

public class CJY_Apollo_Impl extends CJYHelper {

    private static CJY_Apollo_Impl wzwManager;

    Context mContext;

    public static final String STATICIP = "StaticIp";

    public static final String DHCP = "DHCP";

    public static final String ethernet = "ethernet";

    public static CJY_Apollo_Impl getInstance(Context context) {
        if (wzwManager == null) {
            wzwManager = new CJY_Apollo_Impl(context);
        }
        return wzwManager;
    }

    private CJY_Apollo_Impl(Context context) {
        this.mContext = context;
    }

    @Override
    public void HelperInit() {}

    @Override
    public void HelperRelease() {
        mContext = null;
    }

    @Override
    public void setTime(int year, int month, int day, int hour, int minute, int second) {

    }

    @Override
    public void reboot() {
        excuseCmd("vm -c 'reboot'");
    }

    @Override
    public void setStaticEthIPAddress(String IPaddr, String gateWay, String mask, String dns1, String dns2) {
        FileUtils.writeFileSdcard(ethernet, STATICIP);


    }

    @Override
    public String getAndroidDisplay() {
        return Build.DEVICE;
    }

    @Override
    public void setDhcpIpAddress() {

        FileUtils.writeFileSdcard(ethernet, DHCP);
    }

    @Override
    public String getEthMode() {
        try {
            return FileUtils.readFileSdcard(ethernet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    @Override
    public void ethEnabled(boolean status) {

    }

    @Override
    public void turnOffBackLight() {


    }

    @Override
    public void turnOnBackLight() {
    }


    public  float readCPUTem(int i) {
        float result = -1;
        try {
            byte[] receive = new byte[5];
            Process su;
            String cmd = "cat /sys/class/thermal/thermal_zone" + i + "/temp";
            su = Runtime.getRuntime().exec(cmd);
            su.getInputStream().read(receive);
            result = (float) (Float.parseFloat(new String(receive)) / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

}
