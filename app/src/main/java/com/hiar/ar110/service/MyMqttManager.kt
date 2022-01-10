package com.hiar.ar110.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.os.Handler
import android.text.TextUtils
import com.hiar.ar110.data.Coordinate
import com.hiar.ar110.data.HttpResult
import com.hiar.ar110.data.MqttData
import com.hiar.ar110.data.Payload
import com.hiar.ar110.data.cop.CopData
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.sendEvent
import com.hiar.ar110.extension.toJson
import com.hiar.ar110.listener.MyMqttClientListener
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.util.Util
import com.hiar.ar110.util.sendNotification
import com.hiar.mybaselib.utils.AR110Log
import com.hiscene.hiar.mqtt.client.MqttClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent
import java.util.*

/**
 *
 * @author tangxucheng
 * @date 2021/6/24
 * Email: xucheng.tang@hiscene.com
 */
class MyMqttManager {
    val TAG = MyMqttManager::class.java.simpleName
    val ar110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
//    var HOST = "tcp://192.168.23.65:31885" //服务器地址（协议+地址+端口号）
    var USERNAME = "" //用户名
    var PASSWORD = "" //密码
    var mLostCount = 0
    var mMsgCount = 0
    val mSendCount = 0

//    @RequiresApi(api = 26)
//    var CLIENTID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Build.getSerial() else Build.SERIAL //客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示
    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    fun response(message: String) {
        val topic = COP_GNSS_TOPIC
        val qos = 2
        val retained = false
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient!!.publish(topic, qos, message.toByteArray())
            AR110Log.i(TAG, "response $topic------=${message}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 同步状态到mqtt
     * APP是检测用户进入巡逻详情界面，检测任务状态为“处理中“，消息推送警员状态为巡逻中，其他任务状态“未处理”，“已完成”消息推送为空闲，任务状态由“处理中”变为“已完成”，消息推送为空闲；
     * APP检测用户进入接处警详情界面，检测任务状态为“处理中“消息推送警员状态为出警中，其他任务状态“未处理”，“已完成”消息推送为空闲，任务状态由“处理中”变为“已完成”，消息推送为空闲；
     * APP检测用户进入流动人口详情界面，检测任务状态为“处理中“消息推送警员状态为核查中，其他任务状态“未处理”，“已完成”消息推送为空闲，任务状态由“处理中”变为“已完成”，消息推送为空闲；
     * 从警单详情进聊天界面  发空闲状态
     * APP检测除这三个界面的其他界面，消息推送警员状态为空闲；
     * 警员离线状态推送：服务端根据轮巡 APP 10分钟不上传经纬度 ，判断为警员下线，消息推送到MQTT；
     * 警员位置更新 2004
     * 巡逻任务状态更新 3004
     * statusCode 1-空闲 ，2-出警中 ，3-巡逻中，4-下线，5、清查中
     *
     * @params code -1为空闲
     *
     */
    fun updateStatus(code: Int,mCurrentCJZT: Int) {
        if(id==null){
            CommEvent(CommEventTag.USER_INFO_UPDATE).sendEvent()
            AR110Log.i(TAG, "updateLoc fail 警员id = null  ")
            return
        }
        val topic = STATE_TOPIC+ id    //""
        val qos = 2
        val retained = false

        try {
            /**
             * 1-空闲 ，2-出警中 ，3-巡逻中，4-下线，5、清查中
             */
            val status = getStatusCode(code,mCurrentCJZT)
            val payload = Payload(id, status)
            val mqttData = MqttData(COP_STATUS, payload)

            val message = mqttData.toJson("status")
            AR110Log.i(TAG, "response  $topic------=${message}  ")
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient!!.publish(topic, qos, message?.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getStatusCode(code: Int,mCurrentCJZT: Int) :Int {
        //statusCode 1-空闲 ，2-出警中 ，3-巡逻中，4-下线，5、清查中
        return  if(mCurrentCJZT==Util.CJZT_CJZ&&code==COP_STATUS) {
            AR110Log.i(TAG, "getStatusCode=  出警中 ")
             2//2-出警中
        }else if(mCurrentCJZT==Util.CJZT_CJZ&&code==PATROL_TASK_STATUS){
            AR110Log.i(TAG, "getStatusCode=  巡逻中 ")
             3 //3-巡逻中
        } else if(mCurrentCJZT==Util.CJZT_CJZ&&code== POPULATION_TASK_STATUS){
            AR110Log.i(TAG, "getStatusCode=  清查中 ")
            5 //5、清查中
        } else {
            AR110Log.i(TAG, "getStatusCode= 空闲 ")
             1 //1-空闲
        }
    }
    /**
     * 同步位置到mqtt
     * 巡逻任务位置更新 3005
     */
    fun updateLoc(locData : Location) {
        if(id==null){
            CommEvent(CommEventTag.USER_INFO_UPDATE).sendEvent()
            AR110Log.i(TAG, "updateLoc fail 警员id = null  ")
            return
        }
        val topic = COP_GNSS_TOPIC+ id    //""
        val qos = 2
        val retained = false

        try {
            val coordinate = Coordinate(locData.longitude, locData.latitude)
            val payload = Payload(id, coordinate)
            val mqttData = MqttData(COP_GNSS, payload)

            val message = mqttData.toJson(coordinate.javaClass.simpleName)

            AR110Log.i(TAG, "response  $topic------=${message}  ")
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient!!.publish(topic, qos, message?.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var mHandler: Handler? = null
    /**
     * 初始化
     */
    fun init(context:Context) {
        var clientId="Android-" +Util.getIMEI(0)
        AR110Log.i(TAG, "init host=${Util.mMqttHost} clientId=$clientId")
        mContext=context
        mHandler = Handler(context.mainLooper)
        mqttAndroidClient = MqttClient(Util.mMqttHost, clientId, 20)
        mqttAndroidClient?.setListener(mqttCallback)
        doClientConnection()
    }

    /**
     * 连接MQTT服务器
     */
    private fun doClientConnection() {
        if (!mqttAndroidClient!!.isConnected && isConnectIsNomarl) {
            try {
                mqttAndroidClient?.connect(USERNAME, PASSWORD, true, 10)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }/*没有可用网络的时候，延迟3秒再尝试重连*/

    /**
     * 判断网络是否连接
     */
    private val isConnectIsNomarl: Boolean
        get() {
            val connectivityManager = mContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            return if (info != null && info.isAvailable) {
//                val name = info.typeName
//                AR110Log.i(TAG, "当前网络名称：$name")
                true
            } else {
                AR110Log.i(TAG, "没有可用网络")
                /*没有可用网络的时候，延迟3秒再尝试重连*/mHandler?.postDelayed({ doClientConnection() }, 3000)
                false
            }
        }

    fun subscribeTopic(){
        if(id != null) {
            val topic = "$COP_TASK_TOPIC$id/#"
            mqttAndroidClient?.subscribe(topic, 2)
        }
    }

    //订阅主题的回调
    private val mqttCallback: MyMqttClientListener = object : MyMqttClientListener() {
        override fun onConnectSuccess() {
            AR110Log.i(TAG, "onConnectSuccess")
            subscribeTopic()
        }
        override fun onConnectFailure(code: Int) {
            AR110Log.i(TAG, "onConnectFailure code=$code")
            doClientConnection()
        }
        override fun onConnectionLost() {
            mLostCount++
            AR110Log.i(TAG, "onConnectionLost mLostCount=$mLostCount")
            doClientConnection()
        }
        override fun onMessageArrived(topic: String, data: ByteArray) {
            mMsgCount++
            AR110Log.i(TAG, "onMessageArrived:topic=" + topic + ",size=" + data.size + ",data=" + String(data))
            if(null != topic && topic.contains(COP_TASK_TOPIC) ) {
                doSyncCoptask()
            }
        }
    }
    /**
     * 检查是否有新警单
     */
    fun doSyncCoptask() {
        AR110Log.i(TAG, "doSyncCoptask")
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val map: MutableMap<String, String> = HashMap()
                    map["account"] = AR110BaseService.mUserInfo!!.account
                    val result: HttpResult<CopData>? = ar110Api.syncCopTask(map)
                    if (result != null) {
                        AR110Log.i(TAG, "doSyncCoptask result.data=${result.data}")
                    }
                    if (result?.isSuccess == true && result.data != null) {
                        val data: CopData = result.data!!
                        if (!TextUtils.isEmpty(data.cjdbh2)) {
                            Util.mNewMsgCjdbh2 = data.cjdbh2
                            mContext?.let { sendNotification(data.cjdbh2, it) }
                            //通知界面刷新
                            CommEvent(CommEventTag.COPTASK_ADD, data.cjdbh2).sendEvent()
                        }
                    }
                }.onFailure {
                    AR110Log.e(TAG, "doSyncCoptask onfail it.message=${it.message}")
                }
            }
        }
    }



    fun onDestroy() {
        try {
            mqttAndroidClient!!.disconnect() //断开连接
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var mqttAndroidClient: MqttClient? = null
        var COP_TASK_TOPIC = "AR110/AlarmTask/" //警单订阅
        var COP_GNSS_TOPIC = "AR110/Cop/gnss/" //警员位置更新
        var STATE_TOPIC = "AR110/Cop/" //响应主题

        private const val MSG_GET_LOCATION = 399
        const val MSG_GET_LAST_LOC = MSG_GET_LOCATION - 1

        const val COP_GNSS = 2004        // 警员位置更新 2004
        const val COP_STATUS = 2005        // 警员状态更新 2005

        const val PATROL_TASK_STATUS = 3004    // 巡逻任务状态 3004
        const val POPULATION_TASK_STATUS = 3005    // 人口核查状态 3005



        @SuppressLint("StaticFieldLeak")
        private var mInstance: MyMqttManager? = null
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null

        /**
         * 警员id 如：104
         */
        var  id: Int? =null


        val instance: MyMqttManager?
            get() {
                if (null == mInstance) {
                    mInstance = MyMqttManager()
                }
                return mInstance
            }
    }
    init {
        mInstance = this
    }
}