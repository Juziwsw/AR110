package com.hiar.ar110.activity

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.com.cybertech.pdk.OperationLog
import cn.com.cybertech.pdk.UserInfo
import cn.com.cybertech.pdk.auth.Oauth2AccessToken
import cn.com.cybertech.pdk.auth.PstoreAuth
import cn.com.cybertech.pdk.auth.PstoreAuthListener
import cn.com.cybertech.pdk.auth.sso.SsoHandler
import cn.com.cybertech.pdk.exception.PstoreAuthException
import cn.com.cybertech.pdk.exception.PstoreException
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mediaengine.Constant
import com.example.mediaengine.engines.MediaEngineHolder
import com.hiar.ar110.BuildConfig
import com.hiar.ar110.R
import com.hiar.ar110.data.LocationData
import com.hiar.ar110.data.UserInfoData
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.EventLiveBus
import com.hiar.ar110.event.sendEvent
import com.hiar.ar110.extension.visible
import com.hiar.ar110.fragment.HomePageFragment
import com.hiar.ar110.fragment.JDHandleFragment
import com.hiar.ar110.helper.MainActivityServiceHelper
import com.hiar.ar110.helper.NavigationHelper.Companion.TAG_HOME_PAGE
import com.hiar.ar110.helper.NavigationHelper.Companion.TAG_JD_HANDLE
import com.hiar.ar110.helper.NavigationHelper.Companion.TAG_JD_LIST
import com.hiar.ar110.helper.NavigationHelper.Companion.instance
import com.hiar.ar110.impl.OnGlassEventImpl
import com.hiar.ar110.sensor.SensorUtil
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.MainViewModel
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.toast.ToastUtils
import com.hiar.usb.DeviceFilter
import com.hiar.usb.IFrameCallback
import com.hiar.usb.USBMonitor
import com.hileia.common.utils.XLog
import com.rokid.glasssdk.GlassControl
import com.rokid.glasssdk.GlassEvent
import com.rokid.glasssdk.OnGlassEvent
import com.serenegiant.usbcameracommon.UVCCameraHandler
import kotlinx.android.synthetic.main.activity_a_r110_main.*
import java.io.File
import java.lang.ref.WeakReference

class AR110MainActivity : AppCompatActivity() {
    var mCameraHandler: UVCCameraHandler? = null
    private var mUSBMonitor: USBMonitor? = null
    private var mGlassEvent: GlassEvent? = null
    private var mGlassCtrl: GlassControl? = null
    private var mUsbManager: UsbManager? = null
    private var mUsbDevice: UsbDevice? = null
    private var mPermissionIntent: PendingIntent? = null
    private var mViewModel: MainViewModel? = null

    /**
     * for camera preview display
     */
//    private var mUVCCameraView: CameraViewInterface? = null
    private var mHandler: Handler? = null

    //    private var mCameraView: View? = null
    private var mNeedOcr = true

