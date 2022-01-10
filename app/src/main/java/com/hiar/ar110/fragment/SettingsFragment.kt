package com.hiar.ar110.fragment

import android.app.Activity
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.R
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.config.ModuleConfig.isCopTaskEnable
import com.hiar.ar110.config.ModuleConfig.isPatrolTaskEnable
import com.hiar.ar110.config.ModuleConfig.isPopulationTaskEnable
import com.hiar.ar110.data.LocationData
import com.hiar.ar110.data.UserInfoData
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.EventLiveBus.commtEvent
import com.hiar.ar110.extension.visible
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.SettingsViewModel
import com.hiar.ar110.widget.CacheFileSelLayout.CacheType
import com.hiar.ar110.widget.CacheFileSelLayout.Companion.fromValue
import com.hiar.ar110.widget.CacheFileSelLayout.OnCacheFileSelectListener
import com.hiar.ar110.widget.ProgressDialog
import com.hiar.ar110.widget.SoundSelConstraintLayout
import com.hiar.ar110.widget.SoundSelConstraintLayout.OnSoundTypeChangeListener
import com.hiar.mybaselib.utils.AR110Log
import com.hileia.common.enginer.LeiaBoxEngine
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * A simple [SettingsFragment] subclass.
 * create an instance of this fragment.
 */
class SettingsFragment : BaseFragment(), OnSoundTypeChangeListener {
    private var mValidThresh = Util.DEF_FACE_THRESHOOD
    private var mLPRThresh = Util.DEF_LPR_THRESHOOD

    private var mCoptaskSoundType = SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL
    private var mPatrolSoundType = SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT
    private var mMobileSoundType = SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL
    private var mCacheType = CacheType.VIDEO_CACHE_TYPE_DEF

