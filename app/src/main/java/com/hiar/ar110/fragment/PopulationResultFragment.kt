package com.hiar.ar110.fragment

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.activity.AR110MainActivity.Companion.isPushing
import com.hiar.ar110.adapter.PeopleCropImgAdapter
import com.hiar.ar110.adapter.PopulationDateListAdapter
import com.hiar.ar110.adapter.PopulationPeopleListAdapter
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.people.FaceCompareBaseInfo
import com.hiar.ar110.data.people.FaceHisData
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.EventLiveBus
import com.hiar.ar110.extension.gone
import com.hiar.ar110.extension.invisible
import com.hiar.ar110.extension.setOnThrottledClickListener
import com.hiar.ar110.extension.visible
import com.hiar.ar110.factory.RecogFactory
import com.hiar.ar110.helper.NavigationHelper.Companion.instance
import com.hiar.ar110.listener.OnImageItemClickListener
import com.hiar.ar110.mutiscreen.PopulationSreenHelper
import com.hiar.ar110.mutiscreen.SecondaryScreen
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.service.MyMqttManager
import com.hiar.ar110.util.Util
import com.hiar.ar110.util.showConfirmDialog
import com.hiar.ar110.viewmodel.PopulationResultViewModel
import com.hiar.ar110.widget.MySurfaceView
import com.hiar.ar110.widget.RecyclerViewMultiClickListener
import com.hiar.ar110.widget.RecyclerViewMultiClickListener.SimpleOnItemClickListener
import com.hiar.ar110.widget.SoundSelConstraintLayout
import com.hiar.ar110.widget.SpaceDecoration
import com.hiar.mybaselib.recog.FaceRecognitionInfo
import com.hiar.mybaselib.recog.IFaceEngine
import com.hiar.mybaselib.utils.AR110Log
import com.hileia.common.enginer.LeiaBoxEngine
import com.serenegiant.usbcameracommon.UVCCameraHandler
import kotlinx.android.synthetic.main.population_people_result.*
import java.util.*

/**
 *
 * @author xuchengtang
 * @date 11/06/2021
 * Email: xucheng.tang@hiscene.com
 */
class PopulationResultFragment : BaseFragment(), AR110MainActivity.CameraFrameCallback, SecondaryScreen.OnSecondScreenCallback {
    private var mPatrolRecord: PopulationRecord? = null
    private var mPeopleListAdapter: PopulationPeopleListAdapter? = null
    private var mDateListAdapter: PopulationDateListAdapter? = null
    private var mPeopleCropAdapter: PeopleCropImgAdapter? = null
    private var mTaskSoundType = SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL

    /**
     * 已识别人脸数据
     */
    private val mPeopleInfoList = mutableListOf<MutableList<FaceCompareBaseInfo>>()
    private val mPeopleNotInLibMap: MutableMap<String, MutableList<FaceCompareBaseInfo>> = HashMap()
    private var mNotInLibNum = 0
    private var mInLibNum = 0
    private var mCurrentDate: String? = null
    private val mDateList: MutableList<String> = mutableListOf()
    private val mFaceEngine: IFaceEngine = RecogFactory.getFaceEngine()
    /**
     * 未识别人脸数据
     */
    private var mUnRecogArray: MutableList<FaceCompareBaseInfo>? = null
    private var mPeoResult: FaceHisData? = null
    private var firstVisibleItem: Long = 0
    private var mLastFaceDetectionInfo: MutableList<FaceRecognitionInfo>? = null
    private val mFaceList = mutableListOf<FaceRecognitionInfo>()
    private var mLastChangeTime: Long = 0
    private lateinit var mCameraHandler: UVCCameraHandler
    private var mPopulationSreenHelper: PopulationSreenHelper? = null
    private var mLoadMusicOk: Boolean = false
    private lateinit var mFaceView: MySurfaceView
    private val mSoundPool: SoundPool by lazy {
        val soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        soundPool
    }
    private var isFirstStart :Boolean =true

    private var mNoticeMusicId: Int = 0
    private val mViewModel by lazy {
        getViewModel(PopulationResultViewModel::class.java)
    }
    private val isFmActive: Boolean
        get() {
            val am: AudioManager? = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (am == null) {
                Log.w(TAG, "isFmActive: couldn't get AudioManager reference")
                return false
            }
            return am.isMusicActive
        }

