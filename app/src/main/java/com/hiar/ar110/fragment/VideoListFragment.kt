package com.hiar.ar110.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.adapter.VideoWallAdapter
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.MyVideoInfo
import com.hiar.ar110.data.UploadFileData
import com.hiar.ar110.data.VideoUploadState
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.cop.VideoInfoUploadData
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.EventLiveBus
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.VideoListViewModel
import com.hiar.ar110.widget.ProgressDialog
import com.hiar.mybaselib.utils.AR110Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FilenameFilter
import java.util.*

/**
 * Author:wilson.chen
 * date：5/26/21
 * desc：
 */
class VideoListFragment : BaseFragment() {
    private var mCjdbh: String? = null
    private var mCopTask: CopTaskRecord? = null
    private var mPatrolTask: PatrolRecord? = null
    private var isUploading = false

    /**
     * 本地的文件列表
     */
    private val mVideoList = mutableListOf<MyVideoInfo>()

    /**
     * 已上传的文件列表
     */
    private val mVideoUploadList = mutableListOf<VideoUploadState>()
    private val mViewModel: VideoListViewModel by lazy {
        getViewModel(VideoListViewModel::class.java)
    }

    private lateinit var contentView: View
    private val mGridView: GridView by lazy {
        contentView.findViewById<GridView>(R.id.grid_view)
    }
    private val mLayoutBack: RelativeLayout by lazy {
        contentView.findViewById<RelativeLayout>(R.id.layout_back)
    }
    private val mImgUpload: ImageView by lazy {
        contentView.findViewById<ImageView>(R.id.img_upload)
    }
    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(activity!!)
    }
    private val mAdapter by lazy {
        VideoWallAdapter(activity!!, mGridView, mVideoList)
    }

    override fun onBackPressed() {
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_cop_task_video
    }

    override fun initData() {
        mCopTask = arguments!!.getParcelable("key_cop_task")
        mPatrolTask = arguments!!.getParcelable("key_patrol_task")
        if (null != mCopTask) {
            mCjdbh = mCopTask!!.cjdbh2
        } else if (mPatrolTask != null) {
            mCjdbh = mPatrolTask!!.number
        }
    }

    override fun initView(view: View) {
        this.contentView = view
        mGridView.adapter = mAdapter
    }

    override fun initListener() {
        mLayoutBack.setOnClickListener { v: View? ->
            if (progressDialog.isShowing) {
                return@setOnClickListener
            }
            NavigationHelper.instance.backToHandleJD(this)
//            NavigationHelper.instance.beginHandleJD(mCopTask, mPatrolTask)
        }
        mImgUpload.setOnClickListener { v: View? ->
            if (!Util.isNetworkConnected(activity)) {
                Util.showMessage("网络已经断开！")
                return@setOnClickListener
            }
            if (mVideoList.size == 0) {
                Util.showMessage("没有媒体文件可以上传！")
                return@setOnClickListener
            }
            if (isUploading || AR110BaseService.instance!!.isVideoUploading(mCjdbh)) {
                Util.showMessage("正在上传文件！")
                return@setOnClickListener
            }
            if (AR110MainActivity.isPushing) {
                Util.showMessage("正在视频通讯中，暂不支持上传视频")
                return@setOnClickListener
            }
            upLoadVideoFiles()
        }

        mGridView.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, position: Int, l: Long ->
            val movieIntent = Intent()
            val info: MyVideoInfo = mVideoList[position]
            val filePath = info.mVideoAbsName
            val videoFile = File(filePath)
            val videoURI = FileProvider.getUriForFile(Utils.getApp(),
                    "com.hiar.ar110.fileprovider", videoFile)
            movieIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, false)
            movieIntent.action = Intent.ACTION_VIEW
            movieIntent.setDataAndType(videoURI, "video/*")
            val resInfoList = activity!!.packageManager
                    .queryIntentActivities(movieIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                activity!!.grantUriPermission(packageName, videoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            startActivity(movieIntent)
        }
    }

    override fun initObserver() {
        mViewModel.apply {
            getVideoListState.observe(this@VideoListFragment, Observer {
                mVideoList.clear()
                mVideoList.addAll(it)
                mAdapter.notifyDataSetChanged()
                refreshVideoUploadStatus()
            })
            getCopVideoUploadStateStatus.observe(this@VideoListFragment, Observer { videoUploadState ->
                val statues = videoUploadState?.videoList?.mapNotNull {
                    VideoUploadState(it.isUpload, it.name, it.id)
                }?.toMutableList()
                updateVideoStatus(statues)
            })
            getPatrolVideoUploadStateStatus.observe(this@VideoListFragment, Observer { videoUploadState ->
                val statues = videoUploadState?.videoList?.mapNotNull {
                    VideoUploadState(1, it.name, it.id)
                }?.toMutableList()
                updateVideoStatus(statues)
            })
        }
        EventLiveBus.commtEvent.observe(this, Observer {
            if (it.tag == CommEventTag.COP_VIDEO_TASK_UPLOAD_COMPLETED) {
                val copTaskRecord = it.data as CopTaskRecord? ?: return@Observer
                val fileName = it.data2 as String? ?: return@Observer
                if (copTaskRecord.cjdbh2 == mCopTask?.cjdbh2) {
                    for (info in mVideoList) {
                        if (info.mVideoName == fileName) {
                            info.upLoadState = Util.VIDEO_UPLOAD_OK
                            mVideoUploadList.add(VideoUploadState(1, info.mVideoName, info.id))
                            break
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
            if (it.tag == CommEventTag.PATROL_VIDEO_TASK_UPLOAD_COMPLETED) {
                val patrolRecord = it.data as PatrolRecord? ?: return@Observer
                val fileName = it.data2 as String? ?: return@Observer
                if (patrolRecord.number == mPatrolTask?.number) {
                    for (info in mVideoList) {
                        if (info.mVideoName == fileName) {
                            info.upLoadState = Util.VIDEO_UPLOAD_OK
                            mVideoUploadList.add(VideoUploadState(1, info.mVideoName, info.id))
                            break
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun refreshVideoUploadStatus() {
        mViewModel.apply {
            if (mCopTask != null) {
                updateVideoState(mCopTask)
            } else if (mPatrolTask != null) {
                updatePatrolVideoState(mPatrolTask)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        refreshData()
    }

    private fun refreshData() {
        mCjdbh?.let { mViewModel.getVideoList(it) }
    }

    private fun updateVideoStatus(statues: MutableList<VideoUploadState>?) {
        if (!statues.isNullOrEmpty()) {
            mVideoUploadList.clear()
            mVideoUploadList.addAll(statues)
        }
        for (info in mVideoList) {
            if (null != statues) {
                val uploadRes = Util.isFileUploaded(statues, info.mVideoName, mCopTask != null)
                if (uploadRes >= 0) {
                    info.upLoadState = Util.VIDEO_NOT_UPLOADED
                } else if (uploadRes == -1) {
                    info.upLoadState = Util.VIDEO_UPLOAD_OK
                } else if (uploadRes == -2) {
                    info.upLoadState = Util.VIDEO_NOT_UPLOADED
                }
            } else {
                info.upLoadState = Util.VIDEO_NOT_UPLOADED
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    private fun upLoadVideoFiles() {
        val currentFolder = Util.getVideoRootDir(mCjdbh)
        val uploadFolder = File(currentFolder)
        val fileArray: Array<File> = uploadFolder.listFiles(FilenameFilter { dir: File?, name: String -> name.endsWith(".mp4") })
        val len = fileArray.size
        if (len == 0) {
            Util.showMessage("该文件夹下没有文件！")
            isUploading = false
            return
        }
        lifecycleScope.launch(Dispatchers.Main) {
            val upList = LinkedList<UploadFileData>()
            for (file in fileArray) {
                if (file.exists()) {
                    if (mVideoUploadList.size > 0) {
                        val index = Util.isFileUploaded(mVideoUploadList, file.name, mCopTask != null)
                        if (index >= 0) {
                            val item = UploadFileData()
                            item.mFile = file
                            item.id = mVideoUploadList[index].mId
                            upList.add(item)
                        } else if (index == -2) {
                            val item = UploadFileData()
                            item.mFile = file
                            item.id = 0
                            upList.add(item)
                        }
                    } else {
                        val item = UploadFileData()
                        item.mFile = file
                        item.id = 0
                        upList.add(item)
                    }
                }
            }
            if (upList.size == 0) {
                Util.showMessage("所有视频文件都上传完毕，无需上传")
                return@launch
            }
            Util.showMessage("开始上传文件")
            progressDialog.setMaxProgress(upList.size)
            progressDialog.show()

            val upData = VideoInfoUploadData()
            if (null != mCopTask) {
                upData.cjdbh = mCopTask!!.cjdbh
                upData.cjdbh2 = mCopTask!!.cjdbh2
                upData.jjdbh = mCopTask!!.jjdbh
            } else if (null != mPatrolTask) {
                upData.patrolNumber = mPatrolTask!!.number
            }

            upData.deviceId = Util.getIMEI(1)
            upData.jybh = AR110BaseService.mUserInfo!!.account
            upData.jyxm = AR110BaseService.mUserInfo!!.name
            upData.videoInfo = arrayOfNulls(upList.size)
            var index = 0
            for (fileData in upList) {
                kotlin.runCatching {
                    if (null != mCopTask) {
                        mViewModel.upLoadCopVideo(upData, fileData)
                    } else if (null != mPatrolTask) {
                        mViewModel.upLoadPatrolVideo(mPatrolTask!!, upData, fileData)
                    }
                    index++
                }.onSuccess {
                    progressDialog.refreshProgress(index, fileData.mFile.getName())
                }.onFailure {
                    AR110Log.e(TAG, "video upload fail name=${fileData.mFile.name}  msg=${it.message}")
                    Util.showMessage("文件上传失败")
                }
            }
            progressDialog.dismiss()
            refreshData()
        }
    }
}