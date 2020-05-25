package cn.cbdi.hunaninstrument.Tool;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;

public class ActivityCollector {

    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public static void finishAll(){

        OutputControlPresenter.getInstance().WhiteLight(false);
        for (Activity activity:activities){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
        OutputControlPresenter.getInstance().Close();

    }
}
