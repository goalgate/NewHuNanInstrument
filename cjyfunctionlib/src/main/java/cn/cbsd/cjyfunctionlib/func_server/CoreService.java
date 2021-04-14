package cn.cbsd.cjyfunctionlib.func_server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import cn.cbsd.cjyfunctionlib.Tools.NetInfo;

/**
 * @author Created by WZW on 2021-04-13 15:58.
 * @description
 */
public class CoreService extends Service {

    private Server mServer;

    @Override
    public void onCreate() {
        mServer = AndServer.webServer(this)
                .port(8080)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        InetAddress address = NetInfo.getLocalIPAddress();
                        ServerManager.onServerStart(CoreService.this, address.getHostAddress());
                    }

                    @Override
                    public void onStopped() {
                        ServerManager.onServerStop(CoreService.this);
                    }

                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace();
                        ServerManager.onServerError(CoreService.this, e.getMessage());
                    }
                })
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    /**
     * Start server.
     */
    private void startServer() {
        mServer.startup();
    }

    /**
     * Stop server.
     */
    private void stopServer() {
        mServer.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
