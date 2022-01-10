package com.hiar.ar110.viewmodel

import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.MyPhotoInfo
import com.hiar.ar110.data.UploadFileData
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.photo.PhotoUploadReq
import com.hiar.ar110.data.photo.SceneImage
import com.hiar.ar110.data.photo.SceneImageList
import com.hiar.ar110.diskcache.PhotoLocationDiskCache
import com.hiar.ar110.extension.toJson
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Base64Util
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import java.io.File

/**
 *
 * @author wilson
 * @date 24/05/2021
 * Email: haiqin.chen@hiscene.com
 */
class PhotoListViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    val getPhotoListState = EventMutableLiveData<MutableList<MyPhotoInfo>>()
    val getCopPhotoUploadStateStatus = EventMutableLiveData<SceneImageList>()
    val getPatrolPhotoUploadStateStatus = EventMutableLiveData<SceneImageList>()
    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }

    fun getPhotoList(mCjdbh: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                val result = loadLocalPhotoList(mCjdbh)
                getPhotoListState.postValue(result)
            }.onFailure {
                AR110Log.e(TAG, it.message)
            }
        }
    }

    fun updatePhotoState(mCopTask: CopTaskRecord?) {
        if (null == mCopTask) return
        val params = HashMap<String, String>()
        params.put("cjdbh2", mCopTask.cjdbh2)
        params.put("account", AR110BaseService.mUserInfo!!.account)
        request({
            ar110Api.getCopPhotoStatus(params)
        }, {
            getCopPhotoUploadStateStatus.postValue(it)
        })
    }

    fun updatePatrolPhotoState(mPatrolTask: PatrolRecord?) {
        if (null == mPatrolTask) return
        val params = HashMap<String, String>()
        params.put("patrolNumber", mPatrolTask.number)
        params.put("account", AR110BaseService.mUserInfo!!.account)
        request({
            ar110Api.getPatrolPhotoState(params)
        }, {
            getPatrolPhotoUploadStateStatus.postValue(it)
        })
    }

    suspend fun upLoadCopPhoto(mCopTask: CopTaskRecord, upData: PhotoUploadReq, fileData: UploadFileData): Boolean = withContext(Dispatchers.IO) {
        val photoFile = fileData.mFile ?: return@withContext false
        val url = "/api/police/v0.1/live/addSceneImage"
        val params = HashMap<String, String>()
        mCopTask.let {
            params.put("jjdbh", it.jjdbh)
            params.put("cjdbh", it.cjdbh)
            params.put("cjdbh2", it.cjdbh2)
        }
        params.put("deviceId", upData.deviceId)
        params.put("jybh", upData.jybh)
        params.put("jygh", upData.jygh)
        params.put("jyxm", upData.jyxm)
        params.put("imei", upData.imei)
        params.put("imsi", upData.imsi)

        val imgUpload = SceneImage()
        imgUpload.sceneId = photoFile.name
        imgUpload.sceneImage = Base64Util.imageToBase64ByLocal(photoFile.getAbsolutePath())
        val imgsJson: String = imgUpload.toJson() ?: return@withContext false

        val vLoc = PhotoLocationDiskCache.getInstance(Utils.getApp()).getPhotoLocFromDiskCache(photoFile.name)
        if (null != vLoc) {
            AR110Log.i(TAG, "has get MediaLocationData")
            params.put("gpsTime", vLoc.record_time)
            params.put("altitude", vLoc.altitude)
            params.put("latitude", vLoc.latitude)
            params.put("longitude", vLoc.longitude)
        } else {
            AR110Log.i(TAG, "can not get MediaLocationData")
            val time = photoFile.lastModified()
            val recordTime = time.toString()
            params.put("gpsTime", recordTime)
        }
        val result = ar110Api.uploadPhoto(url, params, imgsJson) ?: return@withContext false
        AR110Log.i(TAG, "photo upload result: ${result.isSuccess}")
        return@withContext result.isSuccess
    }

    suspend fun upLoadPatrolPhoto(patrolTask: PatrolRecord, upData: PhotoUploadReq, fileData: UploadFileData): Boolean = withContext(Dispatchers.IO) {
        val photoFile = fileData.mFile
        val params = HashMap<String, String>()
        withContext(Dispatchers.IO) {
            val url = "/api/police/v0.1/patrol/sceneImage/add"
            patrolTask.let {
                params["patrolNumber"] = it.number
            }
            params["deviceId"] = Util.getIMEI(1)
            params["jybh"] = upData.jybh
            params["jygh"] = upData.jygh
            params["jyxm"] = upData.jyxm
            params["imei"] = upData.imei
            params["imsi"] = upData.imsi
            val imgUpload = SceneImage()
            imgUpload.sceneId = photoFile.name
            imgUpload.sceneImage = Base64Util.imageToBase64ByLocal(photoFile.absolutePath)
            val imgsJson = imgUpload.toJson() ?: return@withContext false
            val vLoc = PhotoLocationDiskCache.getInstance(Utils.getApp()).getPhotoLocFromDiskCache(photoFile.name)
            if (null != vLoc) {
                AR110Log.i(TAG, "has get MediaLocationData")
                params["gpsTime"] = vLoc.record_time
                params["altitude"] = vLoc.altitude
                params["latitude"] = vLoc.latitude
                params["longitude"] = vLoc.longitude
            } else {
                AR110Log.i(TAG, "can not get MediaLocationData")
                val time = photoFile.lastModified()
                val recordTime = time.toString()
                params["gpsTime"] = recordTime
            }
            val result = ar110Api.uploadPhoto(url, params, imgsJson) ?: return@withContext false
            AR110Log.i(TAG, "photo upload result: ${result.isSuccess}")
            return@withContext result.isSuccess
        }
        return@withContext false
    }

    private suspend fun loadLocalPhotoList(mCjdbh: String): MutableList<MyPhotoInfo> = withContext(Dispatchers.IO) {
        val mDataList = mutableListOf<MyPhotoInfo>()
        val videoPath = Util.getPhotoRootDir(mCjdbh)
        val videoFolder = File(videoPath)
        if (videoFolder.exists() && videoFolder.isDirectory) {
            val fileArray = videoFolder.listFiles()
            val len = fileArray.size
            if (len > 0) {
                for (i in 0 until len) {
                    val videoFile = fileArray[i]
                    if (videoFile.exists() && videoFile.isFile) {
                        if (videoFile.name.endsWith(".jpg")) {
                            mDataList.add(MyPhotoInfo(videoFile.absolutePath, videoFile.name, Util.VIDEO_NOT_UPLOADED))
                        }
                    }
                }
                if (mDataList.size > 0) {
                    mDataList.sortByDescending {
                        it.mPhotoName
                    }
                }
            }
        }
        return@withContext mDataList
    }
}