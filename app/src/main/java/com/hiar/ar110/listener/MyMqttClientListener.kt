package com.hiar.ar110.listener

import com.hiar.mybaselib.utils.AR110Log
import com.hiscene.hiar.mqtt.client.MqttClientListener

/**
 *
 * @author tangxucheng
 * @date 2021/6/30
 * Email: xucheng.tang@hiscene.com
 */
open class MyMqttClientListener : MqttClientListener {
    val TAG = "MyMqttClientListener"
    override fun onConnectSuccess() {
        AR110Log.d(TAG, "onConnectSuccess")
    }

    override fun onConnectFailure(code: Int) {
        AR110Log.d(TAG, "onConnectFailure code=$code")
    }

    override fun onConnectionLost() {
        AR110Log.d(TAG, "onConnectionLost ")
    }

    override fun onDisconnected() {
        AR110Log.d(TAG, "onDisconnected")
    }

    override fun onSubscribeSuccess(token: Int) {
        AR110Log.d(TAG, "onSubscribeSuccess:token=$token")
    }

    override fun onSubscribeFailure(token: Int, code: Int) {
        AR110Log.d(TAG, "onSubscribeFailure:token=$token,code=$code")
    }

    override fun onUnSubscribeSuccess(token: Int) {
        AR110Log.d(TAG, "onUnSubscribeSuccess:token=$token")
    }

    override fun onUnSubscribeFailure(code: Int, token: Int) {
        AR110Log.d(TAG, "onUnSubscribeFailure:token=$token,code=$code")
    }

    override fun onPublishSuccess(token: Int) {
        AR110Log.d(TAG, "onPublishSuccess:token=$token")
    }

    override fun onPublishFailure(token: Int, code: Int) {
        AR110Log.d(TAG, "onPublishFailure:token=$token,code=$code")
    }

    override fun onMessageArrived(topic: String, data: ByteArray) {
        AR110Log.d(TAG, "onMessageArrived:topic=" + topic + ",size=" + data.size + ",data=" + String(data))
    }
}