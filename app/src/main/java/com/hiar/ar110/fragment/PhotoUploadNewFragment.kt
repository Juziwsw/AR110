package com.hiar.ar110.fragment

import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.adapter.PhotoAdapter
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.MyPhotoInfo
import com.hiar.ar110.data.UploadFileData
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.photo.PhotoUploadReq
import com.hiar.ar110.data.photo.PhotoUploadStatus
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.PhotoListViewModel
import com.hiar.ar110.widget.ProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * Author:wilson.chen
 * date：5/27/21
 * desc：
 */
class PhotoUploadNewFragment : BaseFragment() {
    private var isUploading = false
    private var mCopTask: CopTaskRecord? = null
    private var mPatrolTask: PatrolRecord? = null
    private var mBeginUpload = false
    private var mCjdbh: String? = null

    /**
     * 本地文件列表
     */
    private val mPhotoList = mutableListOf<MyPhotoInfo>()

    /**
     * 已上传文件，通过接口返回
     */
    private val mPhotoUploadList = mutableListOf<PhotoUploadStatus>()
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
        PhotoAdapter(activity!!, mGridView, mPhotoList)
    }
    private val mViewModel by lazy {
        getViewModel(PhotoListViewModel::class.java)
    }

    override fun onBackPressed() {
//        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_photo_upload
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
            NavigationHelper.instance.backToHandleJD(this@PhotoUploadNewFragment)
        }
        mGridView.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, position: Int, l: Long ->
            val info: MyPhotoInfo = mPhotoList[position]
            val filePath = info.mPhotoAbsName
            val photoFile = File(filePath)
            if (photoFile.exists()) {
                (activity as AR110MainActivity?)?.showLargeImg(filePath)
            }
        }
        mImgUpload.setOnClickListener { v: View? ->
            if (!Util.isNetworkConnected(activity)) {
                Util.showMessage("网络已经断开！")
                return@setOnClickListener
            }
            if (mPhotoList.size == 0) {
                Util.showMessage("没有媒体文件可以上传！")
                return@setOnClickListener
            }
            if (isUploading) {
                Util.showMessage("正在上传文件！")
                return@setOnClickListener
            }
            if (AR110MainActivity.isPushing) {
                Util.showMessage("正在视频通讯中，暂不支持上传视频")
                return@setOnClickListener
            }
            upLoadPhotoFiles()
        }
    }

    override fun initObserver() {
        mViewModel.apply {
            getPhotoListState.observe(this@PhotoUploadNewFragment, Observer {
                mPhotoList.clear()
                mPhotoList.addAll(it)
                mAdapter.notifyDataSetChanged()
                if (mCopTask != null) {
                    updatePhotoState(mCopTask)
                } else if (mPatrolTask != null) {
                    updatePatrolPhotoState(mPatrolTask)
                }
            })
            getCopPhotoUploadStateStatus.observe(this@PhotoUploadNewFragment, Observer { data ->
                val statues = data?.sceneImageList?.mapNotNull {
                    PhotoUploadStatus(it.isUpload, it.name, it.id)
                }?.toMutableList()
                updateVideoStatus(statues)
            })
            getPatrolPhotoUploadStateStatus.observe(this@PhotoUploadNewFragment, Observer { data ->
                val statues = data?.sceneImageList?.mapNotNull {
                    PhotoUploadStatus(1, it.name, it.id)
                }?.toMutableList()
                updateVideoStatus(statues)
            })
        }
    }

    override fun onStart() {
        super.onStart()
        refreshData()
    }

    private fun updateVideoStatus(statues: MutableList<PhotoUploadStatus>?) {
        if (!statues.isNullOrEmpty()) {
            mPhotoUploadList.clear()
            mPhotoUploadList.addAll(statues)
        }
        for (info in mPhotoList) {
            if (null != statues) {
                val uploadRes = Util.isFileUploaded(statues, info.mPhotoName, true)
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

    private fun upLoadPhotoFiles() {
        val currentFolder = Util.getPhotoRootDir(mCjdbh)
        val uploadFolder = File(currentFolder)
        if (!uploadFolder.exists()) {
            Util.showMessage("要上传的文件夹不存在！")
            isUploading = false
            return
        }
        val fileArray: Array<File> = uploadFolder.listFiles({ dir: File?, name: String -> name.endsWith(".jpg") })
        val len = fileArray.size
        if (len == 0) {
            Util.showMessage("该文件夹下没有文件！")
            isUploading = false
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            kotlin.runCatching {
                val upList = LinkedList<UploadFileData>()
                for (file in fileArray) {
                    if (file.exists()) {
                        if (mPhotoUploadList.size > 0) {
                            val index = Util.isFileUploaded(mPhotoUploadList, file.name, true)
                            if (index >= 0) {
                                val item = UploadFileData()
                                item.mFile = file
                                item.id = mPhotoUploadList[index].mId
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
                    Util.showMessage("所有图片文件都上传完毕，无需上传")
                    return@launch
                }
                Util.showMessage("开始上传文件")
                progressDialog.setMaxProgress(upList.size)
                progressDialog.show()

                val upData = PhotoUploadReq()
                if (null != mCopTask) {
                    upData.cjdbh = mCopTask!!.cjdbh
                    upData.cjdbh2 = mCopTask!!.cjdbh2
                    upData.deviceId = Util.getIMEI(1)
                    upData.jjdbh = mCopTask!!.jjdbh
                    upData.copNum = mCopTask!!.cops[0].copNum
                } else if (mPatrolTask != null) {
                    upData.patrolNumber = mPatrolTask!!.number
                    upData.deviceId = Util.getIMEI(1)
                }

                upData.jybh = AR110BaseService.mUserInfo!!.account
                upData.jyxm = AR110BaseService.mUserInfo!!.name
                upData.jygh = upData.jybh
                upData.imei = Util.getIMEI(0)
                upData.imsi = upData.imei
                var index = 0
                for (fileData in upList) {
                    index++
                    if (null != mCopTask) {
                        mViewModel.upLoadCopPhoto(mCopTask!!, upData, fileData)
                    } else if (null != mPatrolTask) {
                        mViewModel.upLoadPatrolPhoto(mPatrolTask!!, upData, fileData)
                    }
                    progressDialog.refreshProgress(index, fileData.mFile.getName())
                }
                progressDialog.dismiss()
                refreshData()
            }.onFailure {

            }
        }
    }

    private fun refreshData() {
        mCjdbh?.let { mViewModel.getPhotoList(it) }
    }
}