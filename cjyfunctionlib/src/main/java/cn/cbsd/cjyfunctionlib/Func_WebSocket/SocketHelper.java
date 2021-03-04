package cn.cbsd.cjyfunctionlib.Func_WebSocket;


import org.java_websocket.WebSocket;
import org.json.JSONObject;

public abstract class SocketHelper {

    public final static int cnt_CommonBack = 10000;

    public final static int cnt_Connect = 10001;
    public final static int cnt_Video = 10002;
    public final static int cnt_disconnect = 10003;

    public final static int cnt_getDaid = 20011;
    public final static int cnt_getEthMac = 20012;
    public final static int cnt_getIP = 20013;
    public final static int cnt_getServerId = 20014;
    public final static int cnt_getTem = 20015;
    public final static int cnt_getCPUTem = 20016;
    public final static int cnt_getGPUTem = 20017;
    public final static int cnt_reboot = 20018;
    public final static int cnt_setStaticIP = 20021;
    public final static int cnt_setDynamicIP = 20022;
    public final static int cnt_setServerId = 20023;
    public final static int cnt_getCGUser = 20031;
    public final static int cnt_getXJUser = 20032;
    public final static int cnt_updateUser = 20033;

    public abstract void dealData(WebSocket conn, int code, JSONObject json);

}
