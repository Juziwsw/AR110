package com.hiar.ar110.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.*
import android.os.StrictMode.VmPolicy
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hiar.ar110.ConstantApp
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.activity.AR110MainActivity.CameraFrameCallback
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.LocationData
import com.hiar.ar110.data.MediaLocationData
import com.hiar.ar110.data.TargetInfo
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.vehicle.VehicleInfo
import com.hiar.ar110.data.vehicle.VehicleRecogList
import com.hiar.ar110.diskcache.AudioLocationDiskCache
import com.hiar.ar110.diskcache.VideoLocationDiskCache
import com.hiar.ar110.extension.setOnThrottledClickListener
import com.hiar.ar110.factory.RecogFactory
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.mutiscreen.SecondaryScreen
import com.hiar.ar110.mutiscreen.SecondaryScreen.OnSecondScreenCallback
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.service.MyMqttManager
import com.hiar.ar110.util.Base64Util
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.JDHandleViewModel
import com.hiar.ar110.widget.GestureLayout.OnMyGestrueListener
import com.hiar.ar110.widget.MySurfaceView
import com.hiar.ar110.widget.SoundSelConstraintLayout
import com.hiar.mybaselib.recog.FaceRecognitionInfo
import com.hiar.mybaselib.recog.IFaceEngine
import com.hiar.mybaselib.recog.ILPREngine
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.toast.ToastUtils
import com.hileia.common.enginer.LeiaBoxEngine
import com.hileia.common.entity.proto.Enums
import com.hileia.common.utils.XLog
import com.serenegiant.usbcameracommon.UVCCameraHandler
import kotlinx.android.synthetic.main.fragment_jd_handle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class JDHandleFragment() : BaseFragment(), OnMyGestrueListener, OnSecondScreenCallback,
    CameraFrameCallback {

    private val mViewModel by lazy {
        getViewModel(JDHandleViewModel::class.java)
    }
    private lateinit var mFaceView: MySurfaceView
    private lateinit var mSoundPool: SoundPool
    private lateinit var mHandler: Handler

    private lateinit var mCameraHandler: UVCCameraHandler
    private lateinit var displayManager: DisplayManager
    private lateinit var initCoroutine: Job
    private lateinit var mDialog: AlertDialog

    private var mPeopleList: List<TargetInfo> = emptyList()
    private var mNoticeMusicId = 0
    private var mCarNoticeMusic = 0
    private var mShutterMusic = 0
    private var mCopTask: CopTaskRecord? = null
    private var mPatrolTask: PatrolRecord? = null
    private var mRecordTotalTime = 0
    private var mRecordSingleTime = 0
    private var mAudioRecordTime = 0
    private var frameCount = 0
    private var mFrameCountAfterFaceFound = 0
    private var mSecondaryScreen: SecondaryScreen? = null
    private var mLastImportantPeo: TargetInfo? = null
    private var mCopTaskSoundType = SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL
    private var mPatrolSoundType = SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT
    private var mLoadMusicOk = false
    private var isUseSecondScreen = false
    private var mTotalWarningNum = 0
    private var mTotalFatalNum = 0
    private var mFaceCmpIndex = 0
    private var mCurrentRecordFile: String? = null
    private val mLPREngine: ILPREngine = RecogFactory.getLPREngine()
    private val mFaceEngine: IFaceEngine = RecogFactory.getFaceEngine()

    @JvmField
    var mCurrentCJZT = CJZT_WCJ

    @JvmField
    var mNeedCar = 1

    //眼镜端布局 begin
    private var mLayoutLive: ConstraintLayout? = null
    private var mImgFaceLibGlass: ImageView? = null
    private var mImgFaceCropGlass: ImageView? = null
    private var mTextNameGlass: TextView? = null
    private var mTextConfGlass: TextView? = null
    private var mTextCrimeGlass: TextView? = null
    private var mTextIDCardGlass: TextView? = null
    private var mIconRecording: ImageView? = null
    private var mTextImportName: TextView? = null
    private var mTextImportID: TextView? = null
    private var mLayoutGlassBjd: ConstraintLayout? = null
    private var mLayoutGlassCar: ConstraintLayout? = null
    private var mLayoutGlassPeoResult: ConstraintLayout? = null
    private var mLayoutGlassContent: ConstraintLayout? = null
    private var mTagLayout: ConstraintLayout? = null
    private var mImgCarGlass: ImageView? = null
    private var mTextPlateNumberGlass: TextView? = null
    private var mTextCarBrandGlass: TextView? = null
    private var mImgCarTypeGlass: TextView? = null
    private var mTextCarColorGlass: TextView? = null
    private var mTextRecordTimeGlass: TextView? = null
    private var mGroupGlassRecordTime: Group? = null
    private var mSecondTextLegalName: TextView? = null
    private var mSecondTextDriverID: TextView? = null
    private var mTextFatalPeo: TextView? = null
    private var mTextFatalPeoNum: TextView? = null
    private var mTextWarningPeoNum: TextView? = null
    private var mTextBjrNote: TextView? = null
    private var mImgPicShowGlass: ImageView? = null
    private var isFirstStart: Boolean = true
    //眼镜端布局 end

    private fun showSecondScreen(): Boolean {
        return (AR110MainActivity.mIsInPackage || isUseSecondScreen)
    }

    override fun onSecondScreenStart() {
        initViewFromSecondScreen()
        isUseSecondScreen = true
    }

    override fun takePicture(data: ByteArray?, width: Int, height: Int) {
        playShutterSound()

        lifecycleScope.launch {
            val file = mViewModel.tackPicture(data, width, height, mCopTask, mPatrolTask)
            file?.let {
                mViewModel.sendSinglePhoto(file, mCopTask, mPatrolTask)
                val bmp: Bitmap = BitmapFactory.decodeFile(file.absolutePath)
                img_pic_show.setImageBitmap(bmp)
                img_photo_gather.setImageBitmap(bmp)
                layout_show_photo.visibility = View.VISIBLE
                mImgPicShowGlass?.setImageBitmap(bmp)
                mImgPicShowGlass?.visibility = View.VISIBLE
                delay(2000)
                layout_show_photo.visibility = View.INVISIBLE
                mImgPicShowGlass?.visibility = View.INVISIBLE
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //SYSTEM_ALERT_WINDOW权限申请
            if (!Settings.canDrawOverlays(Utils.getApp())) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:" + Utils.getApp().packageName) //不加会显示所有可能的app
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivityForResult(intent, 1)
            }
        }
    }

    private fun setVehicleDefShow(status: Int) {
        text_driver_idcard.text = ""
        text_driver_name.text = ""
        when (status) {
            CAR_STATUS_EMPTY -> {
                group_car_info.visibility = View.INVISIBLE
                group_no_vehicleinfo.visibility = View.INVISIBLE
                group_no_vehicle.visibility = View.VISIBLE
                text_car_number.visibility = View.INVISIBLE
                img_car.visibility = View.INVISIBLE
                text_car_brand.text = ""
            }
            CAR_STATUS_NOINFO -> {
                group_car_info.visibility = View.INVISIBLE
                group_no_vehicleinfo.visibility = View.VISIBLE
                group_no_vehicle.visibility = View.INVISIBLE
                text_car_number.visibility = View.VISIBLE
                img_car.visibility = View.VISIBLE
                text_car_brand.text = ""
            }
            CAR_STATUS_HASINFO -> {
                group_car_info.visibility = View.VISIBLE
                group_no_vehicleinfo.visibility = View.INVISIBLE
                group_no_vehicle.visibility = View.INVISIBLE
                text_car_number.visibility = View.VISIBLE
                img_car.visibility = View.VISIBLE
            }
        }
    }

    private fun updateGlassShowInfo(type: Int) {
        XLog.i(TAG, "updateGlassShowInfo type: $type")
        if (!showSecondScreen()) {
            return
        }
        mSecondTextDriverID?.text = ""
        mSecondTextLegalName?.text = ""
        if (type == GLASS_SHOW_BJD) {
            mLayoutGlassContent?.visibility = View.VISIBLE
            if (mLayoutGlassBjd?.visibility != View.VISIBLE) {
                mLayoutGlassBjd?.visibility = View.VISIBLE
                mLayoutGlassCar?.visibility = View.INVISIBLE
                mLayoutGlassPeoResult?.visibility = View.INVISIBLE
            }
        } else if (type == GLASS_SHOW_PEO) {
            mLayoutGlassContent?.visibility = View.VISIBLE
            if (mLayoutGlassPeoResult?.visibility != View.VISIBLE) {
                mLayoutGlassBjd?.visibility = View.INVISIBLE
                mLayoutGlassCar?.visibility = View.INVISIBLE
                mLayoutGlassPeoResult?.visibility = View.VISIBLE
            }
        } else if (type == GLASS_SHOW_CAR) {
            mLayoutGlassContent?.visibility = View.VISIBLE
            if (mLayoutGlassCar?.visibility != View.VISIBLE) {
                mLayoutGlassBjd?.visibility = View.INVISIBLE
                mLayoutGlassCar?.visibility = View.VISIBLE
                mLayoutGlassPeoResult?.visibility = View.INVISIBLE
            }
        } else if (type == GLASS_SHOW_TAG) {
            mTagLayout?.visibility = View.VISIBLE
            mLayoutGlassContent?.visibility = View.INVISIBLE
            mLayoutGlassBjd?.visibility = View.INVISIBLE
            mLayoutGlassCar?.visibility = View.INVISIBLE
            mLayoutGlassPeoResult?.visibility = View.INVISIBLE
        } else {
            mLayoutGlassContent?.visibility = View.VISIBLE
            if (mLayoutGlassBjd?.visibility != View.VISIBLE) {
                mLayoutGlassBjd?.visibility = View.VISIBLE
                mLayoutGlassCar?.visibility = View.INVISIBLE
                mLayoutGlassPeoResult?.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateVehicleInfo(
        path: String?, carInfo: VehicleInfo?,
        plateNum: String
    ) {
        mHandler.removeMessages(MSG_HIDE_FACECOMP)
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FACECOMP, 5000)
        activity?.runOnUiThread {
            if (carInfo != null) {
                setVehicleDefShow(CAR_STATUS_HASINFO)
                updateGlassShowInfo(GLASS_SHOW_CAR)
                if (null != path) {
                    val imgFile = File(path)
                    if (imgFile.exists()) {
                        Glide.with(context!!.applicationContext).load(File(path))
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(img_car)
                        mImgCarGlass?.let {
                            Glide.with(context!!.applicationContext).load(File(path))
                                .diskCacheStrategy(DiskCacheStrategy.NONE).into(mImgCarGlass)
                        }
                    } else {
                        val url = Util.getCarPhotoIpHead() + path
                        Glide.with(context!!.applicationContext).load(url)
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(img_car)
                        mImgCarGlass?.let {
                            Glide.with(context!!.applicationContext).load(url)
                                .diskCacheStrategy(DiskCacheStrategy.NONE).into(mImgCarGlass)
                        }
                    }
                }
                if (!TextUtils.isEmpty(carInfo.driverId)) {
                    text_driver_idcard.text = carInfo.driverId
                    mSecondTextDriverID?.text = carInfo.driverId
                }
                if (!TextUtils.isEmpty(carInfo.legalPersonName)) {
                    text_driver_name.text = carInfo.legalPersonName
                    mSecondTextLegalName?.text = carInfo.legalPersonName
                }
                if (!TextUtils.isEmpty(carInfo.carId)) {
                    text_car_number.text = carInfo.carId
                    mTextPlateNumberGlass?.text = carInfo.carId
                } else {
                    text_car_number.text = ""
                    mTextPlateNumberGlass?.text = ""
                }
                if (!TextUtils.isEmpty(carInfo.carBrand)) {
                    text_car_brand.text = carInfo.carBrand
                    mTextCarBrandGlass?.text = carInfo.carBrand
                } else {
                    text_car_brand.text = ""
                    mTextCarBrandGlass?.text = ""
                }
                if (!TextUtils.isEmpty(carInfo.carType)) {
                    text_car_type.text = carInfo.carType
                    mImgCarTypeGlass?.text = carInfo.carType
                } else {
                    text_car_type.text = ""
                    mImgCarTypeGlass?.text = ""
                }
                if (!TextUtils.isEmpty(carInfo.carColor)) {
                    text_car_color.text = carInfo.carColor
                    mTextCarColorGlass?.text = carInfo.carColor
                } else {
                    mTextCarColorGlass?.text = ""
                    text_car_color.text = ""
                }
            } else {
                setVehicleDefShow(CAR_STATUS_NOINFO)
                updateGlassShowInfo(GLASS_SHOW_CAR)
                if (null != path) {
                    val imgFile = File(path)
                    if (imgFile.exists()) {
                        img_car.visibility = View.VISIBLE
                        Glide.with(context!!.applicationContext).load(File(path))
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(img_car)
                        mImgCarGlass?.let {
                            Glide.with(context!!.applicationContext).load(File(path))
                                .diskCacheStrategy(DiskCacheStrategy.NONE).into(mImgCarGlass)
                        }
                    }
                }
                text_car_number.text = plateNum
                mTextPlateNumberGlass?.text = plateNum
            }
        }
    }

    private var mHandled = true

    override fun handleOcr(
        mSdkInited: Int,
        yuvData: ByteArray,
        mNeedOcr: Boolean
    ) {
        if (mCurrentCJZT == Util.CJZT_CJZ && mNeedCar == 2) {
            if (mHandled) {
                mHandled = false
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        if (mSdkInited == 0 && mNeedOcr) {
                            val recogList = mLPREngine.doRecog(
                                Util.PREVIEW_WIDTH,
                                Util.PREVIEW_HEIGHT,
                                yuvData
                            )
                            recogList.forEach {
                                handleSingleLprResult(it.result, it.imageData)
                            }
                        }
                        mHandled = true
                    }
                }
            }
        }
    }

    private fun handleSingleLprResult(
        fieldvalue: Array<String?>?,
        datas: ByteArray?
    ) {
        val lprThresh = Util.getIntPref(Utils.getApp(), Util.KEY_LPR_THRESH,Util.DEF_LPR_THRESHOOD)
        Log.d(TAG,"resArray handleSingleLprResult:${Arrays.toString(fieldvalue)}  lpr_thresh=${lprThresh}")
        if (fieldvalue!!.size > 4) {
            val conf = fieldvalue[4]
            if (conf != null) {
                try {
                    val value = conf.toInt()
                    if (value >= lprThresh) {
                        val mJDHandleFragment =
                            NavigationHelper.instance.findFragment(NavigationHelper.TAG_JD_HANDLE)
                        if (mJDHandleFragment != null && mJDHandleFragment is JDHandleFragment) {
                            newPlateHandle(datas, fieldvalue[0], fieldvalue[1])
                        }
                    }
                } catch (e: NumberFormatException) {
                }
            }
        }
    }

    private fun updateVehicleHistory(path: String?, carInfo: VehicleRecogList?, plateNum: String) {
        updateGlassShowInfo(GLASS_SHOW_BJD)
        if (carInfo != null) {
            setVehicleDefShow(CAR_STATUS_HASINFO)
            if (null != path) {
                val imgFile = File(path)
                if (imgFile.exists()) {
                    Glide.with(context!!.applicationContext).load(File(path))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).into(img_car)
                    mImgCarGlass?.let {
                        Glide.with(context!!.applicationContext).load(File(path))
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(mImgCarGlass)
                    }
                } else {
                    val url: String = Util.getCarPhotoIpHead() + path
                    Glide.with(context!!.applicationContext).load(url)
                        .diskCacheStrategy(DiskCacheStrategy.NONE).into(img_car)
                    mImgCarGlass?.let {
                        Glide.with(context!!.applicationContext).load(url)
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(mImgCarGlass)
                    }
                }
            }
            if (!TextUtils.isEmpty(carInfo.driverId)) {
                text_driver_idcard.text = carInfo.driverId
            }
            if (!TextUtils.isEmpty(carInfo.legalPersonName)) {
                text_driver_name.text = carInfo.legalPersonName
            }
            if (!TextUtils.isEmpty(carInfo.carBrand)) {
                text_car_brand.text = carInfo.carBrand
                mTextCarBrandGlass?.text = carInfo.carBrand
            } else {
                text_car_brand.text = ""
                mTextCarBrandGlass?.text = ""
            }
            if (!TextUtils.isEmpty(carInfo.carId)) {
                text_car_number.text = carInfo.carId
                mTextPlateNumberGlass?.text = carInfo.carId
            } else {
                text_car_number.text = ""
                mTextPlateNumberGlass?.text = ""
            }
            if (!TextUtils.isEmpty(carInfo.carType)) {
                text_car_type.text = carInfo.carType
                mImgCarTypeGlass?.text = carInfo.carType
            } else {
                text_car_type.visibility = View.GONE
                mImgCarTypeGlass?.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(carInfo.carColor)) {
                text_car_color.text = carInfo.carColor
                mTextCarColorGlass?.text = carInfo.carColor
            } else {
                mTextCarColorGlass?.text = ""
                text_car_color.text = ""
            }
        } else {
            setVehicleDefShow(CAR_STATUS_NOINFO)
            if (null != path) {
                val imgFile = File(path)
                if (imgFile.exists()) {
                    img_car.visibility = View.VISIBLE
                    Glide.with(context!!.applicationContext).load(File(path))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).into(img_car)
                    mImgCarGlass?.let {
                        Glide.with(context!!.applicationContext).load(File(path))
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(mImgCarGlass)
                    }
                } else {
                    val url: String = Util.getCarPhotoIpHead() + path
                    Glide.with(context!!.applicationContext).load(url)
                        .diskCacheStrategy(DiskCacheStrategy.NONE).into(img_car)
                    mImgCarGlass?.let {
                        Glide.with(context!!.applicationContext).load(url)
                            .diskCacheStrategy(DiskCacheStrategy.NONE).into(mImgCarGlass)
                    }
                }
            }
            text_car_number.text = plateNum
            mTextPlateNumberGlass?.text = plateNum
        }
    }

    private val isFmActive: Boolean
        get() {
            val am: AudioManager? =
                activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (am == null) {
                Log.w(TAG, "isFmActive: couldn't get AudioManager reference")
                return false
            }
            return am.isMusicActive
        }

    private fun playCarRegSound() {
        if (!mLoadMusicOk) {
            //AR110Log.i("music", "music not load complete");
            return
        }
        if (isFmActive && !AR110MainActivity.isPushing) {
            AR110Log.i("music", "is playing music")
            return
        }
        mSoundPool.play(mCarNoticeMusic, 1f, 1f, 1, 0, 1f)
    }

    private fun playNoticeSound() {
        if (!mLoadMusicOk) {
            //AR110Log.i("music", "music not load complete");
            return
        }
        if (isFmActive && !AR110MainActivity.isPushing) {
            AR110Log.i("music", "is playing music")
            return
        }
        mSoundPool.play(mNoticeMusicId, 1f, 1f, 1, 0, 1f)
    }

    private fun playShutterSound() {
        if (!mLoadMusicOk) {
            return
        }
        mSoundPool.play(mShutterMusic, 1f, 1f, 1, 0, 1f)
    }

    fun newPlateHandle(carData: ByteArray?, plateNum: String?, plateColor: String?) {
        lifecycleScope.launch {
            val platResult = mViewModel.newPlateHandle(
                carData,
                PREVIEW_WIDTH,
                PREVIEW_HEIGHT,
                plateNum,
                plateColor,
                mCopTask,
                mPatrolTask
            )
            platResult?.let {
                playCarRegSound()
                updateVehicleInfo(platResult.name, platResult.info, platResult.plateNum)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AR110Log.i(TAG, "onCreate")
        mCameraHandler = (activity as AR110MainActivity?)!!.mCameraHandler!!
        super.onCreate(savedInstanceState)
        mSoundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        mNoticeMusicId = mSoundPool.load(activity, R.raw.type, 1)
        mCarNoticeMusic = mSoundPool.load(activity, R.raw.caralarm, 1)
        mShutterMusic = mSoundPool.load(activity, R.raw.shutter, 1)
        mSoundPool.setOnLoadCompleteListener { _: SoundPool?, _: Int, _: Int ->
            mLoadMusicOk = true
        }
        mHandler = ClientHandler((activity as AR110MainActivity?)!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_jd_handle
    }

    override fun initData() {
        AR110Log.i(TAG, "initData")
        mFaceCmpIndex = 0
        mNeedCar = 1
        mRecordTotalTime = 0
        mRecordSingleTime = 0
        mCopTask = arguments!!.getParcelable("key_cop_task")
        mPatrolTask = arguments!!.getParcelable("key_patrol_task")
        AR110BaseService.instance!!.setCopTask(mCopTask, mPatrolTask)
        mFaceEngine.initFaceEngine(activity!!, LeiaBoxEngine.getInstance().settingManager().logsDir)
        mCopTask?.let {
            mCurrentCJZT = mCopTask!!.cjzt2
            MyMqttManager.instance!!.updateStatus(MyMqttManager.COP_STATUS, mCurrentCJZT)
        }
        mPatrolTask?.let {
            mCurrentCJZT = if (mPatrolTask!!.status == 0) {
                Util.CJZT_CJZ
            } else {
                Util.CJZT_YWC
            }
            MyMqttManager.instance!!.updateStatus(MyMqttManager.PATROL_TASK_STATUS, mCurrentCJZT)
        }
        if (Util.mNeedMultiScreen) {
            checkPermission()
            startSecondaryScreen()
        }
        mLPREngine.initEngine(context!!)
        initCoroutine = lifecycleScope.launch {
            mViewModel.fetchFaceList(mCopTask, mPatrolTask).flowOn(Dispatchers.IO).catch {
                AR110Log.e(TAG, "fetchFaceList onfail it=${it.message}")
            }.onCompletion {
                updateFaceCmpRes()
            }.collect { target ->
                if (target.mLabelCode == 3) {
                    mTotalWarningNum++
                } else if (target.mLabelCode == 4) {
                    mTotalFatalNum++
                }
                mPeopleList += target
            }
            //不要使用 withContext
            val fetchCarResult =
                async(Dispatchers.IO) { mViewModel.fetchCarRecord(mCopTask, mPatrolTask) }.await()
            if (fetchCarResult != null) {
                updateVehicleHistory(
                    fetchCarResult.path,
                    fetchCarResult.carInfo,
                    fetchCarResult.plateNum!!
                )
            } else {
                setVehicleDefShow(CAR_STATUS_EMPTY)
            }
            //不要使用 withContext
            val imgPath =
                async(Dispatchers.IO) { mViewModel.fetchPhotoRecord(mCopTask, mPatrolTask) }.await()
            imgPath?.let {
                Glide.with(context!!.applicationContext).load(File(imgPath)).into(img_photo_gather)
            }
        }

    }

    override fun initView(view: View) {
        VALID_CONFICENT_THRESHOOD = Util.getIntPref(
            Utils.getApp(),
            Util.KEY_THRESH, Util.DEF_FACE_THRESHOOD
        )
        if (VALID_CONFICENT_THRESHOOD < Util.MIN_FACE_THRESHOOD) {
            VALID_CONFICENT_THRESHOOD = Util.MIN_FACE_THRESHOOD
        }
        if (VALID_CONFICENT_THRESHOOD > Util.MAX_FACE_THRESHOOD) {
            VALID_CONFICENT_THRESHOOD = Util.MAX_FACE_THRESHOOD
        }
        mCopTaskSoundType = Util.getIntPref(
            Utils.getApp(),
            SoundSelConstraintLayout.KEY_COPTASK_SOUND_SET,
            SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL
        )
        mPatrolSoundType = Util.getIntPref(
            Utils.getApp(),
            SoundSelConstraintLayout.KEY_PATROL_SOUND_SET,
            SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT
        )
        if (null != mPatrolTask) {
            layout_patrol_info.visibility = View.VISIBLE
            text_patrol_number.text = mPatrolTask!!.number
            if (null != mPatrolTask!!.startTime) {
                var startTime = mPatrolTask!!.startTime.replace("T", " ")
                startTime = startTime.substring(0, "2021-01-01_12:00:09".length)
                mPatrolTask!!.startTime = startTime
                text_patrol_starttime.text = startTime
            }
            if (!TextUtils.isEmpty(mPatrolTask!!.endTime)) {
                if (mCurrentCJZT == CJZT_YWC) {
                    text_patrol_endtime.text = mPatrolTask!!.endTime
                    img_patrol_end_time.visibility = View.VISIBLE
                }
            }
        } else {
            layout_patrol_info.visibility = View.GONE
        }
        if (null != mCopTask) {
            layout_bjd_info.visibility = View.VISIBLE
            info_state_connect.visibility = View.VISIBLE
        } else if (mPatrolTask != null) {
            text_patrol_people_name.text = AR110BaseService.mUserInfo!!.name
            layout_bjd_info.visibility = View.GONE
            info_state_connect.visibility = View.GONE
        }
        mTagLayout = view.findViewById(R.id.layout_tag_show)
        if (mCopTask != null) {
            text_jdh.text = mCopTask!!.cjdbh2
        } else if (mPatrolTask != null) {
            text_jdh.text = mPatrolTask!!.number
        }
        group_people_info.visibility = View.INVISIBLE
        group_no_face.visibility = View.VISIBLE
        if (null != mCopTask) {
            text_bjr.text = mCopTask!!.bjrxm
            text_cjwz.text = mCopTask!!.afdd
            text_bjnr.text = mCopTask!!.bjnr
            text_bjsj.text = mCopTask!!.bjsj
        }
        if (mCameraHandler.isOpened) {
            text_task.isClickable = true
            mHandler.sendEmptyMessageDelayed(MSG_GALSSES_STATE_VISIBLE, 50)
        } else {
            mHandler.sendEmptyMessageDelayed(MSG_GALSSES_STATE_GONE, 50)
        }
        if (mNeedCar == 1) {
            img_car_switch.setImageResource(R.drawable.switch_off)
        } else {
            img_car_switch.setImageResource(R.drawable.switch_on)
        }
        when (mCurrentCJZT) {
            CJZT_YWC -> {
                text_task.visibility = View.INVISIBLE
                text_task.text = "已完成"
                text_task.background = null
                text_task.setTextColor(activity!!.getColor(R.color.white))
                text_task.isClickable = false
            }
            CJZT_WCJ -> {
                text_task.visibility = View.VISIBLE
                text_task.text = "立即出警"
                text_task.setBackgroundResource(R.drawable.chujing_bg)
                text_task.setTextColor(activity!!.getColor(R.color.white))
            }
            CJZT_CJZ -> {
                text_task.visibility = View.VISIBLE
                text_task.text = "完成处理"
                text_task.setBackgroundResource(R.drawable.wancheng_bg)
                text_task.setTextColor(activity!!.getColor(R.color.white))
            }
        }
        mFaceView = view.findViewById(R.id.face_view)
        setVehicleDefShow(CAR_STATUS_EMPTY)
        if (null != mCopTask) {
            text_title.text = "警单详情"
        }
        if (null != mPatrolTask) {
            text_title.text = "巡逻详情"
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        AR110Log.i(TAG, "setUserVisibleHint $isVisibleToUser")
        if (isVisibleToUser) {
            updateStatus()
        } else {
            MyMqttManager.instance!!.updateStatus(-1, mCurrentCJZT)
        }
    }

    override fun initListener() {
        layout_facecmp.setOnClickListener {
            NavigationHelper.instance.beginShowPeopleRecord(mCopTask, mPatrolTask)
            userVisibleHint = false
        }
        vehicle_recog_layout.setOnClickListener {
            NavigationHelper.instance.beginShowCarRecord(mCopTask, mPatrolTask)
            userVisibleHint = false
        }
        info_state_connect.setOnClickListener {
            //创建群聊
            lifecycleScope.launch {
                val gId = mViewModel.createGroup(mCopTask!!.cjdbh2)
                toChat(gId)
            }
        }
        layout_photo_collect.setOnClickListener {
            NavigationHelper.Companion.instance.beginShowPhotoRecord(mCopTask, mPatrolTask)
            userVisibleHint = false
        }
        text_enter_list.setOnClickListener {
            if (mCameraHandler.isRecording) {
                if (mHandler.hasMessages(MSG_AUDIO_RECORDING)) {
                    return@setOnClickListener
                }
            }
            NavigationHelper.instance.beginShowAudioRecord(mCopTask, mPatrolTask)
            userVisibleHint = false
        }
        img_enter_detail.setOnClickListener {
            if (mCameraHandler.isRecording) {
                if (mHandler.hasMessages(MSG_AUDIO_RECORDING)) {
                    return@setOnClickListener
                }
            }
            NavigationHelper.instance.beginShowAudioRecord(mCopTask, mPatrolTask)
            userVisibleHint = false
        }
        img_audio_record.setOnClickListener {
            if (!mCameraHandler.isOpened) {
                Util.showMessage("未接入眼镜")
                return@setOnClickListener
            }
            if (!mCameraHandler.isPreviewing) {
                Util.showMessage("未接入眼镜")
                return@setOnClickListener
            }
            if (mCameraHandler.isRecording && mHandler.hasMessages(MSG_UPDATE_RECORD_TIME)) {
                Util.showMessage("正在录像中,暂时无法录音")
                return@setOnClickListener
            }
            if (!mCameraHandler.isRecording) {
                if (null != mCopTask) {
                    beginRecordAudio(mCopTask!!.cjdbh2)
                } else if (mPatrolTask != null) {
                    beginRecordAudio(mPatrolTask!!.number)
                }
                text_record_audio_time.visibility = View.VISIBLE
                val audioRecordTime = String.format("%02d : %02d", 0, 0)
                text_record_audio_time.text = audioRecordTime
                img_audio_record.setImageResource(R.drawable.ic_icon_audio_open)
                mHandler.sendEmptyMessageDelayed(MSG_AUDIO_RECORDING, 1000)
            } else {
                mHandler.removeMessages(MSG_AUDIO_RECORDING)
                text_record_audio_time.visibility = View.GONE
                img_audio_record.setImageResource(R.drawable.ic_icon_audio_close)
                mCameraHandler.stopRecording()
                mAudioRecordTime = 0
            }
        }
        layout_back.setOnClickListener {
            if (mCopTask != null) {
                NavigationHelper.instance.backToJdList(false)
            } else if (mPatrolTask != null) {
                NavigationHelper.instance.backToPatrolList()
            }
        }
        img_car_switch.setOnClickListener {
            if (mNeedCar == 1) {
                mNeedCar = 2
                img_car_switch.setImageResource(R.drawable.switch_on)
            } else {
                mNeedCar = 1
                img_car_switch.setImageResource(R.drawable.switch_off)
            }
        }
        text_task.setOnThrottledClickListener {
            Log.d(TAG, "mTextTaskButton onClick $mCurrentCJZT")
            if (mCurrentCJZT == CJZT_WCJ) {
                if (!mCameraHandler.isOpened || !mCameraHandler.isPreviewing) {
                    Util.showMessage("相机没有打开请检查相机")
                }
                if (mCameraHandler.isRecording) {
                    mCameraHandler.stopRecording()
                    mAudioRecordTime = 0
                    mHandler.removeMessages(MSG_AUDIO_RECORDING)
                    img_audio_record.setImageResource(R.drawable.ic_icon_audio_close)
                }
                text_task.text = "完成处理"
                AR110BaseService.instance!!.setCopTask(mCopTask, mPatrolTask)
                text_task.setBackgroundResource(R.drawable.wancheng_bg)
                mCopTask?.let {
                    mCopTask!!.cjzt2 = Util.CJZT_CJZ
                    lifecycleScope.launch {
                        val result = mViewModel.updateState(mCopTask, null, CJZT_CJZ)
                        AR110Log.i(TAG, "update state accept result: $result")
                    }
                }
                mCurrentCJZT = CJZT_CJZ
                MyMqttManager.instance!!.updateStatus(
                    if (mCopTask != null) MyMqttManager.COP_STATUS else MyMqttManager.PATROL_TASK_STATUS,
                    mCurrentCJZT
                )

                law_enfore.isClickable = false
                if (Util.getAvailableSize() < 100) {
                    Util.showMessage("磁盘容量不够，无法录像!")
                    return@setOnThrottledClickListener
                }
                if (!mCameraHandler.isRecording) {
                    if (mCameraHandler.isOpened && mCameraHandler.isPreviewing) {
                        if (null != mCopTask) {
                            beginRecordVideo(mCopTask!!.cjdbh2)
                        } else if (null != mPatrolTask) {
                            beginRecordVideo(mPatrolTask!!.number)
                        }
                        mIconRecording?.visibility = View.VISIBLE
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RECORD_TIME, 10)
                    }
                }
            } else if (mCurrentCJZT == CJZT_CJZ) {
                showConfirmDlg()
            }
        }
        law_enfore.setOnClickListener {
            NavigationHelper.instance.beginVideoUpload(mCopTask, mPatrolTask)
            userVisibleHint = false
        }
        if (mCurrentCJZT == CJZT_CJZ) {
            law_enfore.isClickable = false
        }
    }

    override fun initObserver() {
        mViewModel.mFaceRecognizerLiveData.observe(this, Observer { result ->
            if (result.isSuccess) {
                val faceMap = result.data!!.faceInfoList
                faceMap.forEach {
                    val key = it.key
                    val value = it.value
                    val similarity = value[0].similarity
                    val criminal = value[0].labelName
                    var conf = 0.0f
                    conf = similarity.toFloat()
                    if (conf > 0 && conf <= 1.0f) {
                        conf *= 100f
                    }
                    if (VALID_CONFICENT_THRESHOOD < conf) {
                        conf = (conf.roundToInt()).toFloat()
                        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        val capTime = format.format(Date())
                        val baseInfo = TargetInfo(
                            value[0].name,
                            value[0].faceBase64,
                            value[0].cardId,
                            key.toLong(),
                            "$conf%",
                            value[0].localFaceBase64,
                            capTime,
                            criminal,
                            value[0].labelCode
                        )
                        var needContinue = true
                        val len: Int = mPeopleList.size
                        for (i in 0 until len) {
                            if ((value[0].cardId == mPeopleList[i].idcard)) {
                                val faceId = key.toLong()
                                if (faceId == mPeopleList[i].faceID) {
                                    needContinue = false
                                    break
                                }
                            }
                        }
                        mPeopleList += baseInfo
                        if (needContinue) {
                            if (mCopTask != null) {
                                if (mCopTaskSoundType == SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL) {
                                    playNoticeSound()
                                } else {
                                    if (baseInfo.mLabelCode >= 3) {
                                        playNoticeSound()
                                    }
                                }
                            }
                            if (mPatrolTask != null) {
                                if (mPatrolSoundType == SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL) {
                                    playNoticeSound()
                                } else {
                                    if (baseInfo.mLabelCode >= 3) {
                                        playNoticeSound()
                                    }
                                }
                            }
                            AR110Log.i(TAG, "face request result update UI")
                            val peo: TargetInfo = mPeopleList.last()
                            updateGlassShowInfo(GLASS_SHOW_PEO)
                            if (showSecondScreen()) {
                                mHandler.removeMessages(MSG_HIDE_FACECOMP)
                                mHandler.sendEmptyMessageDelayed(MSG_HIDE_FACECOMP, 5000)
                            }
                            var textColorPhone: Int =
                                ContextCompat.getColor(context!!, R.color.black)
                            var textColorGlass: Int =
                                ContextCompat.getColor(context!!, R.color.white)
                            when (peo.mLabelCode) {
                                0 -> {
                                    textColorPhone =
                                        ContextCompat.getColor(context!!, R.color.purple)
                                    textColorGlass =
                                        ContextCompat.getColor(context!!, R.color.purple)
                                }
                                1 -> {
                                    textColorPhone =
                                        ContextCompat.getColor(context!!, R.color.royalblue)
                                    textColorGlass =
                                        ContextCompat.getColor(context!!, R.color.royalblue)
                                }
                                2 -> {
                                    textColorPhone =
                                        ContextCompat.getColor(context!!, R.color.black)
                                    textColorGlass =
                                        ContextCompat.getColor(context!!, R.color.white)
                                }
                                3 -> {
                                    mTotalWarningNum++
                                    textColorPhone =
                                        ContextCompat.getColor(context!!, R.color.darkorange)
                                    textColorGlass =
                                        ContextCompat.getColor(context!!, R.color.yellow)
                                }
                                4 -> {
                                    mTotalFatalNum++
                                    textColorPhone = ContextCompat.getColor(context!!, R.color.red)
                                    textColorGlass = ContextCompat.getColor(context!!, R.color.red)
                                }
                            }
                            phone_criminal.setTextColor(textColorPhone)
                            mTextCrimeGlass?.setTextColor(textColorGlass)
                            phone_people_name.setTextColor(textColorPhone)
                            mTextNameGlass?.setTextColor(textColorGlass)
                            id_num_val.setTextColor(textColorPhone)
                            mTextIDCardGlass?.setTextColor(textColorGlass)
                            phone_criminal.text = peo.mCriminalType
                            mTextCrimeGlass?.text = peo.mCriminalType
                            text_item_index.text = (String.format("1 / %d", mPeopleList.size))
                            phone_people_name.text = peo.name
                            phone_sconf.text = peo.mConf
                            id_num_val.text = peo.idcard
                            mTextFatalPeoNum?.text = (mTotalFatalNum.toString() + "")
                            mTextWarningPeoNum?.text = (mTotalWarningNum.toString() + "")
                            val face: Bitmap = Base64Util.base64ToBitmap(peo.faceUrl)
                            val faceLib: Bitmap = Base64Util.base64ToBitmap(peo.libFaceUrl)
                            phone_face_lib.setImageBitmap(faceLib)
                            phone_face_crop.setImageBitmap(face)
                            mImgFaceLibGlass?.setImageBitmap(faceLib)
                            mImgFaceCropGlass?.setImageBitmap(face)
                            phone_capure_time.text = peo.capureTime
                            mTextNameGlass?.text = peo.name
                            mTextConfGlass?.text = peo.mConf
                            mTextIDCardGlass?.text = peo.idcard
                            group_people_info.visibility = View.VISIBLE
                            group_no_face.visibility = View.INVISIBLE
                            //更新重点人员显示
                            mTextFatalPeo?.let {
                                if (peo.mLabelCode >= 3) {
                                    if (mLastImportantPeo != null) {
                                        if (mLastImportantPeo!!.mLabelCode <= peo.mLabelCode) {
                                            mTextImportName?.let {
                                                mTextImportName?.setTextColor(textColorGlass)
                                                mTextImportID?.setTextColor(textColorGlass)
                                                mTextImportName?.text = peo.name
                                                mTextImportID?.text = peo.idcard
                                            }
                                            mLastImportantPeo = peo
                                        }
                                    } else {
                                        mTextImportName?.let {
                                            mTextImportName?.setTextColor(textColorGlass)
                                            mTextImportID?.setTextColor(textColorGlass)
                                            mTextImportName?.text = peo.name
                                            mTextImportID?.text = peo.idcard
                                        }
                                        mLastImportantPeo = peo
                                    }
                                }
                            }
                        }
                    } else {
                        AR110Log.i(TAG, "face result similarity : $conf")
                    }
                }
            }
        })
    }

    private fun startSecondaryScreen(): Boolean {
        displayManager = activity!!.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val presentationDisplays = displayManager.displays
        return if (presentationDisplays.size > 1) {
            var point = Point()
            presentationDisplays[1].getSize(point)
            AR110Log.e(
                TAG,
                "startSecondaryScreen presentationDisplays[1]: ${presentationDisplays[1]} point.x=${point.x}， point.y=${point.y}"
            )

            mSecondaryScreen = SecondaryScreen(
                activity!!.applicationContext,
                presentationDisplays[1],
                mCopTask,
                mPatrolTask
            )
            mSecondaryScreen?.setScreenSize1080(point.y == 1080)
            mSecondaryScreen?.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) //TYPE_SYSTEM_ALERT / TYPE_PHONE
            mSecondaryScreen?.setSecondScreenListener(this)

            true
        } else {
            ToastUtils.show("没有发现分屏设备！")
            false
        }
    }

    override fun slideLeft() {
        AR110Log.i(TAG, "slideLeft")
        if (mFaceCmpIndex > 0) {
            mFaceCmpIndex--
            updateFaceCmpRes()
        }
    }

    override fun slideRight() {
        AR110Log.i(TAG, "slideRight")
        if (mFaceCmpIndex < (mPeopleList.size - 1)) {
            mFaceCmpIndex++
            updateFaceCmpRes()
        }
    }

    override fun onBackPressed() {
//        return true
    }

    /**
     * Hileia来电
     */
    override fun onHileiaComingCallCallback() {
        activity?.runOnUiThread {
            mLayoutLive?.visibility = View.VISIBLE
            layout_commu_phone.visibility = View.VISIBLE
        }
    }

    /**
     * Hileia挂断
     */
    override fun onHileiaHangupCallback() {
        activity?.runOnUiThread {
            mLayoutLive?.visibility = View.GONE
            layout_commu_phone.visibility = View.GONE
            mIconRecording?.visibility = View.INVISIBLE
        }
    }

    override fun updateGpsLocation(loc: LocationData) {
        AR110Log.i(TAG, "JDHandleFragment::updateGpsLocation")
        if (mCameraHandler.isOpened && mCameraHandler.isRecording) {
            if (mCurrentRecordFile != null) {
                val vLoc = VideoLocationDiskCache.getInstance(Utils.getApp())
                    .getVideoLocFromDiskCache(mCurrentRecordFile)
                if (null != vLoc) {
                    if (TextUtils.isEmpty(vLoc.longitude) || TextUtils.isEmpty(vLoc.latitude)) {
                        AR110Log.i(TAG, "get gps loc mCurrentRecordFile=$mCurrentRecordFile")
                        vLoc.latitude = loc.latitude
                        vLoc.longitude = loc.longitude
                        vLoc.altitude = loc.altitude
                        VideoLocationDiskCache.getInstance(Utils.getApp())
                            .removeVideoLoc(mCurrentRecordFile)
                        VideoLocationDiskCache.getInstance(Utils.getApp())
                            .addVideoLocation(vLoc)
                    }
                } else {
                    val videoLoc = MediaLocationData()
                    videoLoc.fileName = mCurrentRecordFile
                    videoLoc.record_time = System.currentTimeMillis().toString()
                    mCurrentRecordFile = videoLoc.fileName
                    videoLoc.altitude = loc.altitude
                    videoLoc.latitude = loc.latitude
                    videoLoc.longitude = loc.longitude
                    VideoLocationDiskCache.getInstance(Utils.getApp()).addVideoLocation(videoLoc)
                }
            }
        }
    }

    private inner class ClientHandler(mainActivity: AR110MainActivity) : Handler() {
        private val mWeakActivity: WeakReference<AR110MainActivity>?
        override fun handleMessage(msg: Message) {
            if (mWeakActivity == null) return
            when (msg.what) {
                MSG_GALSSES_STATE_VISIBLE -> {
                    iv_glasses.setImageResource(R.drawable.icon_into_glasses)
                    text_task.isClickable = mCurrentCJZT != CJZT_YWC
                    if (mCurrentCJZT == CJZT_CJZ) {
                        if (!mCameraHandler.isRecording) {
                            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RECORD_TIME, 200)
                        }
                    }
                    if (Util.mNeedMultiScreen && mSecondaryScreen == null) {
                        if (startSecondaryScreen()) {
                            mSecondaryScreen?.show()
                        }
                    }
                }
                MSG_GALSSES_STATE_GONE -> {
                    AR110Log.i(TAG, "MSG_GALSSES_STATE_GONE")
                    iv_glasses.setImageResource(R.drawable.icon_into_glasses_invalid)
                    mRecordTotalTime = 0
                    mAudioRecordTime = 0
                }
                MSG_HIDE_FACECOMP -> {
                }
                MSG_UPDATE_RECORD_TIME -> {
                    if (mCameraHandler.isRecording) {
                        mRecordTotalTime++
                        mRecordSingleTime++
                        val hour = mRecordTotalTime / 3600
                        val minute = (mRecordTotalTime - hour * 3600) / 60
                        val second = mRecordTotalTime % 60
                        val strRec = String.format("%02d: %02d: %02d", hour, minute, second)
                        if (mGroupGlassRecordTime?.visibility != View.VISIBLE) {
                            mGroupGlassRecordTime?.visibility = View.VISIBLE
                        }
                        mTextRecordTimeGlass?.text = strRec
                        text_record_time.text = strRec
                        if (mRecordSingleTime >= RECORD_TIME_LIMIT) {
                            mRecordSingleTime = 0
                            if (mWeakActivity.get()!!.mCameraHandler!!.isRecording) {
                                AR110Log.i(
                                    TAG,
                                    "mRecordSingleTime $mRecordSingleTime >= RECORD_TIME_LIMIT $RECORD_TIME_LIMIT stopRecording "
                                )
                                mWeakActivity.get()!!.mCameraHandler!!.stopRecording()
                            }
                        }
                    } else {
                        if (Util.getAvailableSize() < 100) {
                            Util.showMessage("磁盘容量不够，无法录像!")
                            return
                        }

                        mCopTask?.let {
                            beginRecordVideo(mCopTask!!.cjdbh2)
                        }
                        mPatrolTask?.let {
                            beginRecordVideo(mPatrolTask!!.number)
                        }
                        AR110Log.i(TAG, "begin record")
                    }
                    if (mIconRecording?.visibility != View.VISIBLE) {
                        mIconRecording?.visibility = View.VISIBLE
                    }
                    removeMessages(MSG_UPDATE_RECORD_TIME)
                    sendEmptyMessageDelayed(MSG_UPDATE_RECORD_TIME, 1000)
                }
                MSG_AUDIO_RECORDING -> {
                    if (mCameraHandler.isRecording) {
                        mAudioRecordTime++
                        val minute = mAudioRecordTime / 60
                        val second = mAudioRecordTime % 60
                        val audioRecordTime = String.format("%02d : %02d", minute, second)
                        text_record_audio_time.text = audioRecordTime
                        sendEmptyMessageDelayed(MSG_AUDIO_RECORDING, 1000)
                    } else {
                        img_audio_record.setImageResource(R.drawable.ic_icon_audio_close)
                        text_record_audio_time.visibility = View.GONE
                        mAudioRecordTime = 0
                    }
                }
                MSG_UPDATE_TAGS -> {
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TAGS, 100)
                }
                else -> {
                }
            }
        }

        init {
            mWeakActivity = WeakReference(mainActivity)
        }
    }

    private var mLastFaceDetectionInfo: Array<FaceRecognitionInfo>? = null
    private val mFaceList = ArrayList<FaceRecognitionInfo>()
    private var mLastChangeTime: Long = 0
    override fun onFrameAvailable(data: ByteArray?, width: Int, height: Int) {
        frameCount++
        if (mCurrentCJZT != CJZT_CJZ) {
            return
        }
        val faceDetectionInfos = mFaceEngine.run(data, width, height, 21, true)
        if (faceDetectionInfos == null) {
            mLastFaceDetectionInfo = null
            mLastChangeTime = System.currentTimeMillis()
            mFaceView.clearFaceRect()
            return
        }
        if (faceDetectionInfos.isNotEmpty()) {
            AR110Log.i("frame", "face_number=" + faceDetectionInfos.size)
            mFaceList.clear()
            mFrameCountAfterFaceFound++ // reset to zero
            faceDetectionInfos.forEach {
                mFaceList.add(it)
            }
            mFaceView.offerRects(mFaceList)
            var mFaceChanged = false
            if (null == mLastFaceDetectionInfo) {
                mFaceChanged = true
            } else {
                if (mLastFaceDetectionInfo!!.size != faceDetectionInfos.size) {
                    mFaceChanged = true
                } else {
                    val len = faceDetectionInfos.size
                    val len1 = mLastFaceDetectionInfo!!.size
                    for (i in 0 until len) {
                        var find = false
                        for (j in 0 until len1) {
                            if (faceDetectionInfos[i].face_id == mLastFaceDetectionInfo!![j].face_id) {
                                find = true
                                break
                            }
                        }
                        if (!find) {
                            mFaceChanged = true
                            break
                        }
                    }
                }
            }
            AR110Log.i("frame", "mFaceChanged=$mFaceChanged")
            if (mFaceChanged) {
                mLastFaceDetectionInfo = faceDetectionInfos
                mLastChangeTime = System.currentTimeMillis()
                return
            }
            val timeLast = System.currentTimeMillis() - mLastChangeTime
            if (timeLast in 301..599) {
                if (mFrameCountAfterFaceFound % 5 == 3) {
                    mViewModel.faceRequest(
                        mCopTask,
                        mPatrolTask,
                        data!!.clone(),
                        width,
                        height,
                        mFaceList
                    )
                    AR110Log.i("frame", "requestFaceDetectionInfo !")
                }
            }
        } else {
            mFaceView.clearFaceRect()
            mLastFaceDetectionInfo = null
            mLastChangeTime = System.currentTimeMillis()
            mFrameCountAfterFaceFound = 0
        }
    }

    override fun onResume() {
        super.onResume()
        AR110Log.i(TAG, "onResume")
    }

    override fun onStart() {
        super.onStart()
        Log.d("initview", "onStart init second screen")
        AR110Log.i(TAG, "onStart")
        mSecondaryScreen?.let {
            displayManager = activity!!.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val presentationDisplays = displayManager.displays
            if (presentationDisplays.size > 1) {
                try {
                    mSecondaryScreen?.show()
                } catch (e: Exception) {
                    ToastUtils.show("请插入AR眼镜状态异常，请重新插入眼镜！")
                    mSecondaryScreen = null
                }
            } else {
                ToastUtils.show("请插入AR眼镜状态异常，请重新插入眼镜！")
                mSecondaryScreen?.dismiss()
                mSecondaryScreen = null
            }
        }
        (activity as AR110MainActivity?)!!.setCameraFrameCallback(this)
        if (AR110MainActivity.isPushing) {
            mLayoutLive?.visibility = View.VISIBLE
            layout_commu_phone.visibility = View.VISIBLE
        }
        if (mCurrentCJZT == Util.CJZT_YWC) {
            text_task.visibility = View.INVISIBLE
        }
        if (isFirstStart) {
            isFirstStart = false
        } else {
            updateStatus()
        }
    }

    override fun onPause() {
        AR110Log.i(TAG, "onPause")
        super.onPause()
    }

    fun updateStatus() {
        if (mCopTask != null && userVisibleHint) {
            MyMqttManager.instance!!.updateStatus(MyMqttManager.COP_STATUS, mCurrentCJZT)
        } else if (mPatrolTask != null && userVisibleHint) {
            MyMqttManager.instance!!.updateStatus(MyMqttManager.PATROL_TASK_STATUS, mCurrentCJZT)
        } else {
            MyMqttManager.instance!!.updateStatus(-1, mCurrentCJZT)
        }
    }

    override fun onStop() {
        AR110Log.i(TAG, "onStop")
        (activity as AR110MainActivity?)!!.setCameraFrameCallback(null)
        law_enfore.isClickable = true
        MyMqttManager.instance!!.updateStatus(-1, mCurrentCJZT)
        mGroupGlassRecordTime?.visibility = View.INVISIBLE
        mTextRecordTimeGlass?.text = ""
        text_record_time.text = ""
        AR110BaseService.instance!!.setCopTask(null, null)
        mSecondaryScreen?.hide()
        if (mCurrentCJZT == CJZT_CJZ) {
            mRecordTotalTime = 0
            mRecordSingleTime = 0
        }
        mHandler.removeCallbacksAndMessages(null)
        AR110Log.i(TAG, "onStop ok")
        if (mCameraHandler.isRecording) {
            mCameraHandler.stopRecording()
            mAudioRecordTime = 0
            img_audio_record.setImageResource(R.drawable.ic_icon_audio_close)
            mIconRecording?.visibility = View.INVISIBLE
        }
        super.onStop()
    }

    override fun onDestroyView() {
        if (!initCoroutine.isCompleted) {
            initCoroutine.cancel()
        }
        AR110Log.i(TAG, "mSecondaryScreen dismiss")
        mSecondaryScreen?.setSecondScreenListener(null)
        mSecondaryScreen?.dismiss()
        mSecondaryScreen = null
        isUseSecondScreen = false
        super.onDestroyView()
    }

    override fun onDestroy() {
        AR110Log.i(TAG, "onDestroy")
        mHandler.removeCallbacksAndMessages(null)
        mSoundPool.unload(mNoticeMusicId)
        mSoundPool.unload(mCarNoticeMusic)
        mSoundPool.unload(mShutterMusic)
        mSoundPool.release()
        AR110BaseService.instance!!.setCopTask(null, null)
        AR110Log.i(TAG, "onDestroy ok")
        mFaceEngine.releaseFaceEngine()
        mLPREngine.release()
        super.onDestroy()
    }

    fun glassConnected() {
        mHandler.sendEmptyMessage(MSG_GALSSES_STATE_VISIBLE)
    }

    fun glassDisConnected() {
        mSecondaryScreen?.let {
            mSecondaryScreen?.dismiss()
            mSecondaryScreen = null
        }
        mHandler.sendEmptyMessage(MSG_GALSSES_STATE_GONE)
    }

    private fun updateFaceCmpRes() {
        if (mPeopleList.isEmpty() || mFaceCmpIndex >= mPeopleList.size) {
            return
        }
        val peo = mPeopleList[mFaceCmpIndex]
        if (group_people_info.visibility != View.VISIBLE) {
            group_people_info.visibility = View.VISIBLE
        }
        group_no_face.visibility = View.INVISIBLE
        var face: Bitmap? = null
        var faceLib: Bitmap? = null
        try {
            if (peo.faceUrl != null && peo.faceUrl.length > 512) {
                face = Base64Util.base64ToBitmap(peo.faceUrl)
                faceLib = Base64Util.base64ToBitmap(peo.libFaceUrl)
            }
        } catch (e: IllegalArgumentException) {
        }
        if (showSecondScreen()) {
            mLayoutGlassPeoResult?.visibility = View.VISIBLE
            mLayoutGlassBjd?.visibility = View.INVISIBLE
            mHandler.removeMessages(MSG_HIDE_FACECOMP)
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_FACECOMP, 5000)
        }
        phone_criminal.text = peo.mCriminalType
        text_item_index.text = String.format("%d / %d", mFaceCmpIndex + 1, mPeopleList.size)
        phone_people_name.text = peo.name
        phone_sconf.text = peo.mConf
        id_num_val.text = peo.idcard
        if ((face != null) && (face.width > 0) && (faceLib != null) && (faceLib.width > 0)) {
            phone_face_lib.setImageBitmap(faceLib)
            phone_face_crop.setImageBitmap(face)
        } else {
            if (peo.libFaceUrl != null && peo.libFaceUrl.length < 512) {
                Glide.with(context!!.applicationContext).load(peo.libFaceUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into(phone_face_lib)
            }
            if (peo.faceUrl != null && peo.faceUrl.length < 512) {
                Glide.with(context!!.applicationContext).load(peo.faceUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into(phone_face_crop)
            }
        }
        phone_capure_time.text = peo.capureTime
        updateGlassShowInfo(GLASS_SHOW_BJD)
    }

    private fun beginRecordVideo(cjdh: String) {
        val mAppDataDir = Util.getVideoRootDir(cjdh)
        val folder = File(mAppDataDir)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val fileTime = Util.getRecordFileName()
        val path = "$mAppDataDir/$fileTime.mp4"
        AR110Log.i(
            TAG,
            "mCameraHandler.isOpened ${mCameraHandler.isOpened} mCameraHandler.isPreviewing${mCameraHandler.isPreviewing}"
        )
        if (mCameraHandler.isOpened && mCameraHandler.isPreviewing) {
            mCameraHandler.startRecording(path)
        }
        val videoLoc = MediaLocationData()
        videoLoc.fileName = "$fileTime.mp4"
        videoLoc.record_time = System.currentTimeMillis().toString()
        mCurrentRecordFile = videoLoc.fileName
        val mLocData = AR110BaseService.instance!!.currentLoc
        if (mLocData != null) {
            videoLoc.altitude = mLocData.altitude
            videoLoc.latitude = mLocData.latitude
            videoLoc.longitude = mLocData.longitude
        } else {
            AR110Log.i(TAG, "beginRecordVideo no location get !!!")
        }
        VideoLocationDiskCache.getInstance(Utils.getApp()).addVideoLocation(videoLoc)
    }

    private fun beginRecordAudio(cjdh: String) {
        val mAppDataDir = Util.getAudioRootDir(cjdh)
        val folder = File(mAppDataDir)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val fileTime = Util.getRecordFileName()
        val path = "$mAppDataDir$fileTime.mp3"
        if (mCameraHandler.isOpened && mCameraHandler.isPreviewing) {
            mCameraHandler.startAudioRecording(path)
        }
        val videoLoc = MediaLocationData()
        videoLoc.fileName = "$fileTime.mp3"
        videoLoc.record_time = System.currentTimeMillis().toString()
        mCurrentRecordFile = videoLoc.fileName
        val mLocData = AR110BaseService.instance!!.currentLoc
        if (mLocData != null) {
            videoLoc.altitude = mLocData.altitude
            videoLoc.latitude = mLocData.latitude
            videoLoc.longitude = mLocData.longitude
        } else {
            AR110Log.i(TAG, "beginRecordVideo no location get !!!")
        }
        AudioLocationDiskCache.getInstance(Utils.getApp()).addAudioLocation(videoLoc)
    }

    private fun initViewFromSecondScreen(): Boolean {
        if (null == mSecondaryScreen) {
            return false
        }
        val hashMap = mSecondaryScreen!!.viewMapOfSecondScreen
        mTagLayout = hashMap[R.id.layout_tag_show] as ConstraintLayout?
        mImgFaceLibGlass = hashMap[R.id.glass_face_lib] as ImageView?
        mImgFaceCropGlass = hashMap[R.id.glass_face_crop] as ImageView?
        mTextNameGlass = hashMap[R.id.glass_people_name] as TextView?
        mTextConfGlass = hashMap[R.id.glass_conf] as TextView?
        mTextCrimeGlass = hashMap[R.id.glass_crime_type] as TextView?
        mTextIDCardGlass = hashMap[R.id.glass_id_name] as TextView?
        mIconRecording = hashMap[R.id.icon_recording] as ImageView?
        mFaceView = (hashMap[R.id.face_view] as MySurfaceView?)!!
        mLayoutGlassContent = hashMap[R.id.layout_glass_content_show] as ConstraintLayout?
        mLayoutGlassPeoResult = hashMap[R.id.people_result_glass] as ConstraintLayout?
        mLayoutGlassBjd = hashMap[R.id.glass_bjd] as ConstraintLayout?
        mLayoutGlassCar = hashMap[R.id.layout_car_content] as ConstraintLayout?
        mImgCarGlass = hashMap[R.id.img_car_glass] as ImageView?
        mTextPlateNumberGlass = hashMap[R.id.text_platenum_glass] as TextView?
        mTextCarBrandGlass = hashMap[R.id.text_car_brand_glass] as TextView?
        mImgCarTypeGlass = hashMap[R.id.text_car_type_glass] as TextView?
        mTextCarColorGlass = hashMap[R.id.text_car_color_glass] as TextView?
        mTextRecordTimeGlass = hashMap[R.id.glass_text_recordtime] as TextView?
        mGroupGlassRecordTime = hashMap[R.id.group_glass_recordtime] as Group?
        mLayoutLive = hashMap[R.id.layout_zhzx] as ConstraintLayout?
        mImgPicShowGlass = hashMap[R.id.img_pic_show_glass] as ImageView?
        mSecondTextLegalName = hashMap[R.id.text_legal_name] as TextView?
        mSecondTextDriverID = hashMap[R.id.text_driver_idcard] as TextView?
        mTextImportName = hashMap[R.id.glass_important_peoname] as TextView?
        mTextImportID = hashMap[R.id.glass_important_peoidcard] as TextView?
        mTextFatalPeo = hashMap[R.id.text_fatal_peo] as TextView?
        mTextFatalPeoNum = hashMap[R.id.text_fatal_peonum] as TextView?
        mTextWarningPeoNum = hashMap[R.id.text_warning_peonum] as TextView?
        mTextBjrNote = hashMap[R.id.glass_text_bjr_note] as TextView?
        activity?.runOnUiThread {
            if (null != mCopTask) {
                mTextBjrNote?.text = "报警人"
            } else if (null != mPatrolTask) {
                mTextBjrNote?.text = "巡逻人"
            }
        }
        return true
    }

    private fun showConfirmDlg() {
        val builder = AlertDialog.Builder(context)
        val view = View.inflate(activity, R.layout.layout_mydialog, null)
        builder.setView(view)
        mDialog = builder.create()
        mDialog.show()
        mDialog.setCancelable(false)
        val width = resources.getDimension(R.dimen.dp320).toInt()
        val height = resources.getDimension(R.dimen.dp166).toInt()
        mDialog.window!!.setLayout(width, height)
        mCopTask?.let {
            view.findViewById<TextView>(R.id.text_task_title).setText(R.string.complete_cop_task)
        }
        mPatrolTask?.let {
            view.findViewById<TextView>(R.id.text_task_title).setText(R.string.complete_patrol_task)
        }
        view.findViewById<TextView>(R.id.text_cancel).setOnClickListener { mDialog.dismiss() }
        view.findViewById<TextView>(R.id.text_ok).setOnClickListener {
            mDialog.dismiss()
            if (mCameraHandler.isRecording) {
                Log.d(TAG, "stop recording")
                mCameraHandler.stopRecording()
                mAudioRecordTime = 0
                Log.d(TAG, "stop recording ok")
            }
            mIconRecording?.visibility = View.INVISIBLE
            text_task.text = "已完成"
            text_task.background = null
            text_task.setTextColor(activity!!.getColor(R.color.white))
            mHandler.removeMessages(MSG_UPDATE_RECORD_TIME)
            law_enfore.isClickable = true
            mCopTask?.let {
                mCopTask!!.cjzt2 = Util.CJZT_YWC
            }
            mPatrolTask?.let {
                mPatrolTask!!.endTime = Util.formatDatePatrol()
                text_patrol_endtime.text = mPatrolTask!!.endTime
                text_patrol_endtime.visibility = View.VISIBLE
                img_patrol_end_time.visibility = View.VISIBLE
                mPatrolTask!!.status = 1
            }
            mCurrentCJZT = CJZT_YWC
            MyMqttManager.instance!!.updateStatus(-1, mCurrentCJZT)
            AR110BaseService.instance!!.setCopTask(null, null)

            lifecycleScope.launch {
                val result = mViewModel.updateState(mCopTask, mPatrolTask, CJZT_YWC)
                AR110Log.i(TAG, "update state complete result: $result")
                if (result && !AR110MainActivity.isPushing) {
                    mCopTask?.let {
                        AR110BaseService.instance!!.sendVideouploadTask(mCopTask)
                    }
                    mPatrolTask?.let {
                        AR110BaseService.instance!!.sendPatrolVideouploadTask(mPatrolTask)
                    }
                }
            }
        }
    }

    /**
     * 跳转到根据警单创建的聊天界面
     *
     * @param groupID 根据警单创建的群组ID
     */
    private fun toChat(groupID: String?) {
        //通过groupID进入聊天界面
        AR110Log.i(TAG, "toChat: %s", groupID)
        val bundle = Bundle()
        bundle.putString(ConstantApp.CHAT_RC_ID_KEY, groupID)
        bundle.putInt(ConstantApp.CHAT_TYPE_KEY, Enums.RecentContactType.GROUP_TYPE_VALUE)
        NavigationHelper.instance.beginChat(bundle, null)
        userVisibleHint = false
    }

    companion object {
        private const val MSG_HIDE_FACECOMP = 299
        private const val MSG_UPDATE_RECORD_TIME = MSG_HIDE_FACECOMP - 1
        private const val MSG_GALSSES_STATE_VISIBLE = MSG_HIDE_FACECOMP - 2
        private const val MSG_GALSSES_STATE_GONE = MSG_HIDE_FACECOMP - 3
        private const val MSG_UPDATE_TAGS = MSG_HIDE_FACECOMP - 5
        private const val MSG_AUDIO_RECORDING = MSG_HIDE_FACECOMP - 7
        private var VALID_CONFICENT_THRESHOOD = Util.DEF_FACE_THRESHOOD
        private const val RECORD_TIME_LIMIT = 10 * 60
        private const val PREVIEW_WIDTH = 1280
        private const val PREVIEW_HEIGHT = 720
        private const val CJZT_WCJ = Util.CJZT_WCJ
        private const val CJZT_CJZ = Util.CJZT_CJZ
        private const val CJZT_YWC = Util.CJZT_YWC
        private const val CAR_STATUS_EMPTY = 0
        private const val CAR_STATUS_NOINFO = 1
        private const val CAR_STATUS_HASINFO = 2
        private const val GLASS_SHOW_BJD = 1
        private const val GLASS_SHOW_PEO = 2
        private const val GLASS_SHOW_CAR = 3
        private const val GLASS_SHOW_TAG = 4
    }
}