    override fun getLayoutId(): Int {
        return R.layout.population_people_result
    }

    override fun initData() {
        if (arguments != null) {
            mPatrolRecord = arguments!!.getParcelable("key_population_task")
        }
        mSoundPool.setOnLoadCompleteListener { _: SoundPool?, _: Int, _: Int -> mLoadMusicOk = true }
        mNoticeMusicId = mSoundPool.load(activity, R.raw.type, 1)
        mTaskSoundType = Util.getIntPref(
                Utils.getApp(),
                SoundSelConstraintLayout.KEY_POPULATION_SOUND_SET,
                SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL)
        mCameraHandler = (activity as AR110MainActivity?)!!.mCameraHandler!!
        mPatrolRecord?.let { AR110BaseService.instance!!.setPopulationTask(it) }
        mPopulationSreenHelper = PopulationSreenHelper(activity!!,this).apply {
            lifecycle.addObserver(this)
        }
    }

    override fun initView(contentView: View) {
        img_drop_icon.invisible()
        mFaceView = face_view
        initRecognizeRecycle()
        initUnRecognize()
        initDateRecycle()
        initFaceEngine()
        initGlassState()
        if (Util.mNeedMultiScreen) {
            checkPermission()
            mPopulationSreenHelper?.startSecondaryScreen()
        }
    }

