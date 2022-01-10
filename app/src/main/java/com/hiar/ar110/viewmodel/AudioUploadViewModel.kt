package com.hiar.ar110.viewmodel

import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.activity.AR110MainActivity.Companion.isPushing
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.ProgressViewStatus
import com.hiar.ar110.data.UploadFileData
import com.hiar.ar110.data.audio.AudioFileInfo
import com.hiar.ar110.data.audio.AudioUploadReq
import com.hiar.ar110.data.audio.AudioUploadStatus
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.diskcache.AudioLocationDiskCache
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.network.api.AR110FileApi
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.java.KoinJavaComponent
import java.io.File
import java.util.*

/**
 * author: liwf
 * date: 2021/4/7 11:37
 */
class AudioUploadViewModel : BaseViewModel() {
    private var uploadIndex = 0 //上传的位置
    private var mCjdbh: String? = null
    var audioRootPath: String? = null
        private set
    private var isUploading = false
    private var mCopTask: CopTaskRecord? = null
    private var mPatrolTask: PatrolRecord? = null
    private var upData: AudioUploadReq? = null
    val progressDialogStatus = EventMutableLiveData<ProgressViewStatus>()
    val audioList = EventMutableLiveData<ArrayList<AudioFileInfo>?>()
    private val mAudioUploadList: ArrayList<AudioUploadStatus> = ArrayList() //录音文件上传状态
    private val mUploadList = LinkedList<UploadFileData>() //需要上传的文件
    private val aR110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
    private val aR110FileApi: AR110FileApi by KoinJavaComponent.inject(AR110FileApi::class.java)

    override fun onNewMessage(i: Int, multVal: MultVal) {}
    fun setCjdbh(mCjdbh: String?) {
        this.mCjdbh = mCjdbh
    }

    fun setCopTask(mCopTask: CopTaskRecord?) {
        this.mCopTask = mCopTask
    }

    fun setPatrolTask(mPatrolTask: PatrolRecord?) {
        this.mPatrolTask = mPatrolTask
    }

    /**
     * 获取警单对应本地录音文件
     */
    suspend fun refreshAudio() {
        audioRootPath = Util.getAudioRootDir(mCjdbh)
        val audioFolder = File(audioRootPath)
        if (!audioFolder.exists()) {
            return
        }
        val audioArray = audioFolder.listFiles { dir: File?, name: String -> name.endsWith(".mp3") }
                ?: return
        if (audioArray.isEmpty()) {
            return
        }
        val mAudioList = if (audioList.value != null) audioList.value else ArrayList()
        mAudioList!!.clear()
        for (file in audioArray) {
            val audioInfo = AudioFileInfo()
            audioInfo.isPlaying = false
            //            audioInfo.mCurPlayPos = 0;
            audioInfo.mFileName = file.name
            audioInfo.mUploadStatus = 0
            audioInfo.mCurPlayPos = 0
            audioInfo.absPath = file.absolutePath
            audioInfo.mAudioLen = (Util.getAudioFileVoiceTime(audioInfo.absPath) / 1000).toInt()
            if (audioInfo.mAudioLen > 0) {
                mAudioList.add(audioInfo)
            }
        }
        mAudioList.sortWith(Comparator { o1: AudioFileInfo, o2: AudioFileInfo -> -o1.mFileName.compareTo(o2.mFileName) })
        audioList.postValue(mAudioList)
        if (mCopTask != null) {
            syncAudioStatus()
        } else if (mPatrolTask != null) {
            syncPatrolAudioState()
        }
    }

