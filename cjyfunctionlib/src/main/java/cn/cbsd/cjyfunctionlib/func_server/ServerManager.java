/*
 * Copyright © 2018 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.cbsd.cjyfunctionlib.func_server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * @author Created by WZW on 2021-04-13 15:59.
 * @description
 */
public class ServerManager extends BroadcastReceiver {

    private static final String ACTION = "cn.cbsd.cjyfunctionlib.receiver";

    private static final String CMD_KEY = "CMD_KEY";

    private static final String MESSAGE_KEY = "MESSAGE_KEY";

    private static final int CMD_VALUE_START = 1;
    private static final int CMD_VALUE_ERROR = 2;
    private static final int CMD_VALUE_STOP = 4;

    /**
     * Notify serverStart.
     *
     * @param context context.
     */
    public static void onServerStart(Context context, String hostAddress) {
        sendBroadcast(context, CMD_VALUE_START, hostAddress);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void onServerError(Context context, String error) {
        sendBroadcast(context, CMD_VALUE_ERROR, error);
    }

    /**
     * Notify serverStop.
     *
     * @param context context.
     */
    public static void onServerStop(Context context) {
        sendBroadcast(context, CMD_VALUE_STOP);
    }

    private static void sendBroadcast(Context context, int cmd) {
        sendBroadcast(context, cmd, null);
    }

    private static void sendBroadcast(Context context, int cmd, String message) {
        Intent broadcast = new Intent(ACTION);
        broadcast.putExtra(CMD_KEY, cmd);
        broadcast.putExtra(MESSAGE_KEY, message);
        context.sendBroadcast(broadcast);
    }


    private Context mContext;
    private IServerEvent mEvent;
    private Intent mService;

    public ServerManager(Context mContext, IServerEvent event) {
        this.mContext = mContext;
        this.mEvent = event;
        mService = new Intent(mContext, CoreService.class);
    }

    /**
     * Register broadcast.
     */
    public void register() {
        IntentFilter filter = new IntentFilter(ACTION);
        mContext.registerReceiver(this, filter);
    }

    /**
     * UnRegister broadcast.
     */
    public void unRegister() {
        mContext.unregisterReceiver(this);
    }

    public void startServer() {
        mContext.startService(mService);
    }

    public void stopServer() {
        mContext.stopService(mService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION.equals(action)) {
            int cmd = intent.getIntExtra(CMD_KEY, 0);
            switch (cmd) {
                case CMD_VALUE_START: {
                    String ip = intent.getStringExtra(MESSAGE_KEY);
                    mEvent.onServerStart(ip);
                    break;
                }
                case CMD_VALUE_ERROR: {
                    String error = intent.getStringExtra(MESSAGE_KEY);
                    mEvent.onServerError(error);
                    break;
                }
                case CMD_VALUE_STOP: {
                    mEvent.onServerStop();
                    break;
                }
            }
        }
    }
}