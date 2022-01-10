package com.hiar.ar110.helper

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.hiar.ar110.ConstantApp
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.data.HileiaMessageData
import com.hiar.ar110.service.AR110BaseService
import com.hiar.mybaselib.utils.AR110Log

/**
 * Author:wilson.chen
 * date：5/13/21
 * desc：
 */
public class MainActivityServiceHelper constructor(val activity: AR110MainActivity) : LifecycleObserver {
    val mClientMessenger: Messenger by lazy {
        Messenger(mClientHandler)
    }
    var mServicesMessenger: Messenger? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    public fun startService() {
        AR110Log.i(TAG, "startService")
        val intent = Intent(activity, AR110BaseService::class.java)
        activity.bindService(intent, backgroundService, Service.BIND_AUTO_CREATE)
    }

    private val backgroundService: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {

        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mServicesMessenger = Messenger(service)
            val msg = Message.obtain(null, ConstantApp.ServiceMessage.MSG_FROM_CLIENT)
            msg.replyTo = mClientMessenger
            try {
                mServicesMessenger?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

        }
    }

    @SuppressLint("HandlerLeak")
    val mClientHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
//                ConstantApp.ServiceMessage.MSG_COPTASK_ADD -> {
//                    val messageData = msg.obj as String
//                    activity.onCoptaskAdd(messageData)
//                }
                ConstantApp.ServiceMessage.MSG_HILEIA_CALLING_CALLBACK -> {
                    val messageData = msg.obj as HileiaMessageData
                    activity.onHileiaComingCallCallback(messageData.success, messageData.mSessionId)
                }
                ConstantApp.ServiceMessage.MSG_HILEIA_HANGUP_CALLBACK -> {
                    val messageData = msg.obj as String
                    activity.onHileiaHangupCallback(messageData)
                }
                ConstantApp.ServiceMessage.MSG_HILEIA_LOGIN_CALLBACK -> {
                    val messageData = msg.obj as Boolean
                    activity.onHileiaLoginRes(messageData)
                }
//                ConstantApp.ServiceMessage.MSG_LOCATION_UPDATE -> {
//                    val messageData = msg.obj as LocationData
//                    NavigationHelper.instance.updateGpsLocation(messageData)
//                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public fun onDestroy() {
        mClientHandler.removeCallbacksAndMessages(null)
        activity.unbindService(backgroundService)
    }

    companion object {
        val TAG = "AR110MainActivity"
    }
}