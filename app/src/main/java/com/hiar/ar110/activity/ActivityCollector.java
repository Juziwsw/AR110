package com.hiar.ar110.activity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    private static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(0, activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        List<Activity> actSaveList = new ArrayList<>();
        actSaveList.addAll(activities);
        for (Activity activity : actSaveList) {
            if (null != activity) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
        activities.clear();
    }

    public static Activity getCurrentActivity() {
        return activities.size() > 0 ? activities.get(0) : null;
    }
}
