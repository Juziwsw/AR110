package com.hiar.ar110.service

import android.os.Process
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.Utils
import com.google.gson.Gson
import com.hiar.ar110.activity.AR110MainActivity.Companion.isPushing
import com.hiar.ar110.data.UploadFileData
import com.hiar.ar110.data.audio.AudioUploadReq
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.ImagesUpload
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.cop.VideoInfoUploadData
import com.hiar.ar110.diskcache.AudioLocationDiskCache
import com.hiar.ar110.diskcache.VideoLocationDiskCache
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.EventLiveBus.commtEvent
import com.hiar.ar110.network.api.AR110FileApi
import com.hiar.ar110.util.Base64Util
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.java.KoinJavaComponent
import java.io.File
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class MyVideoUploader {
    private val aR110FileApi: AR110FileApi by KoinJavaComponent.inject(AR110FileApi::class.java)
    private val mWorkThread: Thread
    private val mPatrolWorkThrad: Thread
    private var mLoaderStart = false
    private var mCleanQueue = false
    private val mRequestQueue: BlockingQueue<CopTaskRecord>
    private val mRequestQueuePatrol: BlockingQueue<PatrolRecord>
    private var mCurrentUploadTask: CopTaskRecord? = null
    var mPatrolRecord: PatrolRecord? = null
    private var mObj = Object()
    var mObjPatrol = Object()
    var mBeginUpload = false
    fun cleanWorkQueue() {
        mCleanQueue = true
        synchronized(mObj) { mObj.notify() }
    }

    fun stopWorkThread() {
        mLoaderStart = false
        mWorkThread.interrupt()
    }

    private fun doVideoUpload() {
        try {
            AR110Log.i(TAG, "\n start video upload thread!!!")
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            while (mLoaderStart) {
                kotlin.run outer@{
                    synchronized(mObj) {
                        try {
                            if (mCleanQueue) {
                                do {
                                    mCurrentUploadTask = mRequestQueue.remove()
                                } while (mCurrentUploadTask != null)
                            } else {
                                mCurrentUploadTask = mRequestQueue.remove()
                            }
                        } catch (ex: NoSuchElementException) {
                            AR110Log.i(TAG, "request queue is empty")
                            mCurrentUploadTask = null
                        }
                        mCleanQueue = false
                        if (mCurrentUploadTask == null) {
                            try {
                                mObj.wait()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            return@outer
                        }
                    }
                    AR110Log.i(TAG, "doVideoUpload is mLoaderStart=$mLoaderStart")
                    try {
                        if (!mLoaderStart) {
                            return@outer
                        }
                        Thread.sleep(5000)
                        upLoadVideo(mCurrentUploadTask)
                        upLoadAudio(mCurrentUploadTask)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        mCurrentUploadTask = null
                    }
                }
            }
            AR110Log.i("face_handle", "\n Exit from jingdan video upload routin")
        } catch (e: Exception) {
        }
    }

    private fun doPatrolVideoUpload() {
        try {
            AR110Log.i(TAG, "\n start video upload thread!!! this=$this")
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            while (mLoaderStart) {
                kotlin.run outer@{
                    AR110Log.i(TAG, "doPatrolVideoUpload is mLoaderStart")
                    synchronized(mObjPatrol) {
                        try {
                            if (mCleanQueue) {
                                do {
                                    mPatrolRecord = mRequestQueuePatrol.remove()
                                } while (mPatrolRecord != null)
                            } else {
                                mPatrolRecord = mRequestQueuePatrol.remove()
                            }
                        } catch (ex: NoSuchElementException) {
                            AR110Log.i(TAG, "request queue is empty")
                            mPatrolRecord = null
                        }
                        mCleanQueue = false
                        if (mPatrolRecord == null) {
                            try {
                                mObjPatrol.wait()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            AR110Log.i(TAG, "doPatrolVideoUpload is continue")
                            return@outer
                        }
                    }
                    AR110Log.i(TAG, "doPatrolVideoUpload is mLoaderStart=$mLoaderStart")
                    try {
                        if (!mLoaderStart) {
                            return@outer
                        }
                        Thread.sleep(5000)
                        upLoadPatrolVideo(mPatrolRecord)
                        upLoadPatrolAudio(mPatrolRecord)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                    }
                }
            }

            AR110Log.i("face_handle", "\n Exit from patrol video upload routin")
        } catch (e: Exception) {
        }
    }

    fun requestVideoUpload(record: CopTaskRecord?) {
        if (record == null) {
            return
        }
        synchronized(mObj) {
            if (mRequestQueue.remainingCapacity() > 0) {
                val ret = mRequestQueue.offer(record)
                if (ret) {
                    mObj.notifyAll()
                }
            }
        }
    }

    fun requestPatrolVideoUpload(record: PatrolRecord?) {
        if (record == null) {
            return
        }
        synchronized(mObjPatrol) {
            if (mRequestQueuePatrol.remainingCapacity() > 0) {
                val ret = mRequestQueuePatrol.offer(record)
                if (ret) {
                    mObjPatrol.notifyAll()
                }
            }
        }
    }

    private fun upLoadVideo(mCopTask: CopTaskRecord?) {
        val currentFolder = Util.getVideoRootDir(mCopTask!!.cjdbh2)
        val uploadFolder = File(currentFolder)
        if (!uploadFolder.exists()) {
            return
        }
        val fileArray = uploadFolder.listFiles()
        val upList = LinkedList<File>()
        if (fileArray == null) {
            return
        }
        val len = fileArray.size
        if (len == 0) {
            return
        }
        for (i in 0 until len) {
            val file = fileArray[i]
            if (file.exists() && file.name.endsWith(".mp4")) {
                upList.add(fileArray[i])
            }
        }
        if (upList.size == 0) {
            return
        }
        val upData = VideoInfoUploadData()
        upData.cjdbh = mCopTask.cjdbh
        upData.cjdbh2 = mCopTask.cjdbh2
        upData.deviceId = Util.getIMEI(1)
        upData.jjdbh = mCopTask.jjdbh
        upData.jybh = AR110BaseService.mUserInfo!!.account
        upData.jyxm = AR110BaseService.mUserInfo!!.name
        upData.videoInfo = arrayOfNulls(upList.size)
        val gson = Gson()

        for (i in upList.indices) {
            val videoFile = upList[i]
            val thumbFile = Util.createVideoThumbnail(videoFile)

            val params = MultipartBody.Builder()
            params.setType(MultipartBody.FORM)

            params.addFormDataPart("jjdbh",upData.jjdbh)
            params.addFormDataPart("cjdbh",upData.cjdbh)
            params.addFormDataPart("cjdbh2",upData.cjdbh2)
            params.addFormDataPart("deviceId",upData.deviceId)
            params.addFormDataPart("jybh",upData.jybh)
            params.addFormDataPart("jyxm",upData.jyxm)
            params.addFormDataPart("lastLocation","")

            val filebody = videoFile.asRequestBody(null)
            params.addFormDataPart("file",videoFile.name, filebody)
            params.addFormDataPart("fileMD5",EncryptUtils.encryptMD5File2String(videoFile))
            params.addFormDataPart("videoName",videoFile.name)
            if (null != thumbFile) {
                val imgUpload = ImagesUpload()
                imgUpload.faceId = thumbFile.name
                imgUpload.faceImg = Base64Util.imageToBase64ByLocal(thumbFile.absolutePath)
                val imgsJson = gson.toJson(imgUpload)
                params.addFormDataPart("images",imgsJson)
            }
            val vLoc = VideoLocationDiskCache.getInstance(Utils.getApp()).getVideoLocFromDiskCache(videoFile.name)
            if (null != vLoc) {
                params.addFormDataPart("gpsTime",vLoc.record_time)
                params.addFormDataPart("altitude",vLoc.altitude)
                params.addFormDataPart("latitude",vLoc.latitude)
                params.addFormDataPart("longitude",vLoc.longitude)
            }
            mBeginUpload = true

            val cancelable =  GlobalScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        AR110Log.i(TAG, " upLoadCopVideo")
                        val result = aR110FileApi.upLoadCopVideo(params.build().parts)
                        AR110Log.i(TAG, " upLoadCopVideo complete result=$result")
                        mBeginUpload = false
                        commtEvent.postValue(CommEvent(CommEventTag.COP_VIDEO_TASK_UPLOAD_COMPLETED, mCopTask, videoFile.name))
                    }
                }.onFailure {
                    AR110Log.e(TAG, "onFailure upLoadCopVideo ${it.message}")
                    mBeginUpload = false
                }
            }
            var forceStop = false
            while (mBeginUpload || forceStop) {
                try {
                    Thread.sleep(200)
                    if (isPushing) {
                        cancelable.cancel()
                        forceStop = true
                        break
                    } else {
                        forceStop = false
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun upLoadAudio(mCopTask: CopTaskRecord?) {
        val currentFolder = Util.getAudioRootDir(mCopTask!!.cjdbh2)
        val uploadFolder = File(currentFolder)
        if (!uploadFolder.exists()) {
            return
        }
        val fileArray = uploadFolder.listFiles()
        val upList = LinkedList<UploadFileData>()
        if (fileArray == null) {
            return
        }
        val len = fileArray.size
        if (len == 0) {
            return
        }
        for (i in 0 until len) {
            val file = fileArray[i]
            if (file.exists() && file.name.endsWith(".mp3")) {
                val item = UploadFileData()
                item.mFile = fileArray[i]
                item.id = 0
                upList.add(item)
            }
        }
        if (upList.size == 0) {
            return
        }
        val upData = AudioUploadReq()
        upData.cjdbh = mCopTask.cjdbh
        upData.cjdbh2 = mCopTask.cjdbh2
        upData.deviceId = Util.getIMEI(1)
        upData.jjdbh = mCopTask.jjdbh
        upData.jybh = AR110BaseService.mUserInfo!!.account
        upData.jyxm = AR110BaseService.mUserInfo!!.name
        upData.jygh = upData.jybh
        for (i in upList.indices) {
            val audioFile = upList[i].mFile
            val params = MultipartBody.Builder()
            params.setType(MultipartBody.FORM)
//                params = RequestParams(Util.getAudioAddUrl())
            params.addFormDataPart("jjdbh", upData.jjdbh)
            params.addFormDataPart("cjdbh", upData.cjdbh)
            params.addFormDataPart("cjdbh2", upData.cjdbh2)
            params.addFormDataPart("deviceId", upData.deviceId)
            params.addFormDataPart("jybh", upData.jybh)
            params.addFormDataPart("jygh", upData.jygh)
            params.addFormDataPart("jyxm", upData.jyxm)
            params.addFormDataPart("id", upList[i].id.toString() + "")

            if (null != audioFile) {
                val filebody = audioFile.asRequestBody(null)
                params.addFormDataPart("file",audioFile.name, filebody)
                params.addFormDataPart("audioName",audioFile.name)
                params.addFormDataPart("fileMD5",EncryptUtils.encryptMD5File2String(audioFile))

            }
            val vLoc = AudioLocationDiskCache.getInstance(Utils.getApp()).getAudioLocFromDiskCache(audioFile!!.name)
            if (null != vLoc) {
                AR110Log.i(TAG, "has get MediaLocationData")
                params.addFormDataPart("gpsTime",vLoc.record_time)
                params.addFormDataPart("altitude",vLoc.altitude)
                params.addFormDataPart("latitude",vLoc.latitude)
                params.addFormDataPart("longitude",vLoc.longitude)
            } else {
                AR110Log.i(TAG, "can not get MediaLocationData")
                val time = audioFile.lastModified()
                val recordTime = time.toString()
                params.addFormDataPart("gpsTime",recordTime)
            }

            mBeginUpload = true
            val cancelable = GlobalScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        AR110Log.i(TAG, "addAudio")
                        val result = aR110FileApi.addAudio(Util.mAddAudioUrl,params.build().parts)
                        AR110Log.i(TAG, "addAudio complete result=${result}")
                        mBeginUpload = false
                    }
                }.onFailure {
                    AR110Log.i(TAG, "addAudio onFailure message=${it.message}")
                    mBeginUpload = false
                }
            }

            var forceStop = false
            while (mBeginUpload) {
                try {
                    Thread.sleep(200)
                    if (isPushing) {
                        cancelable.cancel()
                        AR110Log.i(TAG, "begin cancel audio upload !!!")
                        forceStop = true
                        break
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            if (forceStop) {
                break
            }
        }
    }

    private fun upLoadPatrolVideo(mTask: PatrolRecord?) {
        val currentFolder = Util.getVideoRootDir(mTask!!.number)
        val uploadFolder = File(currentFolder)
        if (!uploadFolder.exists()) {
            return
        }
        val fileArray = uploadFolder.listFiles()
        val upList = LinkedList<File>()
        if (fileArray == null) {
            return
        }
        val len = fileArray.size
        if (len == 0) {
            return
        }
        for (i in 0 until len) {
            val file = fileArray[i]
            if (file.exists() && file.name.endsWith(".mp4")) {
                upList.add(fileArray[i])
            }
        }
        if (upList.size == 0) {
            return
        }
        val upData = VideoInfoUploadData()
        upData.deviceId = Util.getIMEI(1)
        upData.patrolNumber = mTask.number
        upData.jybh = AR110BaseService.mUserInfo!!.account
        upData.jyxm = AR110BaseService.mUserInfo!!.name
        upData.videoInfo = arrayOfNulls(upList.size)
        for (i in upList.indices) {
            val videoFile = upList[i]
            val thumbFile = Util.createVideoThumbnail(videoFile)
//            val params = RequestParams(Util.getPatrolVideoUploadUrl())
            val params = MultipartBody.Builder()
            params.setType(MultipartBody.FORM)
            params.addFormDataPart("patrolNumber",mTask.number)
            params.addFormDataPart("deviceId",upData.deviceId)
            params.addFormDataPart("jybh",upData.jybh)
            params.addFormDataPart("jyxm",upData.jyxm)
            params.addFormDataPart("jygh",upData.jybh)

            val filebody = videoFile.asRequestBody(null)
            params.addFormDataPart("file",videoFile.name, filebody)
            params.addFormDataPart("fileMD5",EncryptUtils.encryptMD5File2String(videoFile))

//            params.addFormDataPart("videoName",videoFile.name)
            if (null != thumbFile) {
                val imgUpload = ImagesUpload()
                imgUpload.faceId = thumbFile.name
                imgUpload.faceImg = Base64Util.imageToBase64ByLocal(thumbFile.absolutePath)
                val gson = Gson()
                val imgsJson = gson.toJson(imgUpload)
                params.addFormDataPart("images",imgsJson)
            }
            val vLoc = VideoLocationDiskCache.getInstance(Utils.getApp()).getVideoLocFromDiskCache(videoFile.name)
            if (null != vLoc) {
                params.addFormDataPart("gpsTime",vLoc.record_time)
                params.addFormDataPart("altitude",vLoc.altitude)
                params.addFormDataPart("latitude",vLoc.latitude)
                params.addFormDataPart("longitude",vLoc.longitude)
            }
            mBeginUpload = true

            val cancelable = GlobalScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        AR110Log.d(TAG, " upLoadPatrolVideo")
                        val result = aR110FileApi.upLoadPatrolVideo(params.build().parts)
                        AR110Log.d(TAG, " upLoadPatrolVideo complete result=${result}")
                        mBeginUpload = false
                        commtEvent.postValue(CommEvent(CommEventTag.PATROL_VIDEO_TASK_UPLOAD_COMPLETED, mTask, videoFile.name))
                    }
                }.onFailure {
                    AR110Log.e(TAG, "onFailure upLoadPatrolVideo ${it.message}")
                    mBeginUpload = false
                }
            }
            AR110Log.d(TAG, "addPatrolVideo check")
            var forceStop = false
            while (mBeginUpload || forceStop) {
                try {
                    Thread.sleep(200)
                    if (isPushing) {
                        cancelable.cancel()
                        forceStop = true
                        break
                    } else {
                        forceStop = false
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun upLoadPatrolAudio(patrolRecord: PatrolRecord?) {
        val currentFolder = Util.getAudioRootDir(patrolRecord!!.number)
        val uploadFolder = File(currentFolder)
        if (!uploadFolder.exists()) {
            return
        }
        val fileArray = uploadFolder.listFiles()
        val upList = LinkedList<UploadFileData>()
        if (fileArray == null) {
            return
        }
        val len = fileArray.size
        if (len == 0) {
            return
        }
        for (i in 0 until len) {
            val file = fileArray[i]
            if (file.exists() && file.name.endsWith(".mp3")) {
                val item = UploadFileData()
                item.mFile = fileArray[i]
                item.id = 0
                upList.add(item)
            }
        }
        if (upList.size == 0) {
            return
        }
        val upData = AudioUploadReq()
        upData.patrolNumber = patrolRecord.number
        upData.deviceId = Util.getAndroidID()
        upData.jybh = AR110BaseService.mUserInfo!!.account
        upData.jyxm = AR110BaseService.mUserInfo!!.name
        upData.jygh = upData.jybh
        for (i in upList.indices) {
            val audioFile = upList[i].mFile
            val params = MultipartBody.Builder()
            params.setType(MultipartBody.FORM)
            params.addFormDataPart("patrolNumber",upData.patrolNumber)
            params.addFormDataPart("deviceId",upData.deviceId)
            params.addFormDataPart("jybh",upData.jybh)
            params.addFormDataPart("jygh",upData.jygh)
            params.addFormDataPart("jyxm",upData.jyxm)
            params.addFormDataPart("id",upList[i].id.toString() + "")
            if (null != audioFile) {
                val filebody = audioFile.asRequestBody(null)
                params.addFormDataPart("file",audioFile.name, filebody)
                params.addFormDataPart("audioName",audioFile.name)
                params.addFormDataPart("fileMD5",EncryptUtils.encryptMD5File2String(audioFile))
            }
            val vLoc = AudioLocationDiskCache.getInstance(Utils.getApp()).getAudioLocFromDiskCache(audioFile!!.name)
            if (null != vLoc) {
                AR110Log.i(TAG, "has get MediaLocationData")
                params.addFormDataPart("gpsTime",vLoc.record_time)
                params.addFormDataPart("altitude",vLoc.altitude)
                params.addFormDataPart("latitude",vLoc.latitude)
                params.addFormDataPart("longitude",vLoc.longitude)
            } else {
                AR110Log.i(TAG, "can not get MediaLocationData")
                val time = audioFile.lastModified()
                val recordTime = time.toString()
                params.addFormDataPart("gpsTime",recordTime)
            }
            mBeginUpload = true

            val cancelable = GlobalScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        AR110Log.i(TAG, " addPatrolAudio")
                        val result = aR110FileApi.addAudio(Util.mAddPatrolAudioUrl,params.build().parts)
                        AR110Log.i(TAG, " addPatrolAudio complete result=$result")
                        mBeginUpload = false
                    }
                }.onFailure {
                    AR110Log.e(TAG, "onFailure addPatrolAudio ${it.message}")
                    mBeginUpload = false
                }
            }
            var forceStop = false
            while (mBeginUpload) {
                try {
                    Thread.sleep(200)
                    if (isPushing) {
                        cancelable.cancel()
                        AR110Log.i(TAG, "begin cancel video upload !!!")
                        forceStop = true
                        break
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            if (forceStop) {
                break
            }
        }
    }

    fun isVideoUploading(taskId: String?): Boolean {
        synchronized(mObj) {
            if (null == mCurrentUploadTask && null == mPatrolRecord) {
                return false
            }
            if (mCurrentUploadTask != null) {
                val cjdbh2 = mCurrentUploadTask!!.cjdbh2
                if (cjdbh2 != null && cjdbh2 == taskId) {
                    return true
                }
            }
            if (mPatrolRecord != null) {
                val num = mPatrolRecord!!.number
                if (num != null && num == taskId) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private const val TAG = "MyVideoUploader"
    }

    init {
        mRequestQueue = ArrayBlockingQueue(6)
        mRequestQueuePatrol = ArrayBlockingQueue(6)
        mWorkThread = Thread { doVideoUpload() }
        mWorkThread.name = "VideoUpload"
        mLoaderStart = true
        mWorkThread.start()
        mPatrolWorkThrad = Thread { doPatrolVideoUpload() }
        mPatrolWorkThrad.name = "PatrolVideoUpload"
        mLoaderStart = true
        mPatrolWorkThrad.start()
    }
}