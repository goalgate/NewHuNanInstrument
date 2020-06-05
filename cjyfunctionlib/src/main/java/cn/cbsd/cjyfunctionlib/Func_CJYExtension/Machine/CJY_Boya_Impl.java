package cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine;

import android.content.Context;
import android.content.Intent;

import com.ys.myapi.MyManager;

import cn.cbsd.cjyfunctionlib.Tools.FileUtils;

public class CJY_Boya_Impl extends CJYHelper {

    private static CJY_Boya_Impl wzwManager;

    MyManager manager;

    Context mContext;

    public static final String STATICIP = "StaticIp";

    public static final String DHCP = "DHCP";

    public static final String ethernet = "ethernet";

    public static CJY_Boya_Impl getInstance(Context context) {
        if (wzwManager == null) {
            wzwManager = new CJY_Boya_Impl(context);
        }
        return wzwManager;
    }

    private CJY_Boya_Impl(Context context) {
        this.mContext = context;
        manager = MyManager.getInstance(context);
        manager.setWatchDogEnable(1);
        manager.watchDogFeedTime();
    }



    @Override
    public void HelperInit() {
        manager.bindAIDLService(mContext);

    }

    @Override
    public void HelperRelease() {
        mContext = null;
    }

    @Override
    public void setTime(int year, int month, int day, int hour, int minute, int second) {
        manager.setTime(year, month, day, hour, minute);

    }

    @Override
    public void reboot() {
        Intent intent = new Intent("com.xs.reboot");
        mContext.sendBroadcast(intent);
    }

    @Override
    public void setStaticEthIPAddress(String IPaddr, String gateWay, String mask, String dns1, String dns2) {
        manager.setStaticEthIPAddress(IPaddr, mask, gateWay, dns1, dns2);
        FileUtils.writeFileSdcard(ethernet, STATICIP);

    }

    @Override
    public String getAndroidDisplay() {
        return manager.getAndroidDisplay();

    }

    @Override
    public void setDhcpIpAddress() {
        manager.setDhcpIpAddress(mContext);
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
        manager.ethEnabled(status);

    }
}
