package cn.cbdi.hunaninstrument.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.FaceInitActivity;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {// boot;
            Intent intent2 = new Intent(context, FaceInitActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
    }
}
