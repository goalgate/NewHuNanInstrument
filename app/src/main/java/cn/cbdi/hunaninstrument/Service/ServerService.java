package cn.cbdi.hunaninstrument.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.func_server.IServerEvent;
import cn.cbsd.cjyfunctionlib.func_server.ServerManager;

/**
 * @author Created by WZW on 2021-04-14 15:10.
 * @description
 */

public class ServerService extends Service {

    private ServerManager mServerManager;

    private String mRootUrl;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CJYHelper.getInstance(this);
        mServerManager = new ServerManager(this, new IServerEvent() {
            @Override
            public void onServerStart(String ip) {
                if (!TextUtils.isEmpty(ip)) {
                    List<String> addressList = new LinkedList<>();
                    mRootUrl = "http://" + ip + ":8080/";
                    addressList.add(mRootUrl);
                    addressList.add("http://" + ip + ":8080/login.html");
                } else {
                    mRootUrl = null;
                }
            }

            @Override
            public void onServerError(String message) {
                mRootUrl = null;

            }

            @Override
            public void onServerStop() {
                mRootUrl = null;
            }
        });
        mServerManager.register();
        mServerManager.startServer();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mServerManager.stopServer();
        mServerManager.unRegister();
    }
}