    private fun updateUserInfo(userInfo: UserInfoData?) {
        userInfo?.run {
            text_account_value.text = account
            text_name_value.text = name
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_settings
    }

    private val mViewModel by lazy {
        getViewModel(SettingsViewModel::class.java)
    }

    override fun initData() {
        mValidThresh = Util.getIntPref(Utils.getApp(), Util.KEY_THRESH,
                Util.DEF_FACE_THRESHOOD)
        mLPRThresh = Util.getIntPref(Utils.getApp(), Util.KEY_LPR_THRESH,
            Util.DEF_LPR_THRESHOOD)
        mCoptaskSoundType = Util.getIntPref(
                Utils.getApp(),
                SoundSelConstraintLayout.KEY_COPTASK_SOUND_SET,
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL)
        mPatrolSoundType = Util.getIntPref(
                Utils.getApp(),
                SoundSelConstraintLayout.KEY_PATROL_SOUND_SET,
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT)
        mMobileSoundType = Util.getIntPref(
                Utils.getApp(),
                SoundSelConstraintLayout.KEY_POPULATION_SOUND_SET,
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL)
        mCacheType = fromValue(Util.getIntPref(
                Utils.getApp(),
                Util.KEY_VIDEO_CACHE_TYPE,
                CacheType.VIDEO_CACHE_TYPE_DEF.cacheDays))
    }

    override fun initView(contentView: View) {
        if (mValidThresh < Util.MIN_FACE_THRESHOOD) {
            mValidThresh = Util.MIN_FACE_THRESHOOD
        }
        if (mValidThresh > Util.MAX_FACE_THRESHOOD) {
            mValidThresh = Util.MAX_FACE_THRESHOOD
        }
        text_face_thresh_value.text = mValidThresh.toString()
        text_lpr_thresh_value.text = mLPRThresh.toString()
        layout_face_thresh_set.setTextView(text_face_thresh_value)
        layout_lpr_thresh_set.setTextView(text_lpr_thresh_value)
        text_gps_locate_value.text = "未定位"
        val versionName = Util.getAPPVersionName(activity)
        val versionCode = Util.getVersionCode(activity)
        text_version_value.text = versionName
        text_version_code_value.text = versionCode
        layout_sound_set.setOnSoundChangeListener(this)
        if (mCoptaskSoundType == SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL) {
            text_coptask_sound_select.setText(R.string.alarm_type_all)
        } else {
            text_coptask_sound_select.setText(R.string.alarm_type_important)
        }
        if (mPatrolSoundType == SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL) {
            text_patrol_sound_select.setText(R.string.alarm_type_all)
        } else {
            text_patrol_sound_select.setText(R.string.alarm_type_important)
        }
        if (mMobileSoundType == SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL) {
            text_mobile_sound_select.setText(R.string.alarm_type_all)
        } else {
            text_mobile_sound_select.setText(R.string.alarm_type_important)
        }
        updateVideoCacheType()
        updateUserInfo(AR110BaseService.mUserInfo)
        progressDialog = ProgressDialog(context!!)
        updateModuleConfig()
    }

    private fun updateVideoCacheType() {
        text_cache_file_select.text = mCacheType.desc
        Util.setIntPref(activity, Util.KEY_VIDEO_CACHE_TYPE, mCacheType.cacheDays)
    }

    override fun initListener() {
        layout_face_thresh.setOnClickListener { v: View? ->
            if (layout_sound_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_cache_file_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_lpr_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            layout_face_thresh_set.visibility = View.VISIBLE
        }
        layout_lpr_thresh.setOnClickListener { v: View? ->
            if (layout_sound_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_cache_file_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_face_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            layout_lpr_thresh_set.visible()
        }
        layout_back.setOnClickListener { v: View? ->
            if (layout_sound_set.visibility == View.VISIBLE) {
                layout_sound_set.visibility = View.GONE
                return@setOnClickListener
            }
            if (layout_face_thresh_set.visibility == View.VISIBLE) {
                layout_face_thresh_set.visibility = View.GONE
                return@setOnClickListener
            }
            if (layout_lpr_thresh_set.visibility == View.VISIBLE) {
                layout_lpr_thresh_set.visibility = View.GONE
                return@setOnClickListener
            }
            if (layout_cache_file_set.visibility == View.VISIBLE) {
                layout_cache_file_set.visibility = View.GONE
                return@setOnClickListener
            }
            NavigationHelper.instance.backToHomePage(this@SettingsFragment)
        }
        layout_coptask_sound.setOnClickListener { v: View? ->
            if (layout_face_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_lpr_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_cache_file_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            layout_sound_set.setSoundTitle("出警声音提醒")
            layout_sound_set.visibility = View.VISIBLE
        }
        layout_patrol_sound.setOnClickListener { v: View? ->
            if (layout_face_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_lpr_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_cache_file_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            layout_sound_set.setSoundTitle("巡逻声音提醒")
            layout_sound_set.visibility = View.VISIBLE
        }
        layout_mobile_sound.setOnClickListener { v: View? ->
            if (layout_face_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_lpr_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_cache_file_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            layout_sound_set.setSoundTitle("流动清查声音提醒")
            layout_sound_set.visibility = View.VISIBLE
        }
        layout_cache_file_set.setOnCacheFileChangeListener(object : OnCacheFileSelectListener {
            override fun onCacheTypeChanged(cacheType: CacheType) {
                mCacheType = cacheType
                updateVideoCacheType()
                layout_cache_file_set.visibility = View.GONE
            }
        })
        layout_cache_file.setOnClickListener { l: View? ->
            if (layout_face_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_lpr_thresh_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            if (layout_sound_set.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            layout_cache_file_set.visibility = View.VISIBLE
        }
        text_upload_log.setOnClickListener { view: View? -> uploadLog() }
        initObserver()
    }

    // todo refactor 日志上传逻辑后期需要抽取
    private var isUploading = false
    private lateinit var progressDialog: ProgressDialog
    private val mUploadList = LinkedList<File>()
    private fun uploadLog() {
        if (isUploading) {
            Util.showMessage("正在上传日志文件！")
            return
        }
        with(mViewModel) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val currentFolder = LeiaBoxEngine.getInstance().settingManager().logsDir
                    val uploadFolder = File(currentFolder)
                    if (!uploadFolder.exists()) {
                        withContext(Dispatchers.Main) {
                            Util.showMessage("要上传的文件夹不存在！")
                        }
                        isUploading = false
                        return@withContext
                    }
                    val fileArray = uploadFolder.listFiles()
                    val upList = LinkedList<File>()
                    if (fileArray == null) {
                        withContext(Dispatchers.Main) { Util.showMessage("该文件夹下没有文件！") }
                        isUploading = false
                        return@withContext
                    }
                    val len = fileArray.size
                    if (len == 0) {
                        withContext(Dispatchers.Main) { Util.showMessage("该文件夹下没有文件！") }
                        isUploading = false
                        return@withContext
                    }
                    for (i in 0 until len) {
                        val file = fileArray[i]
                        if (file.exists() && file.name.endsWith(".log")) {
                            upList.add(fileArray[i])
                        }
                    }
                    if (upList.size == 0) {
                        withContext(Dispatchers.Main) { Util.showMessage("该文件夹下没有日志文件！") }
                        isUploading = false
                        return@withContext
                    }
                    mUploadList.addAll(upList)
                    isUploading = true
                    withContext(Dispatchers.Main) {
                        progressDialog.setMaxProgress(upList.size)
                        progressDialog.show()
                    }
                    for (i in upList.indices) {
                        val videoFile = upList[i]
                        uploadLog(videoFile, videoFile.name, i)
                    }
                }
                isUploading = false
                withContext(Dispatchers.Main) { progressDialog.dismiss() }
                AR110Log.i(TAG, "progressDialog.dismiss()")
            }
        }
    }

    override fun initObserver() {
        mViewModel.uploadLogResult.observe(this, Observer { result ->
            if (result.isSuccess) {
                AR110Log.i(TAG, "observe res=$result")
                progressDialog.refreshProgress(result.position + 1, result.name)
            }
        })
        commtEvent.observe(this, Observer { commEvent: CommEvent ->
            if (commEvent.tag === CommEventTag.MODULE_CONFIG_UPDATE) {
                updateModuleConfig()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        text_face_thresh_value.text = mValidThresh.toString()
        if (AR110BaseService.isInitialized) {
            val locData = AR110BaseService.instance!!.currentLoc
            if (null != locData) {
                val gpsLongitude = "" + locData.longitude
                val gpsLatitude = "" + locData.latitude
                text_gps_locate_value.text = "已定位"
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        if (progressDialog != null && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (layout_sound_set.visibility == View.VISIBLE) {
            layout_sound_set.visibility = View.GONE
            return
        }
        if (layout_face_thresh_set.visibility == View.VISIBLE) {
            layout_face_thresh_set.visibility = View.GONE
            return
        }
        if (layout_lpr_thresh_set.visibility == View.VISIBLE) {
            layout_lpr_thresh_set.visibility = View.GONE
            return
        }
        NavigationHelper.Companion.instance.backToHomePage(this)
    }

    override fun updateGpsLocation(locationData: LocationData) {
        val gpsLongitude = "" + locationData.longitude
        val gpsLatitude = "" + locationData.latitude
        val act: Activity? = activity
        act?.runOnUiThread { text_gps_locate_value.text = "已定位" }
    }

    override fun onSoundTypeChanged(alarmType: String, soundTy: Int) {
        if (alarmType == "出警声音提醒") {
            when (soundTy) {
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL -> {
                    text_coptask_sound_select.setText(R.string.alarm_type_all)
                }
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT -> {
                    text_coptask_sound_select.setText(R.string.alarm_type_important)
                }
            }
        } else if (alarmType == "巡逻声音提醒") {
            when (soundTy) {
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL -> {
                    text_patrol_sound_select.setText(R.string.alarm_type_all)
                }
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT -> {
                    text_patrol_sound_select.setText(R.string.alarm_type_important)
                }
            }
        } else if (alarmType == "流动清查声音提醒") {
            when (soundTy) {
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL -> {
                    text_mobile_sound_select.setText(R.string.alarm_type_all)
                }
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT -> {
                    text_mobile_sound_select.setText(R.string.alarm_type_important)
                }
            }
        }
    }

    private fun updateModuleConfig() {
        layout_coptask_sound.visibility = if (isCopTaskEnable) View.VISIBLE else View.GONE
        layout_patrol_sound.visibility = if (isPatrolTaskEnable) View.VISIBLE else View.GONE
        layout_mobile_sound.visibility = if (isPopulationTaskEnable) View.VISIBLE else View.GONE
    }
}