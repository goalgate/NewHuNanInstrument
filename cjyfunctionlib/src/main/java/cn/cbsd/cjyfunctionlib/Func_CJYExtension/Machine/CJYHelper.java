package cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine;

import android.content.Context;

public abstract class CJYHelper {

    public abstract void HelperInit();

    public abstract void HelperRelease();

    public abstract void setTime(int year, int month, int day, int hour, int minute, int second);

    public abstract void reboot();

    public abstract void setStaticEthIPAddress(String IPaddr, String gateWay, String mask, String dns1, String dns2);

    public abstract String getAndroidDisplay();

    public abstract void setDhcpIpAddress();

    public abstract String getEthMode();

    public abstract void ethEnabled(boolean status);

    public static CJYHelper getInstance(Context context) {
        return CJY_Boya_Impl.getInstance(context);
    }


}
