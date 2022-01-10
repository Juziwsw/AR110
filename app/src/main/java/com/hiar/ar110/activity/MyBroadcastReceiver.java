package com.hiar.ar110.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hiar.ar110.service.AR110BaseService;
import com.hiar.ar110.util.Util;
import com.hiar.mybaselib.utils.AR110Log;

import cn.com.cybertech.pdk.PushMessage;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AR110Log.i(TAG, "action="+action);
        if (PushMessage.buildupBroadcastAction4PushMessage(
                context.getPackageName()).equals(action)) {
            PushMessage message = (PushMessage) intent.getSerializableExtra(
                            PushMessage.KEY_MESSAGE);
        } else if(action.equals("com.hiscene.deviceinforeporter.face_status")) {
            AR110Log.i(TAG,"com.hiscene.deviceinforeporter.face_status");
            //Intent Serintent = new Intent(context, AR110BaseService.class);
            //context.startService(Serintent);
        }
    }
}
