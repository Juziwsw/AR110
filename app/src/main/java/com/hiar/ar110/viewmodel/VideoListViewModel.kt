package com.hiar.ar110.viewmodel

import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.GetVideoUploadedData
import com.hiar.ar110.data.MyVideoInfo
import com.hiar.ar110.data.UploadFileData
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.ImagesUpload
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.cop.VideoInfoUploadData
import com.hiar.ar110.diskcache.VideoLocationDiskCache
import com.hiar.ar110.extension.toJson
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.network.api.AR110FileApi
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Base64Util
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.java.KoinJavaComponent.inject
import java.io.File

/**
 *
 * @author wilson
 * @date 24/05/2021
 * Email: haiqin.chen@hiscene.com
 */
class VideoListViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    private val fileApi: AR110FileApi by inject(AR110FileApi::class.java)
    val getVideoListState = EventMutableLiveData<MutableList<MyVideoInfo>>()
    val getCopVideoUploadStateStatus = EventMutableLiveData<GetVideoUploadedData>()
    val getPatrolVideoUploadStateStatus = EventMutableLiveData<GetVideoUploadedData>()
    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }

    fun getVideoList(mCjdbh: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                val result = loadLocalVideoList(mCjdbh)
                getVideoListState.postValue(result)
            }.onFailure {
                AR110Log.e(TAG, it.message)
            }
        }
    }

    fun updateVideoState(mCopTask: CopTaskRecord?) {
        if (null == mCopTask) return
        val params = HashMap<String, String>()
        params.put("cjdbh", mCopTask.cjdbh)
        params.put("cjdbh2", mCopTask.cjdbh2)
        params.put("jybh", AR110BaseService.mUserInfo!!.account)
        params.put("deviceId", Util.getAndroidID())
        params.put("from", "0")
        params.put("limit", "200")
        request({
            ar110Api.getCopVideoStatus(params)
        },{
            getCopVideoUploadStateStatus.postValue(it)
        })
    }

    fun updatePatrolVideoState(mPatrolTask: PatrolRecord?) {
        if (null == mPatrolTask) return
        val params = HashMap<String, String>()
        params.put("patrolNumber", mPatrolTask.number)
        params.put("jygh", AR110BaseService.mUserInfo!!.account)
        params.put("deviceId", Util.getAndroidID())
        params.put("from", "0")
        params.put("limit", "200")
        request({
            ar110Api.getPatrolVideoState(params)
        },{
            getPatrolVideoUploadStateStatus.postValue(it)
        })
    }

    suspend fun upLoadCopVideo(upData: VideoInfoUploadData, fileData: UploadFileData): Boolean = withContext(Dispatchers.IO) {
        val thumbFile = Util.createVideoThumbnail(fileData.mFile)
        val multiBuilder = MultipartBody.Builder()
        multiBuilder.setType(MultipartBody.FORM)
        val filebody = fileData.mFile.asRequestBody(null)
        multiBuilder.addFormDataPart("file", fileData.mFile.name, filebody)
        multiBuilder.addFormDataPart("fileMD5",EncryptUtils.encryptMD5File2String(fileData.mFile))

        multiBuilder.addFormDataPart("jjdbh", upData.jjdbh)
        multiBuilder.addFormDataPart("cjdbh", upData.cjdbh)
        multiBuilder.addFormDataPart("cjdbh2", upData.cjdbh2)
        multiBuilder.addFormDataPart("lastLocation", "")
        multiBuilder.addFormDataPart("videoName", fileData.mFile.name)
        multiBuilder.addFormDataPart("deviceId", upData.deviceId)
        multiBuilder.addFormDataPart("jybh", upData.jybh)
        multiBuilder.addFormDataPart("jyxm", upData.jyxm)
        multiBuilder.addFormDataPart("id", fileData.id.toString() + "")

        if (null != thumbFile) {
            val imgUpload = ImagesUpload()
            imgUpload.faceId = thumbFile.name
            imgUpload.faceImg = Base64Util.imageToBase64ByLocal(thumbFile.absolutePath)
            multiBuilder.addFormDataPart("images", imgUpload.toJson()!!)
        }

        val vLoc = VideoLocationDiskCache.getInstance(Utils.getApp()).getVideoLocFromDiskCache(fileData.mFile.getName())
        if (null != vLoc) {
            AR110Log.i(TAG, "has get MediaLocationData")
            multiBuilder.addFormDataPart("gpsTime", vLoc.record_time)
            multiBuilder.addFormDataPart("altitude", vLoc.altitude)
            multiBuilder.addFormDataPart("latitude", vLoc.latitude)
            multiBuilder.addFormDataPart("longitude", vLoc.longitude)
        } else {
            AR110Log.i(TAG, "can not get MediaLocationData")
            val time: Long = fileData.mFile.lastModified()
            val recordTime = time.toString()
            multiBuilder.addFormDataPart("gpsTime", recordTime)
        }
        kotlin.runCatching {
            val response = fileApi.upLoadCopVideo(multiBuilder.build().parts) ?: return@withContext false
            return@withContext response.isSuccess
        }.onFailure {
            AR110Log.e(TAG, "upLoadCopVideo onfail ${it.message}")
        }
        return@withContext false
    }

    suspend fun upLoadPatrolVideo(mPatrolTask: PatrolRecord, upData: VideoInfoUploadData, fileData: UploadFileData): Boolean = withContext(Dispatchers.IO) {
        val thumbFile = Util.createVideoThumbnail(fileData.mFile)
        Util.getPatrolVideoUploadUrl()
        val multiBuilder = MultipartBody.Builder()
        multiBuilder.setType(MultipartBody.FORM)
        val filebody = fileData.mFile.asRequestBody(null)
        multiBuilder.addFormDataPart("file", fileData.mFile.name, filebody)
        multiBuilder.addFormDataPart("fileMD5",EncryptUtils.encryptMD5File2String(fileData.mFile))

        multiBuilder.addFormDataPart("patrolNumber", mPatrolTask.number)
        multiBuilder.addFormDataPart("jygh", upData.jybh)
        multiBuilder.addFormDataPart("deviceId", upData.deviceId)
        multiBuilder.addFormDataPart("jybh", upData.jybh)
        multiBuilder.addFormDataPart("jyxm", upData.jyxm)
        multiBuilder.addFormDataPart("id", fileData.id.toString() + "")

        if (null != thumbFile) {
            val imgUpload = ImagesUpload()
            imgUpload.faceId = thumbFile.name
            imgUpload.faceImg = Base64Util.imageToBase64ByLocal(thumbFile.absolutePath)
            multiBuilder.addFormDataPart("images", imgUpload.toJson()!!)
        }

        val vLoc = VideoLocationDiskCache.getInstance(Utils.getApp()).getVideoLocFromDiskCache(fileData.mFile.getName())
        if (null != vLoc) {
            AR110Log.i(TAG, "has get MediaLocationData")
            multiBuilder.addFormDataPart("gpsTime", vLoc.record_time)
            multiBuilder.addFormDataPart("altitude", vLoc.altitude)
            multiBuilder.addFormDataPart("latitude", vLoc.latitude)
            multiBuilder.addFormDataPart("longitude", vLoc.longitude)

        } else {
            AR110Log.i(TAG, "can not get MediaLocationData")
            val time: Long = fileData.mFile.lastModified()
            val recordTime = time.toString()
            multiBuilder.addFormDataPart("gpsTime", recordTime)
        }
        kotlin.runCatching {
            val response = fileApi.upLoadPatrolVideo(multiBuilder.build().parts) ?: return@withContext false
            return@withContext response.isSuccess
        }.onFailure {
            AR110Log.e(TAG, "upLoadCopVideo onfail ${it.message}")
        }
        return@withContext false
    }

    private suspend fun loadLocalVideoList(mCjdbh: String): MutableList<MyVideoInfo> = withContext(Dispatchers.IO) {
        val mDataList = mutableListOf<MyVideoInfo>()
        val videoPath = Util.getVideoRootDir(mCjdbh)
        val videoFolder = File(videoPath)
        if (videoFolder.exists() && videoFolder.isDirectory) {
            val fileArray = videoFolder.listFiles()
            val len = fileArray.size
            if (len > 0) {
                for (i in 0 until len) {
                    val videoFile = fileArray[i]
                    if (videoFile.exists() && videoFile.isFile) {
                        if (videoFile.name.endsWith(".mp4")) {
                            mDataList.add(MyVideoInfo(videoFile.absolutePath, videoFile.name, Util.VIDEO_NOT_UPLOADED))
                        }
                    }
                }
                if (mDataList.size > 0) {
                    mDataList.sortByDescending {
                        it.mVideoName
                    }
                }
            }
        }
        return@withContext mDataList
    }
}