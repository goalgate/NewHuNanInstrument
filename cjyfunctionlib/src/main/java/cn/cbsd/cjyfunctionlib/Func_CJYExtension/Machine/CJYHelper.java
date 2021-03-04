package cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine;

import android.content.Context;
import android.os.Build;

import java.io.DataOutputStream;

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
        if (Build.DEVICE.startsWith("rk3288")) {
            return CJY_Boya_Impl.getInstance(context);
        } else if (Build.DEVICE.startsWith("Apollo7")) {
            return CJY_Apollo_Impl.getInstance(context);
        }
        return null;
    }

    public abstract void turnOffBackLight();

    public abstract void turnOnBackLight();

    public static void excuseCmd(String command) {
        Process process = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("vm");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception var12) {
            var12.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }

                if (process != null) {
                    process.destroy();
                }
            } catch (Exception var11) {
                var11.printStackTrace();
            }

        }
    }

    public float readCPUTem(int i){
        return -1;
    }

}
