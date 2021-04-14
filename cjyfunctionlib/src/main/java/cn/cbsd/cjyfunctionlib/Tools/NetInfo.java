package cn.cbsd.cjyfunctionlib.Tools;

import android.os.Build;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;


/**
 * Created by Administrator on 2017-07-13.
 */

public class NetInfo {
    //取有线网卡MAC
    public String getMac() {
        return getMac("eth0");
    }

    //取无线网卡MAC
    public String getWifiMac() {
        return getMac("wlan0");
    }

    public String getMac(String name) {
        String macSerial = "";
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/" + name + "/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            macSerial = "";
            ex.printStackTrace();
        }
        return macSerial;
    }

    //取macID
    public String getMacId() {
        if (Build.DEVICE.startsWith("rk3288")) {
            if (TextUtils.isEmpty(macToId(getWifiMac()))) {
                return macToId(getMac());
            } else {
                return macToId(getWifiMac());
            }
        } else {
            return macToId(getMac());
        }
//        return macToId(getMac());
    }


    public String macToId(String mac) {
        String s = "";
        if (mac == null) {
            return "";
        }
        String[] ss = mac.split(":");
        if (ss.length < 6) {
            return "";
        }
        try {
            for (int i = 0; i < 6; i++) {
                int b = Integer.parseInt(ss[i].trim(), 16);
                s += formatStr(String.valueOf(b), 3);
                if (i == 1 || i == 3) {
                    s += "-";
                }
            }

        } catch (Exception ex) {
            s = "";
        }
        return s;
    }

    public String formatStr(String str, int len) {
        String s = "";
        if (str.length() == len) {
            s = str;
        } else if (str.length() < len) {
            for (int i = str.length(); i < len; i++) {
                s = '0' + s;
            }
            s = s + str;
        } else if (str.length() > len) {
            s = str.substring(str.length() - len);

        }

        return s;

    }


    /**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                    "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     *
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * Get local Ip address.
     */
    public static InetAddress getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                if (inetAddresses != null) {
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress;
                        }
                    }
                }
            }
        }
        return null;
    }

}
