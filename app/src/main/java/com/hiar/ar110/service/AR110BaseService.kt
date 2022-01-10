package com.hiar.ar110.service

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.widget.Toast
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.Utils
import com.example.mediaengine.engines.MediaEngineHolder
import com.example.mediaengine.entity.EngineConfigInfo
import com.example.mediaengine.entity.MediaType
import com.google.protobuf.InvalidProtocolBufferException
import com.hiar.ar110.BuildConfig
import com.hiar.ar110.ConstantApp
import com.hiar.ar110.MainApplication.Companion.getInstance
import com.hiar.ar110.R
import com.hiar.ar110.data.*
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.patrol.PatrolLocationReportData
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.sendEvent
import com.hiar.ar110.extension.toJson
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hileia.common.enginer.LeiaBoxEngine
import com.hileia.common.entity.MessageCallback
import com.hileia.common.entity.MultVal
import com.hileia.common.entity.proto.EntityOuterClass.Entity.SessionStateInfo
import com.hileia.common.entity.proto.Enums
import com.hileia.common.entity.proto.Handler.HandlerMsgIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.java.KoinJavaComponent
import java.util.*

class AR110BaseService : Service(), MessageCallback {
    private val ar110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
    private var mHandler: Handler? = null
    private var mSessionId = ""
    private var mVideoUploader: MyVideoUploader? = null
    var lastNetworkIsConnect = true
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            AR110Log.i(TAG, "the user exited $action")
            if (action == ConstantApp.BroadcastAction.ACTION_PSTORE_EXIT) {
                getInstance()!!.exit()
                stopSelf()
            }
        }
    }
    private var mServiceMessenger: Messenger = Messenger(ServiceHandler(Looper.getMainLooper()))
    var mClientMessenger: Messenger? = null
    override fun onBind(intent: Intent): IBinder? {
        return mServiceMessenger.binder
    }

    inner class ServiceHandler(mainLooper: Looper?) : Handler(mainLooper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ConstantApp.ServiceMessage.MSG_FROM_CLIENT -> {
                    mClientMessenger = msg.replyTo
                    val reply = Message.obtain(
                        null,
                        ConstantApp.ServiceMessage.MSG_FROM_SERVICE
                    )
                    sendMessageToClient(reply)
                    mHandler!!.sendEmptyMessageDelayed(MSG_CLEAN_CACHE_FILE, (1000 * 5).toLong())
                }
            }
        }
    }

    private fun initLeiaBoxEngine() {
        AR110Log.i(TAG, "initLeiaBoxEngine")
        if (Util.mISExternalWeb && BuildConfig.DEBUG) {
            //LeiaBoxEngine.getInstance().settingManager().setRunningEnv(0);
            LeiaBoxEngine.getInstance().settingManager().runningEnv = 3 //Hileia私有化环境
            val url = Util.mExternalHileiaUrl
            val port = Util.mExternalHileiaPort
            //AR110Log.i("hileia", "hileia config: ip="+url+", port="+port);
            LeiaBoxEngine.getInstance().settingManager()
                .setServerConfigInfo(Enums.ProtocolType.HTTP, url, port)
        } else {
            LeiaBoxEngine.getInstance().settingManager().runningEnv = 3 //Hileia私有化环境
            val url = Util.getHileiaPrivateUrl()
            val port = Util.getHileiaPrivatePort()
            //AR110Log.i("hileia", "hileia config: ip="+url+", port="+port);
            LeiaBoxEngine.getInstance().settingManager().setServerConfigInfo(
                Enums.ProtocolType.HTTP,
                Util.getHileiaPrivateUrl(),
                Util.getHileiaPrivatePort()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        isInitialized = true
        AR110Log.i(TAG, "BaiduLocServer onCreate")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mHandler = object : Handler(mainLooper) {
            @SuppressLint("MissingPermission")
            override fun handleMessage(msg: Message) {
                AR110Log.i(TAG, "mHandler handleMessage msg.what-------------=${msg.what}")
                when (msg.what) {
                    MSG_CHECK_NETWORK -> checkNetwork()
                    MSG_GET_LAST_LOC -> lastLocation
                    MSG_CLEAN_CACHE_FILE -> Thread(CacheFileCleanTask()).start()
                    else -> {
                    }
                }
            }
        }
        AR110Log.i(TAG, "BaiduLocServer onCreate mHandler=$mHandler this=$this")
        if (!BuildConfig.DEBUG) {
            val filter = IntentFilter()
            filter.addAction(ConstantApp.BroadcastAction.ACTION_PSTORE_EXIT)
            registerReceiver(mReceiver, filter)
        }
        onStart()
    }//if(BuildConfig.DEBUG) {

    /**
     * 检查网络状态
     * todo 未来看是否可以通过网络状态监听回调实现
     */
    private fun checkNetwork() {
        if (!Util.isNetworkConnected(this@AR110BaseService)) {
            Util.showMessage("网络已断开，请检查网络!")
            lastNetworkIsConnect = false
        } else if (!lastNetworkIsConnect) {
            lastNetworkIsConnect = true
            CommEvent(CommEventTag.DATA_UPDATE).sendEvent()
            if (!isInitHileia) {
                initHileiaToken()
            }
        }
        mHandler!!.sendEmptyMessageDelayed(MSG_CHECK_NETWORK, 5000)
    }

    private fun sendMessageToClient(message: Message) {
        try {
            if (null != mClientMessenger) {
                mClientMessenger!!.send(message)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        AR110Log.d(TAG, "onStartCommand()")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun onStart() {
        val CHANNEL_ONE_ID = "CHANNEL_ONE_ID"
        val CHANNEL_ONE_NAME = "CHANNEL_ONE_ID"
        var notificationChannel: NotificationChannel? = null
        //进行8.0的判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = 0x0000ff
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.jianshu.com/p/14ba95c6c3e2"))
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        var notification: Notification? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(this).setChannelId(CHANNEL_ONE_ID)
                .setTicker("Nature")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("这是一个测试标题")
                .setContentIntent(pendingIntent)
                .setContentText("这是一个测试内容")
                .build()
        }
        notification!!.flags = notification.flags or Notification.FLAG_NO_CLEAR
        startForeground(0, notification)
//        mImei = Util.getAndroidID()
//        mImsi = Util.getAndroidID()
        mVideoUploader = MyVideoUploader()
        if (mUserInfo == null) {
            mUserInfo = Util.getUserInfo(this)
            AR110Log.i(
                TAG, "mUserInfo is null *** get userInfo account=" + mUserInfo!!.account +
                        ",name=" + mUserInfo!!.name
            )
        }

        initHileiaToken()
        MyMqttManager.instance?.init(this)

        mHandler!!.removeCallbacksAndMessages(null)
        mHandler!!.sendEmptyMessage(MSG_GET_LAST_LOC);
        mHandler!!.sendEmptyMessageDelayed(MSG_CHECK_NETWORK, 500)
//        mHandler!!.sendEmptyMessageDelayed(MSG_SYNC_COPTASK, 10000)
    }

    var isInitHileia: Boolean = false
    private fun initHileiaToken() {
        val map: MutableMap<String, String> = HashMap()
        map["account"] = mUserInfo!!.account
        map["name"] = mUserInfo!!.name
        AR110Log.i(TAG, "copNum=" + mUserInfo!!.account + ",name=" + mUserInfo!!.name)
        GlobalScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    var result: HttpResult<AccountData>? = ar110Api.getToken(map)
                    if (result != null) {
                        AR110Log.i(TAG, "获取HIleia账号信息结果 getToken result.data=${result.data}")
                    }
                    if (result?.isSuccess == true && result.data != null) {
                        var data: AccountData = result.data!!
                        if (null != data.token) {
                            AR110Log.i(TAG, "hileia token=${data.token}")
                            initLeiaBoxEngine()
                            LeiaBoxEngine.getInstance().addMessageCallback(this@AR110BaseService)
                            loginWithToken(data.token)
                            isInitHileia = true
                        }
                        //目前外网无emergencyId
                        if (null != data.emergencyId) {
                            mHileiaUserId = data.emergencyId
                        }
                    }
                }
            }.onFailure {
                isInitHileia = false
                AR110Log.i(TAG, "get    `Token onFailure ${it.message}")
            }
        }
    }

    override fun onDestroy() {
        AR110Log.i(TAG, "onDestroy !!!")
        LeiaBoxEngine.getInstance().removeMessageCallback(this)
        mHandler!!.removeCallbacksAndMessages(null)
        //locationManager.removeUpdates(locationListenerNetwork);
        if (BuildConfig.DEBUG) {
            //locationService.unregisterListener(mListener); //注销掉监听
            //locationService.stop(); //停止定位服务
        } else {
            unregisterReceiver(mReceiver)
            //getContentResolver().unregisterContentObserver(mContentObserver);
        }
        mVideoUploader!!.stopWorkThread()
        MyMqttManager.instance?.onDestroy()
        super.onDestroy()
    }

    /**
     * 位置更新部分
     */
    var mLastLocationData: LocationData? = null
    private val mObjLoc = Any()

    val currentLoc: LocationData?
        get() {
            synchronized(mObjLoc) {
                return if (null == mLastLocationData) {
                    null
                } else LocationData(mLastLocationData)
            }
        }

    val lastLocation: Unit
        @SuppressLint("MissingPermission") get() {
            if (!Util.isLocationEnabled()) {
                Util.showMessage("请打开位置服务！")
                getLastLocDelay()
                return
            }
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.permission(PermissionConstants.LOCATION).request()
                return
            }
            var locGps = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            //AR110Log.i("gps","handle MSG_GET_LAST_LOC");
            if (null != locGps) {
                AR110Log.i(
                    "location",
                    "last gps location:" + locGps.latitude + "_" + locGps.longitude
                )
                getLastLocDelay()
                handleNewLocation(locGps)
            } else {
                //if(BuildConfig.DEBUG) {
                locGps = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (null != locGps) {
                    AR110Log.i(
                        "location",
                        "last network location:" + locGps.latitude + "_" + locGps.longitude
                    )
                    getLastLocDelay()
                    handleNewLocation(locGps)
                } else {
                    locGps =
                        locationManager!!.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    if (null != locGps) {
                        Util.showMessage("use passive network")
                        getLastLocDelay()
                        handleNewLocation(locGps)
                    } else {
                        Util.showMessage("请到室外定位！")
                    }
                }
            }
        }

    private fun getLastLocDelay() {
        getLastLocDelay(GPS_UPDATE_INTERVEL.toLong())
    }

    private fun getLastLocDelay(delay: Long) {
        mHandler!!.removeMessages(MyMqttManager.MSG_GET_LAST_LOC)
        mHandler!!.sendEmptyMessageDelayed(MyMqttManager.MSG_GET_LAST_LOC, delay)
    }

    fun startLocationReport() {
        if (null != locationManager) {
            registLocationListener()
        } else {
            Util.showMessage("无法获取位置管理器")
            AR110Log.i(TAG, "无法获取位置管理器")
        }
        AR110Log.e(TAG, "startLocationReport mHandler=$mHandler this=$this")
        getLastLocDelay(0)
    }

    private var locationManager: LocationManager? = null

    @SuppressLint("MissingPermission")
    private fun registLocationListener() {
        AR110Log.i(TAG, "BaiduLocServer registLocationListener")
        try {
            locationManager!!.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                GPS_UPDATE_INTERVEL.toLong(), 5f, locationListenerNetwork
            )
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                GPS_UPDATE_INTERVEL.toLong(), 5f, locationListenerNetwork
            )
            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                GPS_UPDATE_INTERVEL.toLong(), 5f, locationListenerNetwork
            )
        } catch (e: SecurityException) {
            Toast.makeText(
                Utils.getApp(), "没有给AR110授权位置权限，请在系统设置中给AR110授权位置权限！",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private var locationListenerNetwork: LocationListener = object : LocationListener {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            AR110Log.i("gps", "onStatusChanged:$provider")
        }

        // Provider被enable时触发此函数，比如GPS被打开
        override fun onProviderEnabled(provider: String) {
            AR110Log.i(provider, "onProviderEnabled:$provider")
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        override fun onProviderDisabled(provider: String) {
            AR110Log.i(provider, "onProviderDisabled:$provider")
        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        override fun onLocationChanged(location: Location) {
            val lat = location.latitude
            val locLong = location.longitude
            val loc = "$lat,$locLong"
            AR110Log.i(TAG, "onLocationChanged loc=$loc")
            getLastLocDelay(GPS_UPDATE_TIMEOUT.toLong())
            handleNewLocation(location)
        }
    }
    private var mCurrentCopTask: CopTaskRecord? = null
    private var mPatrolTask: PatrolRecord? = null
    private var mPopulationRecord: PopulationRecord? = null
    private val mObjTask = Any()
    fun setCopTask(copData: CopTaskRecord?, patrolRecord: PatrolRecord?) {
        synchronized(mObjTask) {
            mCurrentCopTask = copData
            mPatrolTask = patrolRecord
            mPopulationRecord = null
        }
    }

    fun setPopulationTask(populationRecord: PopulationRecord) {
        synchronized(mObjTask) {
            mPopulationRecord = populationRecord
            mCurrentCopTask = null
            mPatrolTask = null
        }
    }

    private var lastUpdateGpsTime = 0L
    private fun handleNewLocation(location: Location?) {
        AR110Log.i("location", "handleNewLocation ")
        if (location != null) {
            val gpsTime = System.currentTimeMillis()
            if (gpsTime - lastUpdateGpsTime < 1000 * 2) {
                return
            }
            lastUpdateGpsTime = gpsTime
            val locData = LocationData()
            locData.altitude = location.altitude.toString()
            locData.imei = Util.getAndroidID()
            locData.imsi = Util.getAndroidID()
            locData.longitude = location.longitude.toString()
            locData.latitude = location.latitude.toString()
            locData.gps_time = gpsTime.toString()
            locData.last_location = ""
            locData.bearing = location.bearing.toString()
            locData.speed = location.speed.toString()
            locData.deviceId = locData.imei
            if (locData.latitude != null && locData.latitude.isEmpty()) {
                AR110Log.i("location", "latitude is wrong !!!")
                return
            }
            synchronized(mObjLoc) { mLastLocationData = LocationData(locData) }
            //通知界面刷新位置
            CommEvent(CommEventTag.LOCATION_UPDATE, locData).sendEvent()
            var sendStr: String? = null
            GlobalScope.launch {
                kotlin.runCatching {
                    withContext(Dispatchers.IO) {
                        var url: String? = null
                        if (null != mCurrentCopTask || mCurrentCopTask == null
                            && mPatrolTask == null
                            && mPopulationRecord == null
                        ) {
                            url = Util.mLocationUrl
                            val repdata = LocationReportData(locData, mUserInfo)
                            repdata.manufactor = "hiscene"
                            repdata.deviceType = "glasses"
                            synchronized(mObjTask) {
                                if (mCurrentCopTask != null) {
                                    repdata.jjdbh = mCurrentCopTask!!.jjdbh
                                    repdata.cjdbh = mCurrentCopTask!!.cjdbh
                                    repdata.cjdbh2 = mCurrentCopTask!!.cjdbh2
                                } else {
                                    repdata.jjdbh = ""
                                    repdata.cjdbh = ""
                                    repdata.cjdbh2 = ""
                                }
                            }
                            if (null != mUserInfo) {
                                repdata.jybh = mUserInfo!!.account
                                repdata.jyxm = mUserInfo!!.name
                            }
                            sendStr = repdata.toJson()

                        } else if (mPatrolTask != null) {
                            url = Util.mPatrolLocationAdd
                            val repdata = PatrolLocationReportData(locData, mUserInfo)
                            repdata.manufactor = "hiscene"
                            repdata.deviceType = "glasses"
                            synchronized(mObjTask) {
                                if (mPatrolTask != null) {
                                    repdata.patrolNumber = mPatrolTask!!.number
                                }
                            }
                            if (null != mUserInfo) {
                                repdata.jybh = mUserInfo!!.account
                                repdata.jyxm = mUserInfo!!.name
                            }
                            sendStr = repdata.toJson()
                        } else if (mPopulationRecord != null) {
                            url = Util.mPopulationLocationAdd
                            val repdata = PatrolLocationReportData(locData, mUserInfo)
                            repdata.manufactor = "hiscene"
                            repdata.deviceType = "glasses"
                            synchronized(mObjTask) {
                                if (mPopulationRecord != null) {
                                    repdata.verificationNumber = mPopulationRecord!!.number
                                }
                            }
                            if (null != mUserInfo) {
                                repdata.jybh = mUserInfo!!.account
                                repdata.jyxm = mUserInfo!!.name
                            }
                            sendStr = repdata.toJson()
                        }

                        MyMqttManager.instance!!.updateLoc(location)
                        val requestBody =
                            sendStr?.toRequestBody("application/json".toMediaTypeOrNull())

                        AR110Log.i(TAG, "location request=${url}  requestBody=$sendStr")
                        val result = ar110Api.updateLocation(url!!, requestBody!!)
                            ?: return@withContext
                        AR110Log.i(TAG, "location ---"+ location.toString()+ "---"+result.retCode)
                        AR110Log.i(TAG, "location post  res=${result.data}")
                    }
                }.onFailure {
                    AR110Log.e(TAG, "location send error result=${it.message}")
                }
            }
        } else {
            AR110Log.i("location", "locGps is null")
        }
    }

    override fun onNewMessage(responseCode: Int, data: MultVal) {
        AR110Log.i("hileia", "receive handler: " + HandlerMsgIds.forNumber(responseCode))
        when (responseCode) {
            HandlerMsgIds.HD_MSG_ACCOUNT_LOGIN_SUCCESS_VALUE -> {
                setUpMediaEngine(
                    applicationContext,
                    LeiaBoxEngine.getInstance().accountManager().userInfo.userID
                )
                LeiaBoxEngine.getInstance().imManager().connectIM()
                val obtain = Message.obtain(
                    null,
                    ConstantApp.ServiceMessage.MSG_HILEIA_LOGIN_CALLBACK
                )
                obtain.obj = true
                sendMessageToClient(obtain)
            }
            HandlerMsgIds.HD_MSG_ACCOUNT_LOGIN_FAILED_VALUE -> {
                val obtain = Message.obtain(
                    null,
                    ConstantApp.ServiceMessage.MSG_HILEIA_LOGIN_CALLBACK
                )
                obtain.obj = false
                sendMessageToClient(obtain)
            }
            HandlerMsgIds.HD_MSG_CALL_SESSION_STATE_CHANGED_VALUE -> {
                val sessionStateInfo: SessionStateInfo?
                try {
                    sessionStateInfo = SessionStateInfo.parseFrom(data.buf)
                    AR110Log.i(
                        "hileia",
                        "session state: " + Enums.CallSessionState.forNumber(sessionStateInfo.sessionState) + " id: " + sessionStateInfo.sessionId
                    )
                    when (sessionStateInfo.sessionState) {
                        Enums.CallSessionState.SESSION_WAIT_FOR_ANSWER_VALUE -> {
                            run {
                                val session = LeiaBoxEngine.getInstance().callManager()
                                    .getSessionById(sessionStateInfo.sessionId)
                                if (session.callType == Enums.CallType.INCOMING) {
                                    //来电
                                    AR110Log.i("hileia", "来电了！！！")
                                    mSessionId = sessionStateInfo.sessionId
                                    val obtain = Message.obtain(
                                        null,
                                        ConstantApp.ServiceMessage.MSG_HILEIA_CALLING_CALLBACK
                                    )
                                    val hileiaMessageData = HileiaMessageData(mSessionId, true)
                                    obtain.obj = hileiaMessageData
                                    sendMessageToClient(obtain)
                                }
                            }
                            run {
                                mSessionId = sessionStateInfo.sessionId
                                val obtain = Message.obtain(
                                    null,
                                    ConstantApp.ServiceMessage.MSG_HILEIA_CALLING_CALLBACK
                                )
                                val hileiaMessageData = HileiaMessageData(mSessionId, true)
                                obtain.obj = hileiaMessageData
                                sendMessageToClient(obtain)
                            }
                        }
                        Enums.CallSessionState.SESSION_TALKING_VALUE -> {
                            mSessionId = sessionStateInfo.sessionId
                            val obtain = Message.obtain(
                                null,
                                ConstantApp.ServiceMessage.MSG_HILEIA_CALLING_CALLBACK
                            )
                            val hileiaMessageData = HileiaMessageData(mSessionId, true)
                            obtain.obj = hileiaMessageData
                            sendMessageToClient(obtain)
                        }
                        Enums.CallSessionState.SESSION_CLOSED_VALUE -> {

                            //挂断
                            if (!TextUtils.isEmpty(mSessionId) && mSessionId == sessionStateInfo.sessionId) {
                                AR110Log.i("hileia", "挂断了！！！")
                                val obtain = Message.obtain(
                                    null,
                                    ConstantApp.ServiceMessage.MSG_HILEIA_HANGUP_CALLBACK
                                )
                                obtain.obj = sessionStateInfo.sessionId
                                sendMessageToClient(obtain)
                                mSessionId = ""
                            }
                        }
                    }
                } catch (e: InvalidProtocolBufferException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setUpMediaEngine(context: Context?, id: String) {
        AR110Log.i(TAG, "setUpMediaEngine userId=$id")
        val config = LeiaBoxEngine.getInstance().accountManager().configInfo
        val engineConfigInfo = EngineConfigInfo()
        when (config.mediaType) {
            Enums.MediaSource.MediaSAgora_VALUE -> {
                engineConfigInfo.appKey = config.agora.appKey
                engineConfigInfo.mediaType = MediaType.MediaSAgora_VALUE
            }
            Enums.MediaSource.MediaSHisrtc_VALUE -> {
                engineConfigInfo.appKey = config.hisrtc.appKey
                engineConfigInfo.appSecret = config.hisrtc.appSecret
                engineConfigInfo.serverAddressCount = config.hisrtc.serverAddressCount
                engineConfigInfo.serverAddressList = config.hisrtc.serverAddressList
                engineConfigInfo.mediaType = MediaType.MediaSHisrtc_VALUE
            }
        }
        MediaEngineHolder.Instance().setUpMediaEngine(context, id, engineConfigInfo)
    }

    fun loginWithToken(token: String) {
        AR110Log.e(TAG, "TOKEN = $token")
        LeiaBoxEngine.getInstance().accountManager().loginWithToken(token)
        //LeiaBoxEngine.getInstance().accountManager().login("17621110003", "A123456");
    }

    fun isVideoUploading(taskId: String?): Boolean {
        return if (null != mVideoUploader) {
            mVideoUploader!!.isVideoUploading(taskId)
        } else false
    }

    fun sendVideouploadTask(task: CopTaskRecord?) {
        AR110Log.d(TAG, "sendVideouploadTask")
        mVideoUploader?.requestVideoUpload(task)
    }

    fun sendPatrolVideouploadTask(task: PatrolRecord?) {
        AR110Log.d(TAG, "sendPatrolVideouploadTask")
        mVideoUploader?.requestPatrolVideoUpload(task)
    }

    fun cancleUploadTask() {
        if (null != mVideoUploader) {
            mVideoUploader!!.cleanWorkQueue()
        }
    }

    companion object {
        private const val TAG = "AR110BaseService"
        private const val MSG_GET_LOCATION = 399
        const val MSG_GET_LAST_LOC = MSG_GET_LOCATION - 1
        const val MSG_CHECK_NETWORK = MSG_GET_LOCATION - 2
        const val MSG_CLEAN_CACHE_FILE = MSG_GET_LOCATION - 3

        @JvmField
        var mUserInfo: UserInfoData? = null
        private const val GPS_UPDATE_INTERVEL = 10000
        private const val GPS_UPDATE_TIMEOUT = 20000
        private var mInstance: AR110BaseService? = null
        var isInitialized = false
            private set
        var mHileiaUserId: Long = -1
        val instance: AR110BaseService?
            get() {
                if (null == mInstance) {
                    mInstance = AR110BaseService()
                }
                return mInstance
            }
    }

    init {
        mInstance = this
    }
}