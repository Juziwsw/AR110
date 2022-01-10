package com.hiar.ar110.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.hiar.ar110.activity.ActivityCollector;
import com.hiar.mybaselib.utils.AR110Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * Author:wilson.chen
 * date：5/11/21
 * desc：
 */
public class UILifecycleRegister {

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
    }

    private static final class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            printMethod(activity.getClass().getSimpleName(), "onActivityCreated");
            ActivityCollector.addActivity(activity);
            if (activity instanceof FragmentActivity) {
                initFragmentLifecycle((FragmentActivity) activity);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            printMethod(activity.getClass().getSimpleName(), "onActivityStarted");
        }

        @Override
        public void onActivityResumed(Activity activity) {
            printMethod(activity.getClass().getSimpleName(), "onActivityResumed");
        }

        @Override
        public void onActivityPaused(Activity activity) {
            printMethod(activity.getClass().getSimpleName(), "onActivityPaused");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            printMethod(activity.getClass().getSimpleName(), "onActivityStopped");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            printMethod(activity.getClass().getSimpleName(), "onActivitySaveInstanceState");
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            printMethod(activity.getClass().getSimpleName(), "onActivityDestroyed");
            ActivityCollector.removeActivity(activity);
        }

        private void initFragmentLifecycle(FragmentActivity activity) {
            activity.getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentLifecycleCallbacks(), true);
        }
    }

    private static final class FragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {
        @Override
        public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f,
                                      @Nullable Bundle savedInstanceState) {
            super.onFragmentCreated(fm, f, savedInstanceState);
            printMethod(f.getClass().getSimpleName(), "onFragmentCreated");
        }

        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentDestroyed(fm, f);
            printMethod(f.getClass().getSimpleName(), "onFragmentDestroyed");
        }
    }

    private static void printMethod(String tagName, String methodName) {
        AR110Log.i("AR110-UI", tagName + "->" + methodName);
    }
}