    override fun initListener() {
        recycler_cropimg_view.addOnItemTouchListener(RecyclerViewMultiClickListener(context, recycler_cropimg_view, object : SimpleOnItemClickListener() {
            override fun onItemClick(view: View, position: Int) {
                val url = Util.getPeoplePhotoIpHead() + mPeopleCropAdapter!!.mListData[position].name
                (activity as AR110MainActivity?)!!.showLargeImg(url)
            }
        }))
        layout_back.setOnClickListener {
            instance.backPopulationVerification()
        }
        text_end_task.setOnThrottledClickListener {
            showConfirmDlg()
        }
        text_unrecog.isActivated = false
        text_recog.isActivated = true
        text_date_sel.setOnClickListener { img_drop_icon!!.performClick() }
        text_unrecog.setOnClickListener {
            text_unrecog.isActivated = true
            text_recog.isActivated = false
            people_list_recog!!.visibility = View.GONE
            people_list_notrecog!!.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(mCurrentDate)) {
                mCurrentDate?.let { it1 -> setNewDate(it1) }
            }
            (activity as AR110MainActivity?)!!.hideLargeImg()
        }
        text_recog!!.setOnClickListener {
            text_unrecog.isActivated = false
            text_recog.isActivated = true
            people_list_notrecog!!.visibility = View.GONE
            people_list_recog!!.visibility = View.VISIBLE
            if (mPeopleInfoList.size > 0) {
                mPeopleListAdapter!!.setAdapter(mPeopleInfoList)
            }
            (activity as AR110MainActivity?)!!.hideLargeImg()
        }
    }

    override fun initObserver() {
        mViewModel.peopleHisResult.observe(this, Observer { peoRes ->
            AR110Log.d(TAG, "facelist $peoRes")
            doAfterGetHisData(peoRes)
        })

        mViewModel.modifyState.observe(this, Observer {
            if (it.isSuccess) {
                text_end_task.gone()
                releaseEngine()
            }
        })

        mViewModel.mFaceRecognizerAdd.observe(this, Observer {
            doAfterRecognizeAdd(it)
        })

        mViewModel.mFaceUnRecognizerAdd.observe(this, Observer {
            doAfterUnRecognizeAdd(it)
        })

        EventLiveBus.commtEvent.observe(this, Observer {
            if (it.tag == CommEventTag.GLASS_CONNECT_STATE_CHANGE) {
                val isConnected = it.data as Boolean
                if (isConnected) {
                    onGlassConnect()
                } else {
                    onGlassDisconnect()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (isPushing) {
            layout_commu_phone.visible()
        }
        fetchFaceHisData()
        if(isFirstStart){
            isFirstStart=false
        }else {
            MyMqttManager.instance!!.updateStatus(MyMqttManager.POPULATION_TASK_STATUS,if(mPatrolRecord?.status == 0)Util.CJZT_CJZ else Util.CJZT_YWC)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseEngine()
    }

    override fun onBackPressed() {
        instance.backPopulationVerification()
    }

    override fun onHileiaComingCallCallback() {
        layout_commu_phone?.visible()
    }

    override fun onHileiaHangupCallback() {
        layout_commu_phone?.gone()
    }

    var mFrameCountAfterFaceFound = 0
    override fun onFrameAvailable(data: ByteArray?, width: Int, height: Int) {
        val result = mFaceEngine.run(data, width, height, 21, true)
        if (result.isNullOrEmpty()) {
            mLastFaceDetectionInfo = null
            mLastChangeTime = System.currentTimeMillis()
            mFaceView.clearFaceRect()
            return
        }
        val faceDetectionInfos =result.toMutableList()
        if (faceDetectionInfos.isNotEmpty()) {
            AR110Log.i("frame", "face_number=" + faceDetectionInfos.size)
            mFaceList.clear()
            mFrameCountAfterFaceFound++ // reset to zero
            faceDetectionInfos.forEach {
                mFaceList.add(it)
            }
            mFaceView.offerRects(mFaceList.toList())
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
                    mViewModel.faceRequest(mPatrolRecord, data!!.clone(), width, height, mFaceList)
                    AR110Log.i("frame", "requestFaceDetectionInfo !")
                }
            }
        } else {
            mLastFaceDetectionInfo = null
            mLastChangeTime = System.currentTimeMillis()
            mFaceView.clearFaceRect()
            mFrameCountAfterFaceFound = 0
        }
    }

    override fun takePicture(data: ByteArray?, width: Int, height: Int) {
    }

    override fun handleOcr(mSdkInited: Int, yuvData: ByteArray, mNeedOcr: Boolean) {
    }

    private fun initUnRecognize() {
        recycler_cropimg_view.addItemDecoration(SpaceDecoration(resources.getDimension(R.dimen.dp8).toInt(), resources.getDimension(R.dimen.dp4).toInt()))
        mPeopleCropAdapter = PeopleCropImgAdapter(context!!.applicationContext)
        recycler_cropimg_view.adapter = mPeopleCropAdapter
    }

    private fun initDateRecycle() {
        val mLinearLayoutManagerDate = LinearLayoutManager(activity)
        mDateListAdapter = PopulationDateListAdapter(mDateList) {
            setNewDate(mDateList[it])
        }
        recycler_view_datelist.layoutManager = mLinearLayoutManagerDate
        recycler_view_datelist.addItemDecoration(SpaceDecoration(0, resources.getDimension(R.dimen.dp1).toInt()))
        recycler_view_datelist.adapter = mDateListAdapter
    }

    private fun initRecognizeRecycle() {
        mPeopleListAdapter = PopulationPeopleListAdapter(object : OnImageItemClickListener {
            override fun onItemClick(url: String) {
                (activity as AR110MainActivity?)!!.showLargeImg(url)
            }
        }, mPeopleInfoList)
        val mLinearLayoutManager = LinearLayoutManager(activity)
        recycler_view_peopleinfo.layoutManager = mLinearLayoutManager
        recycler_view_peopleinfo.addItemDecoration(SpaceDecoration(0, resources.getDimension(R.dimen.dp16).toInt()))
        recycler_view_peopleinfo.adapter = mPeopleListAdapter
    }

    private fun initFaceEngine() {
        if (mPatrolRecord?.status == 0) {
            mFaceEngine.initFaceEngine(activity!!, LeiaBoxEngine.getInstance().settingManager().logsDir)
            (activity as AR110MainActivity?)?.setCameraFrameCallback(this)
            text_end_task.visible()
            MyMqttManager.instance!!.updateStatus(MyMqttManager.POPULATION_TASK_STATUS,Util.CJZT_CJZ)
        } else {
            text_end_task.gone()
            MyMqttManager.instance!!.updateStatus(MyMqttManager.POPULATION_TASK_STATUS,Util.CJZT_YWC)
        }
    }

    private fun initGlassState() {
        if (mCameraHandler.isOpened) {
            onGlassConnect()
        } else {
            onGlassDisconnect()
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

    private fun releaseEngine() {
        (activity as AR110MainActivity?)?.setCameraFrameCallback(null)
        mFaceEngine.releaseFaceEngine()
    }

    private fun playNoticeSound() {
        if (!mLoadMusicOk) {
            return
        }
        if (isFmActive && !AR110MainActivity.isPushing) {
            AR110Log.i("music", "is using audio ")
            return
        }
        mSoundPool.play(mNoticeMusicId, 1f, 1f, 1, 0, 1f)
    }

    private fun showConfirmDlg() {
        context?.let {
            showConfirmDialog(it, "确定完成本次核查吗？") {
                mViewModel.modifyState("${mPatrolRecord?.id}")
                MyMqttManager.instance!!.updateStatus(MyMqttManager.POPULATION_TASK_STATUS,Util.CJZT_YWC)
            }
        }
    }

    private fun onGlassConnect() {
        iv_glasses.setImageResource(R.drawable.icon_into_glasses)
        if (Util.mNeedMultiScreen) {
            mPopulationSreenHelper?.apply {
                if (mSecondaryScreen == null) {
                    if (startSecondaryScreen()) {
                        show()
                    }
                } else {
                    show()
                }
            }
        }
    }

    private fun onGlassDisconnect() {
        iv_glasses.setImageResource(R.drawable.icon_into_glasses_invalid)
        mPopulationSreenHelper?.dismiss()
    }

    private fun setNewDate(currentDate: String) {
        recycler_view_datelist.gone()
        img_drop_icon.setImageResource(R.drawable.ic_icon_up)
        val index = mDateList.indexOf(currentDate)
        mUnRecogArray = mPeoResult!!.unrecoFace!![index]
        if (null != mUnRecogArray && mUnRecogArray!!.isNotEmpty()) {
            mCurrentDate = currentDate
            val dateArray = mCurrentDate!!.split("-").toTypedArray()
            val dateDisplay = String.format("%s年%s月%s日", dateArray[0], dateArray[1], dateArray[2])
            text_date_sel.text = dateDisplay
            val faceList = mUnRecogArray?.filter {
                it.gpsTime.contains(currentDate)
            }
            mPeopleCropAdapter?.setAdapter(faceList)
        }
    }

    private fun fetchFaceHisData() {
        mViewModel.fetchFaceHiscene(mPatrolRecord)
    }

    private fun doAfterGetHisData(peoRes: FaceHisData) {
        mPeopleInfoList.clear()
        mPeopleNotInLibMap.clear()
        mDateList.clear()
        mPeoResult = peoRes
        Util.mFacePhotoUrlHead = peoRes.imageUrl
        if (null == peoRes.recoFace) {
            peoRes.recoFace = mutableListOf()
        }
        if (null == peoRes.unrecoFace) {
            peoRes.unrecoFace = mutableListOf()
        }
        if (peoRes.recoFace!!.isNotEmpty()) {
            for (i in peoRes.recoFace!!.indices) {
                mPeopleInfoList.add(peoRes.recoFace!![i])
            }
        } else {
            mInLibNum = 0
        }
        if (peoRes.unrecoFace!!.isNotEmpty()) {
            mNotInLibNum = 0
            mPeopleNotInLibMap.clear()
            mDateList.clear()
            val dateLen = peoRes.unrecoFace!!.size
            for (j in 0 until dateLen) {
                mUnRecogArray = peoRes.unrecoFace!![j]
                val itemLen = mUnRecogArray!!.size
                mCurrentDate = ""
                for (i in 0 until itemLen) {
                    val face = mUnRecogArray!![i]
                    var gpsTime = face.gpsTime
                    gpsTime = gpsTime.substring(0, "2021-01-27".length)
                    if (!TextUtils.isEmpty(face.gpsTime)) {
                        if (!mDateList.contains(gpsTime)) {
                            mDateList.add(0, gpsTime)
                        }
                        mNotInLibNum++
                    }
                }
            }
            mDateList.sortWith { o1, o2 -> -o1!!.compareTo(o2!!) }
            mCurrentDate = if (mDateList.size > 0) {
                mDateList[0]
            } else {
                ""
            }
        } else {
            mNotInLibNum = 0
            mCurrentDate = ""
        }
        mInLibNum = mPeopleInfoList.size

        val strInLib = getString(R.string.vehicle_has_recog) + " " + String.format(" (%d)", mInLibNum)
        text_recog.text = strInLib
        mPeopleListAdapter?.setAdapter(mPeopleInfoList)
        if (!TextUtils.isEmpty(mCurrentDate)) {
            val dateArray = mCurrentDate!!.split("-").toTypedArray()
            val dateDisplay = String.format("%s年%s月%s日", dateArray[0], dateArray[1], dateArray[2])
            text_date_sel!!.text = dateDisplay
            if (mNotInLibNum > 0) {
                img_drop_icon.visible()
            } else {
                img_drop_icon.invisible()
            }
            img_drop_icon.isClickable = true
            img_drop_icon.setOnThrottledClickListener {
                if (recycler_view_datelist.visibility == View.VISIBLE) {
                    recycler_view_datelist.invisible()
                    img_drop_icon.setImageResource(R.drawable.ic_icon_up)
                } else {
                    mDateListAdapter?.setAdapter(mDateList)
                    recycler_view_datelist.visible()
                    img_drop_icon.setImageResource(R.drawable.ic_icon_down)
                }
            }
        }
        text_recog.isClickable = true
        text_unrecog.isClickable = true
    }

    /**
     *  新增识别到的数据处理  新增条目数据 or 追加到对应的条目数据中
     */
    private fun doAfterRecognizeAdd(it: MutableList<FaceCompareBaseInfo>) {
        val info = it[0]
        var index = -1
        for (i in mPeopleInfoList.indices) {
            val item = mPeopleInfoList[i][0]
            if (item.cardId == info.cardId) {
                index = i
                break
            }
        }
        if (mTaskSoundType == SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL) {
            playNoticeSound()
        } else {
            if (info.labelCode >= 3) {
                playNoticeSound()
            }
        }
        if (index == -1) {
            // 新增一条
            mPeopleInfoList.add(0, it)
            mInLibNum = mPeopleInfoList.size
            val strInLib = getString(R.string.vehicle_has_recog) + " " + String.format(" (%d)", mInLibNum)
            text_recog.text = strInLib
            mPeopleListAdapter?.notifyItemInserted(0)
            recycler_view_peopleinfo.scrollToPosition(0)
        } else {
            // 添加到原有数据中，刷新条目
            mPeopleListAdapter?.addNewFace(recycler_view_peopleinfo.findViewHolderForAdapterPosition(index), index, it)
            recycler_view_peopleinfo.scrollToPosition(0)
        }
        mPopulationSreenHelper?.updateView(it, mPeopleInfoList)
    }

    /**
     * 新增未识别数据处理  新增日期数据 or 追加到对应的日期数据中
     */
    private fun doAfterUnRecognizeAdd(it: MutableList<FaceCompareBaseInfo>) {
        val face = it[0]
        var gpsTime = face.gpsTime
        gpsTime = gpsTime.substring(0, "2021-01-27".length)
        if (!TextUtils.isEmpty(face.gpsTime)) {
            if (!mDateList.contains(gpsTime)) {
                // 新增日期未识别数据
                mDateList.add(0, gpsTime)
                mPeopleNotInLibMap[gpsTime] = it
                mPeoResult!!.unrecoFace!!.addAll(0, mutableListOf(it))
                mDateListAdapter?.notifyDataSetChanged()
            } else {
                // 旧日期未识别数据添加一条
                val index = mDateList.indexOf(gpsTime)
                mUnRecogArray = mPeoResult!!.unrecoFace!![index]
                mUnRecogArray?.addAll(0, it)
            }
            setNewDate(gpsTime)
        }
    }
    override fun onStop() {
        super.onStop()
        MyMqttManager.instance!!.updateStatus(-1, -1)
    }
    override fun onSecondScreenStart() {
        mFaceView =mPopulationSreenHelper?.mFaceView!!
    }
}