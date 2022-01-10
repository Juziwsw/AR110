package com.hiar.ar110.mutiscreen

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.data.TargetInfo
import com.hiar.ar110.data.people.FaceCompareBaseInfo
import com.hiar.ar110.extension.invisible
import com.hiar.ar110.extension.visible
import com.hiar.ar110.mutiscreen.SecondaryScreen.OnSecondScreenCallback
import com.hiar.ar110.util.Base64Util
import com.hiar.ar110.util.Util
import com.hiar.ar110.widget.MySurfaceView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Author:wilson.chen
 * date：6/29/21
 * desc：
 */
public class PopulationSreenHelper(val context: Activity, val mListener: OnSecondScreenCallback? = null) : LifecycleObserver {
    lateinit var mFaceView: MySurfaceView

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
    //眼镜端布局 end

    private var mTotalFatalNum: Int = 0
    private var mTotalWarningNum: Int = 0
    private var isUseSecondScreen = false
    private var mLastImportantPeo: TargetInfo? = null
    var mSecondaryScreen: SecondaryScreen? = null

    private val faceThresh by lazy {
        Util.getIntPref(Utils.getApp(),
                Util.KEY_THRESH, Util.DEF_FACE_THRESHOOD)
    }

    init {

    }

    fun startSecondaryScreen(): Boolean {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val presentationDisplays = displayManager.displays
        return if (presentationDisplays.size > 1) {
            createSecondScreen(presentationDisplays[1])
            true
        } else {
            Util.showMessage("没有发现分屏设备！")
            false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (!show()) {
            mSecondaryScreen?.let {
                Util.showMessage("请插入AR眼镜状态异常，请重新插入眼镜！")
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        hide()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public fun onDestroy() {
        mSecondaryScreen?.setSecondScreenListener(null)
        mSecondaryScreen?.dismiss()
        mSecondaryScreen = null
        isUseSecondScreen = false
    }

    private fun showSecondScreen(): Boolean {
        return (AR110MainActivity.mIsInPackage || isUseSecondScreen)
    }

    private fun createSecondScreen(display: Display) {
        mSecondaryScreen = SecondaryScreen(context.applicationContext, display, null, null)
        mSecondaryScreen?.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) //TYPE_SYSTEM_ALERT / TYPE_PHONE
        mSecondaryScreen?.setSecondScreenListener {
            initViewFromSecondScreen()
            isUseSecondScreen = true
            updateGlassShowInfo()
            mListener?.onSecondScreenStart()
        }
    }

    private fun updateGlassShowInfo() {
        if (!showSecondScreen()) {
            return
        }
        mSecondTextDriverID?.text = ""
        mSecondTextLegalName?.text = ""
        mLayoutGlassContent?.visible()
        if (mLayoutGlassPeoResult?.visibility != View.VISIBLE) {
            mLayoutGlassBjd?.invisible()
            mLayoutGlassCar?.invisible()
            mLayoutGlassPeoResult?.visible()
        }
    }

    private fun initViewFromSecondScreen(): Boolean {
        val hashMap = mSecondaryScreen?.viewMapOfSecondScreen ?: return false
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

        mTextBjrNote?.text = "核查人"
        return true
    }

    fun updateView(list: MutableList<FaceCompareBaseInfo>, mPeopleList: MutableList<MutableList<FaceCompareBaseInfo>>) {
        if (list.isNotEmpty()) {
            val info = list[0]
            val similarity = info.similarity ?: return
            val criminal = info.labelName
            var conf = 0.0f
            conf = similarity.toFloat()
            if (conf > 0 && conf <= 1.0f) {
                conf *= 100f
            }
            if (faceThresh < conf) {
                conf = (conf.roundToInt()).toFloat()
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val capTime = format.format(Date())
                val baseInfo = TargetInfo(info.faceName, info.faceBase64, info.cardId, 0,
                        "$conf%", info.localFaceBase64, capTime, criminal, info.labelCode)
                var needContinue = true
                val len: Int = mPeopleList.size
                if (mLastImportantPeo != null) {
                    for (i in 0 until len) {
                        if ((info.cardId == mPeopleList[i][0].cardId)) {
                            if (baseInfo.faceUrl == mLastImportantPeo?.faceUrl) {
                                // 同一个人，同一张图片不更新视图
                                needContinue = false
                                break
                            }
                        }
                    }
                }
                if (needContinue) {
                    updateInfo(baseInfo)
                }
                mLastImportantPeo = baseInfo
            }
        }
    }


    private fun updateInfo(peo: TargetInfo) {
        var textColorPhone: Int = ContextCompat.getColor(context, R.color.black)
        var textColorGlass: Int = ContextCompat.getColor(context, R.color.white)
        when (peo.mLabelCode) {
            0 -> {
                textColorPhone = ContextCompat.getColor(context, R.color.purple)
                textColorGlass = ContextCompat.getColor(context, R.color.purple)
            }
            1 -> {
                textColorPhone = ContextCompat.getColor(context, R.color.royalblue)
                textColorGlass = ContextCompat.getColor(context, R.color.royalblue)
            }
            2 -> {
                textColorPhone = ContextCompat.getColor(context, R.color.black)
                textColorGlass = ContextCompat.getColor(context, R.color.white)
            }
            3 -> {
                mTotalWarningNum++
                textColorPhone = ContextCompat.getColor(context, R.color.darkorange)
                textColorGlass = ContextCompat.getColor(context, R.color.yellow)
            }
            4 -> {
                mTotalFatalNum++
                textColorPhone = ContextCompat.getColor(context, R.color.red)
                textColorGlass = ContextCompat.getColor(context, R.color.red)
            }
        }
        mTextCrimeGlass?.setTextColor(textColorGlass)
        mTextNameGlass?.setTextColor(textColorGlass)
        mTextIDCardGlass?.setTextColor(textColorGlass)
        mTextCrimeGlass?.text = peo.mCriminalType
        mTextFatalPeoNum?.text = (mTotalFatalNum.toString() + "")
        mTextWarningPeoNum?.text = (mTotalWarningNum.toString() + "")
        val face: Bitmap = Base64Util.base64ToBitmap(peo.faceUrl)
        val faceLib: Bitmap = Base64Util.base64ToBitmap(peo.libFaceUrl)
        mImgFaceLibGlass?.setImageBitmap(faceLib)
        mImgFaceCropGlass?.setImageBitmap(face)
        mTextNameGlass?.text = peo.name
        mTextConfGlass?.text = peo.mConf
        mTextIDCardGlass?.text = peo.idcard

        //更新重点人员显示
        mTextFatalPeo?.let {
            if (peo.mLabelCode >= 3) {
                if (mLastImportantPeo != null) {
                    mLastImportantPeo?.let {
                        if (it.mLabelCode <= peo.mLabelCode) {
                            mTextImportName?.let {
                                mTextImportName?.setTextColor(textColorGlass)
                                mTextImportID?.setTextColor(textColorGlass)
                                mTextImportName?.text = peo.name
                                mTextImportID?.text = peo.idcard
                            }
                        }
                    }
                } else {
                    mTextImportName?.let {
                        mTextImportName?.setTextColor(textColorGlass)
                        mTextImportID?.setTextColor(textColorGlass)
                        mTextImportName?.text = peo.name
                        mTextImportID?.text = peo.idcard
                    }
                }
            }
        }
    }

    fun show(): Boolean {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val presentationDisplays = displayManager.displays
        if (presentationDisplays.size > 1) {
            if (mSecondaryScreen?.display == presentationDisplays[1]) {
                mSecondaryScreen?.show()
            } else {
                createSecondScreen(presentationDisplays[1])
            }
            return true
        }
        return false
    }

    fun dismiss() {
        mSecondaryScreen?.dismiss()
        mSecondaryScreen = null
    }

    fun hide() {
        mSecondaryScreen?.hide()
    }
}