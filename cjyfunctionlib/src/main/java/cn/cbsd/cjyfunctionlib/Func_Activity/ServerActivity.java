package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.R;
import cn.cbsd.cjyfunctionlib.func_server.IServerEvent;
import cn.cbsd.cjyfunctionlib.func_server.ServerManager;


/**
 * @author Created by WZW on 2021-04-13 15:57.
 * @description
 */
public class ServerActivity extends Activity {

    private ServerManager mServerManager;

    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnBrowser;
    private TextView mTvMessage;

    private String mRootUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        CJYHelper.getInstance(this);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnBrowser = findViewById(R.id.btn_browse);
        mTvMessage = findViewById(R.id.tv_message);

        mBtnStart.setOnClickListener(
                (v) -> mServerManager.startServer()
        );
        mBtnStop.setOnClickListener(
                (v) -> mServerManager.stopServer()
        );
        mBtnBrowser.setOnClickListener((v)->{
            if (!TextUtils.isEmpty(mRootUrl)) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(mRootUrl));
                startActivity(intent);
            }
        });

        // AndServer run in the service.
        mServerManager = new ServerManager(this, new IServerEvent() {
            @Override
            public void onServerStart(String ip) {
                mBtnStart.setVisibility(View.GONE);
                mBtnStop.setVisibility(View.VISIBLE);
                mBtnBrowser.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(ip)) {
                    List<String> addressList = new LinkedList<>();
                    mRootUrl = "http://" + ip + ":8080/";
                    addressList.add(mRootUrl);
                    addressList.add("http://" + ip + ":8080/login.html");
                    mTvMessage.setText(TextUtils.join("\n", addressList));
                } else {
                    mRootUrl = null;
                    mTvMessage.setText(R.string.server_ip_error);
                }
            }

            @Override
            public void onServerError(String message) {
                mRootUrl = null;
                mBtnStart.setVisibility(View.VISIBLE);
                mBtnStop.setVisibility(View.GONE);
                mBtnBrowser.setVisibility(View.GONE);
                mTvMessage.setText(message);
            }

            @Override
            public void onServerStop() {
                mRootUrl = null;
                mBtnStart.setVisibility(View.VISIBLE);
                mBtnStop.setVisibility(View.GONE);
                mBtnBrowser.setVisibility(View.GONE);
                mTvMessage.setText(R.string.server_stop_succeed);
            }
        });
        mServerManager.register();

        mBtnStart.performClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServerManager.unRegister();
    }
}