    companion object {
        private const val TAG = "AR110MainActivity"
        var mIsInPackage = false
        private const val ACTION_USB_PERMISSION = "com.rokid.glassdemo.USB_PERMISSION"
        var mNeedCar = 1

        /**
         * set true if you want to record movie using MediaSurfaceEncoder
         * (writing frame data into Surface camera from MediaCodec
         * by almost same way as USBCameratest2)
         * set false if you want to record movie using MediaVideoEncoder
         */
        private const val USE_SURFACE_ENCODER = false

        /**
         * preview mode
         * if your camera does not support specific resolution and mode,
         * [UVCCamera.setPreviewSize] throw exception
         * 0:YUYV, other:MJPEG
         */
        private const val PREVIEW_MODE = 1
        var oauth2AccessToken: Oauth2AccessToken? = null
        var isPushing = false
            private set
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState != null) { //存在Bundle数据,去除fragments的状态保存，解决Fragme错乱问题。
            val FRAGMENTS_TAG = "android:support:fragments"
            outState.remove(FRAGMENTS_TAG)
        }
    }

    /*绑定服务*/
    private fun initService() {
    }


    private fun startService() {
        serviceHelper!!.startService()
    }

    /*设置OCR检测值*/
    private fun initParam() {

    }

    private fun stopOcr() {

    }

    fun showLargeImg(url: String?) {
        if (img_takepic == null) {
            return
        }
        layout_show_photo.visible()
        img_takepic.visible()
        val file = File(url)
        if (file.exists()) {
            Log.d(TAG,"showLargeImg  file:${url}")
            Glide.with(applicationContext).load(File(url)).diskCacheStrategy(DiskCacheStrategy.NONE).into(img_takepic)
        } else {
            Log.d(TAG,"showLargeImg  url:${url}")
            Glide.with(applicationContext).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).into(img_takepic)
        }
    }

    fun hideLargeImg() {
        if (img_takepic == null) {
            return
        }
        layout_show_photo!!.visibility = View.INVISIBLE
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (mIsInPackage) {
            true
        } else super.dispatchTouchEvent(ev)
    }

    private fun onCoptaskAdd(messageData: String) {
        val mHomePageFragment = instance.findFragment(TAG_HOME_PAGE)
        if (mHomePageFragment != null && mHomePageFragment is HomePageFragment) {
            mHomePageFragment.refreshData()
        }
    }

    private inner class ClientHandler(mainActivity: AR110MainActivity) : Handler() {
        private val mWeakActivity: WeakReference<AR110MainActivity>?
        override fun handleMessage(msg: Message) {
            if (mWeakActivity == null) {
                return
            }
            if (msg.what == SensorUtil.MSG_IS_NEAR) {
                mIsInPackage = true
                onBrightNessChange(true)
                return
            }
            if (msg.what == SensorUtil.MSG_IS_FAR) {
                mIsInPackage = false
                onBrightNessChange(false)
                return
            }
            if (msg.what == SensorUtil.MSG_CHECK_SERVICE_RUNNING) {
                if (isServiceRunning) {
                    AR110BaseService.instance!!.startLocationReport()
                }
            }
        }

        init {
            mWeakActivity = WeakReference(mainActivity)
        }
    }

    private var serviceHelper: MainActivityServiceHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_a_r110_main)
        mViewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory(application))[MainViewModel::class.java]
        lifecycle.addObserver(mViewModel!!)
        initListener()
        initObserve()
        initService()
        instance.init(this)
        serviceHelper = MainActivityServiceHelper(this)
        //        mBaseActivity = this;
        mHandler = ClientHandler(this)
        if (!BuildConfig.DEBUG) {
            Handler().postDelayed({ ssoHandler() }, 500)
        } else {
            val androidId = Util.getAndroidID()
            val mUserInfoData = Util.getUserInfo(this)
            AR110Log.i(TAG, "androidid=" + androidId + "name="
                    + mUserInfoData.name + ",account=" + mUserInfoData.account + ",idcard=" + mUserInfoData.idcard)
            mViewModel!!.addUserInfo(mUserInfoData)
            if (!isServiceRunning) {
                AR110BaseService.mUserInfo = UserInfoData(mUserInfoData)
                AR110Log.i(TAG, "start service")
                startService()
            } else {
                AR110BaseService.mUserInfo = UserInfoData(mUserInfoData)
                AR110Log.i(TAG, "service is running")
            }
            mHandler!!.sendEmptyMessageDelayed(SensorUtil.MSG_CHECK_SERVICE_RUNNING, 2000)
        }
        mUSBMonitor = USBMonitor(this, mOnDeviceConnectListener)
        mCameraHandler = UVCCameraHandler.createHandler(this, camera_view,
                if (USE_SURFACE_ENCODER) 0 else 1, Util.PREVIEW_WIDTH, Util.PREVIEW_HEIGHT, PREVIEW_MODE)
        if (BuildConfig.DEBUG) {
            instance.beginHomePage()
        }
        mUsbManager = getSystemService(USB_SERVICE) as UsbManager
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
    }


    private fun initListener() {
        layout_show_photo!!.setOnClickListener { view: View? -> layout_show_photo!!.visibility = View.GONE }
    }
    private fun initObserve() {
        EventLiveBus.commtEvent.observe(this, Observer {
            if (it.tag == CommEventTag.COPTASK_ADD) {
                onCoptaskAdd(it.data as String);
            }else if(it.tag == CommEventTag.LOCATION_UPDATE){
                instance.updateGpsLocation(it.data as LocationData)
            }else if(it.tag == CommEventTag.USER_INFO_UPDATE){
                mHandler!!.postDelayed({
                    mViewModel!!.addUserInfo(AR110BaseService.mUserInfo)
                },2000)

            }
        })
    }
    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            mGlassCtrl = GlassControl(context, it)
                            mGlassEvent = GlassEvent(context, it)
                            mGlassEvent!!.SetOnGlassEvent(mOnGlassEvent)
                        }
                    } else {
                        XLog.i(TAG, "permission denied for device $device")
                    }
                }
            }
        }
    }
    private var auth: PstoreAuth? = null
    private var ssoHandler: SsoHandler? = null

    //sso登录
    private fun ssoHandler() {
        auth = PstoreAuth(this, Util.APP_ID_JWT)
        ssoHandler = SsoHandler(this, auth)
        ssoHandler!!.authorize(AuthListener())
    }

    //sso刷新
    private fun ssoHandlerRefresh() {
        auth = PstoreAuth(this, Util.APP_ID_JWT)
        ssoHandler = SsoHandler(this, auth)
        ssoHandler!!.authorizeRefresh(AuthListener())
    }

    private fun authLogin() {
        try {
            val user = UserInfo.getUser(this)
            if (user != null) {
                AR110Log.i(TAG, "获得用户信息: name=" + user.name + ",uid=" + user.uid + "," +
                        "account=" + user.account)
                AR110BaseService.mUserInfo = UserInfoData(user)
                mViewModel!!.addUserInfo(AR110BaseService.mUserInfo)
                if (!isServiceRunning) {
                    AR110Log.i(TAG, "start service")
                    startService()
                } else {
                    AR110Log.i(TAG, "service is running")
                }
                mHandler!!.sendEmptyMessageDelayed(SensorUtil.MSG_CHECK_SERVICE_RUNNING, 2000)
            }
        } catch (e: Exception) {
            AR110Log.i(TAG, "authLogin exception:$e")
            e.printStackTrace()
        }
        try {
            OperationLog.logging(this, Util.APP_ID_JWT, "", 0, 0, 1, "")
        } catch (e: Exception) {
            Util.showMessage("发送登录日志失败")
            e.printStackTrace()
        }
        instance.beginHomePage()
    }

    private val mSBuffer = StringBuffer()
    fun setBrightness(bright: Int) {
        onBrightNessChange(bright == 0)
    }

    fun onBrightNessChange(inPackage: Boolean) {
        if (instance.findFragment(TAG_JD_HANDLE) != null) {
            if (!Util.mNeedMultiScreen) {
                if (inPackage) {
                    //hideStatusNavigationBar();
                    if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                    mIsInPackage = true
                    val lp = window.attributes
                    lp.screenBrightness = java.lang.Float.valueOf(0f) * (1f / 255f)
                    window.attributes = lp
                } else {
                    //showNavigationBar();
                    mIsInPackage = false
                    val lp = window.attributes
                    lp.screenBrightness = -1f
                    window.attributes = lp
                    if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
        }
    }

    /**
     * onComplete回掉后执行：
     * 1. 使用access_token去APP SERVER登录;
     * 2. 若未绑定账号，则先绑定账号；
     * 3. 若有其他问题，则解决；
     * 4. 进入APP。
     * 具体流程见文档（android-pstore-sdk-v2.xx）2.1流程图。
     */
    internal inner class AuthListener : PstoreAuthListener {
        override fun onComplete(accessToken: Oauth2AccessToken) {
            oauth2AccessToken = accessToken
            mSBuffer.append(accessToken.toString())
            //showWithText(mTextInfo);
            val expiresTime = accessToken.expiresTime
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis < expiresTime) {
                oauth2AccessToken = accessToken
            } else {
                ssoHandler!!.authorizeRefresh(AuthListener())
            }

            //这里拿到了授权信息 前往登录
            authLogin()
        }

        override fun onPstoreException(pstoreException: PstoreException) {
            if (pstoreException is PstoreAuthException) {
                Util.showMessage(getString(R.string.login_error_notice))
            } else {
                Util.showMessage(getString(R.string.unknown_error))
            }
        }

        override fun onCancel() {}
    }

    private val isServiceRunning: Boolean
        private get() {
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if ("com.hiar.ar110.service.AR110BaseService" == service.service.className) {
                    return true
                }
            }
            return false
        }

    fun onHileiaComingCallCallback(success: Boolean, sessionId: String?) {
        if (mCameraHandler != null) {
            if (success) {
                instance.onHileiaComingCallCallback()
                isPushing = mViewModel!!.enterChannel(sessionId)
            }
        }
    }

    fun onHileiaHangupCallback(sessionId: String?) {
        isPushing = false
        mViewModel!!.leaveChannel()
        instance.onHileiaHangupCallback()
    }

    fun onHileiaLoginRes(loginRes: Boolean) {
        if (loginRes) {
            runOnUiThread { Util.showMessage("登录成功！") }
        } else {
            runOnUiThread { Util.showMessage("登录失败！") }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                //成功
            }
        } else {
            if (ssoHandler != null) {
                ssoHandler!!.authorizeCallBack(requestCode, resultCode, data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        AR110Log.i(TAG, "onConfigurationChanged--->$newConfig")
        super.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        AR110Log.i(TAG, "onResume")
        if (Util.mNewMsgCjdbh2 != null) {
            if (instance.findFragment(TAG_JD_LIST) != null) {
                instance.backToJdList(true)
            }
        }
        mNeedCar = Util.getIntPref(applicationContext, Util.KEY_CAR_RECOG,
                Util.DEF_CAR_RECOG_MODE)
        super.onResume()
    }

    override fun onPause() {
        AR110Log.i(TAG, "onPause")
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        AR110Log.i(TAG, "onStart")
        SensorUtil.start(this, mHandler)
        mShowLogcat = false
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (null != mUSBMonitor) {
            mUSBMonitor!!.register()
            if (camera_view != null) {
                camera_view!!.onResume()
            }
        }
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
        if (mGlassEvent != null) {
            if (null != mOnGlassEvent) {
                mGlassEvent!!.SetOnGlassEvent(mOnGlassEvent)
            }
        }
    }

    override fun onStop() {
        AR110Log.i(TAG, "onStop")
//        if (null != mBackgroundHandler) {
//            mBackgroundHandler!!.removeCallbacksAndMessages(this)
//        }
        if (isPushing) {
            mViewModel!!.hangup()
            isPushing = false
            instance.onHileiaHangupCallback()
        }
        camera_view?.onPause()
        mCameraHandler?.close()
        mUSBMonitor?.unregister()
        if (mGlassEvent != null) {
            mGlassEvent!!.SetOnGlassEvent(null)
        }
        unregisterReceiver(usbReceiver)
        SensorUtil.stop()
        super.onStop()
    }

    override fun onDestroy() {
        AR110Log.i(TAG, "onDestroy")
        mCameraHandler?.setFrameCallback(null)
        mCameraHandler?.release()
        stopOcr()
        instance.onDestroy()
        super.onDestroy()
    }

    private fun startPreview() {
        val st = camera_view!!.surfaceTexture
        if (st != null) {
            mCameraHandler!!.setFrameCallback(mFrameCallback)
            mCameraHandler!!.startPreview(Surface(st))
        } else {
            XLog.e(TAG, "surfaceTexture must not be null")
        }
    }

    private var mNeedCaptureFrame = false
    private var frameCount: Long = 0
    private var frameTotal: Long = 0
    private var timeUsed: Long = 0
    private var lastTime: Long = 0

    private var yuvData: ByteArray? = null
    private var uvData: ByteArray? = null
    private val mFrameCallback = IFrameCallback { byteBuffer,l ->
        val yLen = Util.PREVIEW_WIDTH * Util.PREVIEW_HEIGHT
        val uvLen = yLen shr 1
        if (yuvData == null) {
            yuvData = ByteArray(yLen + uvLen)
        }
        byteBuffer[yuvData, 0, yLen]
        if (uvData == null) {
            uvData = ByteArray(uvLen)
        }
        byteBuffer[uvData, 0, uvLen]
        var i = 0
        while (i < uvLen) {
            yuvData!![yLen + i] = uvData!![i + 1]
            yuvData!![yLen + i + 1] = uvData!![i]
            i += 2
        }
        frameCount++
        mCameraFrameCallback?.handleOcr(0, yuvData!!, mNeedOcr)

        frameTotal++
        if (frameCount == -1L) {
            XLog.d("frame", "get first frame !!!")
            lastTime = System.currentTimeMillis()
        }
        timeUsed = System.currentTimeMillis() - lastTime
        if (timeUsed >= 1000) {
            XLog.d("frame", "frame_rate=$frameTotal")
            frameTotal = 0
            lastTime = System.currentTimeMillis()
        }
        if (mNeedCaptureFrame) {
            mNeedCaptureFrame = false
            mCameraFrameCallback?.takePicture(yuvData, Util.PREVIEW_WIDTH, Util.PREVIEW_HEIGHT)
        }
        if (isPushing) {
            val mediaEngine = MediaEngineHolder.Instance().mediaEngine
            val mirror = Constant.MirrorMode.VIDEO_MIRROR_MODE_DISABLED //Constant
            // .MirrorMode.VIDEO_MIRROR_MODE_ENABLED;
            val start = SystemClock.elapsedRealtime()
            mediaEngine.inputVideoFrame(yuvData, yuvData!!.size, Util.PREVIEW_WIDTH,
                    Util.PREVIEW_HEIGHT, Constant.VideoFMT.VIDEO_FMT_NV21, mirror,
                    System.currentTimeMillis())
        }
        if (null != mCameraFrameCallback) {
            mCameraFrameCallback!!.onFrameAvailable(yuvData, Util.PREVIEW_WIDTH, Util.PREVIEW_HEIGHT)
        }
    }

    private var hasAttach = false

    private val mOnDeviceConnectListener: USBMonitor.OnDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(usbDevice: UsbDevice) {
            if (!hasAttach) {
                hasAttach = true
                val filter = DeviceFilter.getDeviceFilters(this@AR110MainActivity,
                        R.xml.device_filter)
                val deviceList = mUSBMonitor!!.getDeviceList(filter[0])
                XLog.i(TAG, "device size: " + deviceList.size)
                if (!mCameraHandler?.isOpened!!) {
                    for (device in deviceList) {
                        XLog.i(TAG, "device info: " + device.toString() + " " + UsbConstants.USB_CLASS_VIDEO)
                        if (device.deviceClass == UsbConstants.USB_CLASS_VIDEO || device.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_VIDEO) {
                            mUSBMonitor!!.requestPermission(device)
                            hasAttach = true
                            break

                        }
                    }
                }
            }
        }

        override fun onDettach(device: UsbDevice) {
            XLog.i(TAG, "onDettach")
            hasAttach = false
        }

        override fun onConnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
            XLog.i(TAG, "onConnect:")
            XLog.i(TAG, "currentState ${lifecycle.currentState.name}")
            mCameraHandler?.open(ctrlBlock)
            startPreview()
            lastTime = System.currentTimeMillis()
            val mFragment = instance.findFragment(TAG_JD_HANDLE)
            CommEvent(CommEventTag.GLASS_CONNECT_STATE_CHANGE,true).sendEvent()
            if (mFragment != null && mFragment is JDHandleFragment) {
                mFragment.glassConnected()
            }
            val deviceList = mUsbManager!!.deviceList
            val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
            while (deviceIterator.hasNext()) {
                mUsbDevice = deviceIterator.next()
            }
            if (mUsbDevice!!.productId == 5678) {
                mUsbManager!!.requestPermission(mUsbDevice, mPermissionIntent)
            }
        }

        override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
            XLog.i(TAG, "onDisconnect:")
            val mFragment = instance.findFragment(TAG_JD_HANDLE)
            if (mFragment != null && mFragment is JDHandleFragment) {
                mFragment.glassDisConnected()
            }
            CommEvent(CommEventTag.GLASS_CONNECT_STATE_CHANGE,false).sendEvent()
            XLog.i(TAG, "currentState ${lifecycle.currentState.name}")
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                Util.showMessage("设备已拔出!")
            }
            mCameraHandler?.close()
            hasAttach = false
        }

        override fun onCancel(device: UsbDevice) {
            XLog.i(TAG, "onCancel:")
            hasAttach = false
        }
    }

    override fun onBackPressed() {
        instance.onBackPressed()
    }

    private var mCameraFrameCallback: CameraFrameCallback? = null
    fun setCameraFrameCallback(callback: CameraFrameCallback?) {
        mCameraFrameCallback = callback
    }

    interface CameraFrameCallback {
        fun onFrameAvailable(data: ByteArray?, width: Int, height: Int)
        fun takePicture(data: ByteArray?, width: Int, height: Int)
        fun handleOcr(mSdkInited: Int, yuvData: ByteArray,mNeedOcr: Boolean)
    }

    var mShowLogcat = false
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_UP) {
                onBackPressed()
            }
            return true
        }
        if (instance.findFragment(TAG_JD_HANDLE) != null) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && action == KeyEvent.ACTION_UP) {
                mNeedCaptureFrame = true
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && action == KeyEvent.ACTION_UP) {
                if (!mIsInPackage) {
                    mHandler!!.sendEmptyMessage(SensorUtil.MSG_IS_NEAR)
                } else {
                    mHandler!!.sendEmptyMessage(SensorUtil.MSG_IS_FAR)
                }
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private val mOriData = DoubleArray(3)
    val glassOrientation: DoubleArray
        get() {
            synchronized(mOriData) { return mOriData }
        }
    private val mOnGlassEvent: OnGlassEvent? = object : OnGlassEventImpl() {
        override fun OnKeyPress(keyCode: Int, press: Boolean) {
            if (press) {
                if (keyCode == 16) {
                    XLog.d("mode_switch", "press")
                }
            } else {
                if (keyCode == 16) {
                    XLog.d("mode_switch", "release")
                    mNeedCaptureFrame = true
                } else if (keyCode == 4) {
                    ToastUtils.show("开始呼叫")
                    if (AR110BaseService.mHileiaUserId != -1L) {
                        ToastUtils.show("SOS呼叫中")
                        mViewModel!!.call()
                    } else {
                        ToastUtils.show("未获取到ID")
                    }
                }
            }
        }
    }
}