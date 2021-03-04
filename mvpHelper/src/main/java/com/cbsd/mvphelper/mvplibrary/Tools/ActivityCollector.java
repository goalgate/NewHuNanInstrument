package com.cbsd.mvphelper.mvplibrary.Tools;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

public class ActivityCollector {

    //存放应用已开启的所有Activity.
    private List<Activity> mActivities = new LinkedList<>();
    private static ActivityCollector instance;

    public static ActivityCollector getInstance() {
        if (null == instance) {
            instance = new ActivityCollector();
        }
        return instance;
    }

    /**
     * 添加一个Activity到Activity列表中.
     */
    public void addActivity(Activity activity) {
        if (!mActivities.contains(activity)) {
            mActivities.add(activity);
        }
    }

    /**
     * 从Activity列表中删除指定的Activity.
     */
    public void removeActivity(Activity activity) {
        if (mActivities.contains(activity)) {
            mActivities.remove(activity);
        }
    }

    /**
     * 关闭所有的Activity, 退出程序.
     */
    public void finishAll() {
        for (Activity activity : mActivities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        System.exit(0);
    }
}
