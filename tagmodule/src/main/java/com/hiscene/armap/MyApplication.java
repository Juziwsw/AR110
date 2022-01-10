package com.hiscene.armap;

import android.app.Application;

public class MyApplication extends Application {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}