    /**
     * 同步警单录音上传状态
     */
    private suspend fun syncAudioStatus() {
//        var params: RequestParams? = null
        if (mCopTask == null) {
            return
        }
//        params = RequestParams(Util.getAudioListUrl())
        val map: MutableMap<String, String> = HashMap()
        map["cjdbh2"] = mCopTask!!.cjdbh2

//        params.addBodyParameter("cjdbh2", mCopTask!!.cjdbh2)
        mAudioUploadList.clear()
        kotlin.runCatching {
            val result= aR110Api.getAudioList(map)
            if (result?.data != null) {
                try {
                    val audioList = result.data!!.audioList
                    if (audioList != null && audioList.size > 0) {
                        val arrayLen = audioList.size
                        for (i in 0 until arrayLen) {
                            val item = audioList[i]
                            val audioUrl = item.name
                            val isUpload = item.isUpload
                            val id = item.id
                            if (null != audioUrl) {
                                val state = AudioUploadStatus(isUpload, audioUrl, id)
                                mAudioUploadList.add(state)
                            }
                        }
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                val imgSceneListLen = mAudioUploadList.size
                val mAudioList = if (audioList.value != null) audioList.value else ArrayList()
                val len = mAudioList!!.size
                if (len > 0) {
                    for (i in 0 until len) {
                        val info = mAudioList[i]
                        if (imgSceneListLen > 0) {
                            val uploadRes = Util.isAudioUploaded(mAudioUploadList, info.mFileName)
                            when {
                                uploadRes >= 0 -> {
                                    info.mUploadStatus = Util.VIDEO_NOT_UPLOADED
                                }
                                uploadRes == -1 -> {
                                    info.mUploadStatus = Util.VIDEO_UPLOAD_OK
                                }
                                uploadRes == -2 -> {
                                    info.mUploadStatus = Util.VIDEO_NOT_UPLOADED
                                }
                            }
                        } else {
                            info.mUploadStatus = Util.VIDEO_NOT_UPLOADED
                        }
                    }
                    audioList.postValue(mAudioList)
                    AR110Log.i(TAG,"syncAudioStatus postValue")
                }
            }
            AR110Log.i(TAG,"syncAudioStatus onsuccess ")
        }.onFailure {
            AR110Log.e(TAG,"syncAudioStatus onFailure message=${it.message}")
        }

    }

    /**
     * 同步巡逻录音上传状态
     */
    private  suspend fun syncPatrolAudioState() {
        if (mPatrolTask == null) {
            return
        }
        val map: MutableMap<String, String> = HashMap()
        map["patrolNumber"] = mPatrolTask!!.number
        mAudioUploadList.clear()
        kotlin.runCatching {
            var result=aR110Api.getPatrolAudioList(map)
            if (result != null&&result.isSuccess&&null != result.data) {
                val listData = result.data!!.audioList
                if (null != listData && listData.size > 0) {
                    try {
                        val arrayLen = listData.size
                        for (i in 0 until arrayLen) {
                            val item = listData[i]
                            val audioUrl = item.name
                            val isUpload = 1
                            val id = item.id
                            if (null != audioUrl) {
                                val state = AudioUploadStatus(isUpload, audioUrl, id)
                                mAudioUploadList.add(state)
                            }
                        }
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }
                val imgSceneListLen = mAudioUploadList.size
                val mAudioList = if (audioList.value != null) audioList.value else ArrayList()
                val len = mAudioList!!.size
                if (len > 0) {
                    for (i in 0 until len) {
                        val info = mAudioList[i]
                        if (imgSceneListLen > 0) {
                            val uploadRes = Util.isAudioUploaded(mAudioUploadList, info.mFileName)
                            when {
                                uploadRes >= 0 -> {
                                    info.mUploadStatus = Util.VIDEO_NOT_UPLOADED
                                }
                                uploadRes == -1 -> {
                                    info.mUploadStatus = Util.VIDEO_UPLOAD_OK
                                }
                                uploadRes == -2 -> {
                                    info.mUploadStatus = Util.VIDEO_NOT_UPLOADED
                                }
                            }
                        } else {
                            info.mUploadStatus = Util.VIDEO_NOT_UPLOADED
                        }
                    }
                    audioList.postValue(mAudioList)
                    AR110Log.i(TAG," getPatrolAudioList onsussess mAudioList.size=${mAudioList.size}, mAudioList=${mAudioList}")
                }
            }
            AR110Log.i(TAG," getPatrolAudioList onsussess result=${result}")
        }.onFailure {
            AR110Log.e(TAG," getPatrolAudioList onfail message=${it.message}")
        }
    }

    /**
     * 录音文件上传
     */
    fun uploadAudio() {
        if (!Util.isNetworkConnected(Utils.getApp().applicationContext)) {
            Util.showMessage("网络已经断开！")
            return
        }
        val mAudioList = audioList.value
        if (mAudioList == null || mAudioList.size == 0) {
            Util.showMessage("没有媒体文件可以上传！")
            return
        }
        if (isUploading) {
            Util.showMessage("正在上传文件！")
            return
        }
        val currentFolder = Util.getAudioRootDir(mCjdbh)
        val uploadFolder = File(currentFolder)
        if (!uploadFolder.exists()) {
            Util.showMessage("要上传的文件夹不存在！")
            isUploading = false
            return
        }
        val fileArray = uploadFolder.listFiles()
        mUploadList.clear()
        if (fileArray == null) {
            Util.showMessage("该文件夹下没有文件！")
            isUploading = false
            return
        }
        val len = fileArray.size
        if (len == 0) {
            Util.showMessage("该文件夹下没有文件！")
            isUploading = false
            return
        }
        for (file in fileArray) {
            if (file.exists() && file.name.endsWith(".mp3")) {
                if (mAudioUploadList.size > 0) {
                    val index = Util.isAudioUploaded(mAudioUploadList, file.name)
                    if (index >= 0) {
                        val item = UploadFileData()
                        item.mFile = file
                        item.id = mAudioUploadList[index].mId
                        mUploadList.add(item)
                    } else if (index == -2) {
                        val item = UploadFileData()
                        item.mFile = file
                        item.id = 0
                        mUploadList.add(item)
                    }
                } else {
                    if (mAudioUploadList.size == 0) {
                        val item = UploadFileData()
                        item.mFile = file
                        item.id = 0
                        mUploadList.add(item)
                    }
                }
            }
        }
        if (mUploadList.size == 0) {
            Util.showMessage("所有视频文件都上传完毕，无需上传")
            isUploading = false
            return
        }
        Util.showMessage("开始上传文件")
        upData = AudioUploadReq()
        if (null != mCopTask) {
            upData!!.cjdbh = mCopTask!!.cjdbh
            upData!!.cjdbh2 = mCopTask!!.cjdbh2
            upData!!.deviceId = Util.getIMEI(1)
            upData!!.jjdbh = mCopTask!!.jjdbh
        } else if (mPatrolTask != null) {
            upData!!.patrolNumber = mPatrolTask!!.number
            upData!!.deviceId = Util.getIMEI(1)
        }
        upData!!.jybh = AR110BaseService.mUserInfo!!.account
        upData!!.jyxm = AR110BaseService.mUserInfo!!.name
        upData!!.jygh = upData!!.jybh
        isUploading = true

        //显示progressDialog
        val showProgressStatus = ProgressViewStatus(ProgressViewStatus.SHOW)
        showProgressStatus.maxProgress = mUploadList.size
        progressDialogStatus.postValue(showProgressStatus)
        uploadIndex = 0
        realUploadAudio() //调用上传
    }

    /**
     * 真正网络请求方法
     */
    private fun realUploadAudio() {
        val audioFile = mUploadList[uploadIndex].mFile
        val params = MultipartBody.Builder()
        var url:String?=null
        if (mCopTask != null) {
            url = Util.mAddAudioUrl
            params.addFormDataPart("jjdbh", upData!!.jjdbh)
            params.addFormDataPart("cjdbh", upData!!.cjdbh)
            params.addFormDataPart("cjdbh2", upData!!.cjdbh2)
            params.addFormDataPart("deviceId", upData!!.deviceId)
            params.addFormDataPart("jybh", upData!!.jybh)
            params.addFormDataPart("jygh", upData!!.jygh)
            params.addFormDataPart("jyxm", upData!!.jyxm)
        } else if (mPatrolTask != null) {
            url = Util.mAddPatrolAudioUrl
            params.addFormDataPart("patrolNumber", upData!!.patrolNumber)
            params.addFormDataPart("deviceId", upData!!.deviceId)
            params.addFormDataPart("jybh", upData!!.jybh)
            params.addFormDataPart("jygh", upData!!.jygh)
            params.addFormDataPart("jyxm", upData!!.jyxm)
        }
        params.addFormDataPart("id", mUploadList[uploadIndex].id.toString() + "")
        if (null != audioFile) {
            //params.addBodyParameter("file", audioFile);
            val filebody = audioFile.asRequestBody(null)
            params.addFormDataPart("file",audioFile.name, filebody)
//            params.addFormDataPart("file", audioFile, null, audioFile.name)
            params.addFormDataPart("audioName", audioFile.name)
            params.addFormDataPart("fileMD5", EncryptUtils.encryptMD5File2String(audioFile))
        }
        val vLoc = AudioLocationDiskCache.getInstance(Utils.getApp()).getAudioLocFromDiskCache(audioFile!!.name)
        if (null != vLoc) {
            AR110Log.i(TAG, "has get MediaLocationData")
            params.addFormDataPart("gpsTime", vLoc.record_time)
            params.addFormDataPart("altitude", vLoc.altitude)
            params.addFormDataPart("latitude", vLoc.latitude)
            params.addFormDataPart("longitude", vLoc.longitude)
        } else {
            AR110Log.i(TAG, "can not get MediaLocationData")
            val time = audioFile.lastModified()
            val recordTime = time.toString()
            params.addFormDataPart("gpsTime", recordTime)
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    AR110Log.i(TAG, " realUploadAudio")
                    val result = aR110FileApi.addAudio(url!!,params.build().parts)
                    AR110Log.i(TAG, " realUploadAudio complete result=$result")
                    if(result!=null) {
                        val updateProgressStatus = ProgressViewStatus(ProgressViewStatus.UPDATE)
                        updateProgressStatus.progress = uploadIndex + 1
                        updateProgressStatus.filename = mUploadList[uploadIndex].mFile.name
                        progressDialogStatus.postValue(updateProgressStatus)
                        if (isPushing) {
                            AR110Log.i(TAG, "begin cancel video upload !!!")
                            upData = null
                            isUploading = false
                            //隐藏progressDialog
                            val cancelProgressStatus = ProgressViewStatus(ProgressViewStatus.CANCEL)
                            progressDialogStatus.postValue(cancelProgressStatus)
                            return@withContext
                        }
                        if (uploadIndex >= mUploadList.size - 1) {
                            AR110Log.i(TAG, "audio file upload finish")
                            upData = null
                            isUploading = false
                            //隐藏progressDialog
                            val cancelProgressStatus = ProgressViewStatus(ProgressViewStatus.CANCEL)
                            progressDialogStatus.postValue(cancelProgressStatus)
                            return@withContext
                        }

                        //继续上传下一个文件
                        uploadIndex++
                        realUploadAudio()
                    }
                }
                AR110Log.i(TAG, "onSuccess realUploadAudio uploadIndex=$uploadIndex")
            }.onFailure {
                AR110Log.e(TAG, "onFailure realUploadAudio ${it.message}")
            }
        }

    }
}