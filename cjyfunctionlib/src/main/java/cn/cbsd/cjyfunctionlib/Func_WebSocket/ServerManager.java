package cn.cbsd.cjyfunctionlib.Func_WebSocket;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static cn.cbsd.cjyfunctionlib.Func_WebSocket.SocketHelper.cnt_CommonBack;
import static cn.cbsd.cjyfunctionlib.Func_WebSocket.SocketHelper.cnt_Connect;
import static cn.cbsd.cjyfunctionlib.Func_WebSocket.SocketHelper.cnt_Video;
import static cn.cbsd.cjyfunctionlib.Func_WebSocket.SocketHelper.cnt_disconnect;
import static cn.cbsd.cjyfunctionlib.Func_WebSocket.SocketHelper.cnt_getCGUser;

public class ServerManager {

    private CJYWebSocketServer serverSocket = null;

    WebSocket videoSocket;

    boolean playVideo = false;

    private SocketHelper socketHelper;

    private ServerManager() {
    }

    private static ServerManager instance = null;

    public static ServerManager getInstance() {
        if (instance == null)
            instance = new ServerManager();
        return instance;
    }

    public void SendVideoData(byte[] data) {
        if (videoSocket != null && playVideo) {
            videoSocket.send(data);
        }
    }

    public boolean isReady() {
        if (videoSocket == null) {
            return false;
        }else {
            return true;
        }
    }

    public boolean Start(int port, SocketHelper socketHelper) {
        if (port < 0) {
            Log.i("TAG", "Port error...");
            return false;
        }

        Log.i("TAG", "Start ServerSocket...");

        try {
            serverSocket = new CJYWebSocketServer(port);
            this.socketHelper = socketHelper;
            serverSocket.start();
            Log.i("TAG", "Start ServerSocket Success...");
            return true;
        } catch (Exception e) {
            Log.i("TAG", "Start Failed...");
            e.printStackTrace();
            return false;
        }
    }

    public boolean Stop() {
        try {
            serverSocket.stop();
            Log.i("TAG", "Stop ServerSocket Success...");
            return true;
        } catch (Exception e) {
            Log.i("TAG", "Stop ServerSocket Failed...");
            e.printStackTrace();
            return false;
        }
    }

    class CJYWebSocketServer extends WebSocketServer {

        public CJYWebSocketServer(int port) throws UnknownHostException {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Log.i("TAG", "Some one Connected...");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            if (conn.equals(videoSocket)) {
                videoSocket = null;
            }
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            try {
                JSONObject jmessage = new JSONObject(message);
                int code = jmessage.getInt("code");
                switch (code) {
                    case cnt_Connect:
                        JSONObject data_cnt_Connect = jmessage.getJSONObject("data");
                        String username = data_cnt_Connect.getString("user");
                        String pwd = data_cnt_Connect.getString("pwd");
                        if (username.equals("cjy") && pwd.equals("888888")) {
                            if (videoSocket != null) {
                                JSONObject returnJSON = new JSONObject();
                                returnJSON.put("code", cnt_CommonBack);
                                JSONObject return_data = new JSONObject();
                                return_data.put("errCode", 1);
                                return_data.put("errMsg", "Video has been used");
                                returnJSON.put("data", return_data);
                                conn.send(returnJSON.toString());
                            } else {
                                JSONObject returnJSON = new JSONObject();
                                returnJSON.put("code", cnt_CommonBack);
                                JSONObject return_data = new JSONObject();
                                return_data.put("errCode", 0);
                                return_data.put("errMsg", "loginSuccess");
                                returnJSON.put("data", return_data);
                                conn.send(returnJSON.toString());
                                videoSocket = conn;
                            }
                        } else {
                            JSONObject returnJSON = new JSONObject();
                            returnJSON.put("code", cnt_getCGUser);
                            JSONObject return_data = new JSONObject();
                            return_data.put("errCode", 1);
                            return_data.put("errMsg", "loginFailed");
                            returnJSON.put("data", return_data);
                            conn.send(returnJSON.toString());
                        }
                        break;
                    case cnt_Video:
                        JSONObject data_cnt_Video = jmessage.getJSONObject("data");
                        if (data_cnt_Video.getInt("playVideo") == 1) {
                            playVideo = true;
                        } else if (data_cnt_Video.getInt("playVideo") == 0) {
                            playVideo = false;
                        }
                        break;
                    case cnt_disconnect:
                        JSONObject data = jmessage.getJSONObject("data");
                        if (data.getInt("Cease") == 1) {
                            conn.close();
                            if (videoSocket != null) {
                                videoSocket = null;
                            }
                        }
                    default:
                        if (socketHelper != null) {
                            socketHelper.dealData(conn, code, jmessage);
                        }
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("TAG", "OnMessage:" + message.toString());
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            Log.i("TAG", "Socket Exception:" + ex.toString());
        }

        @Override
        public void onStart() {

        }
    }
}
