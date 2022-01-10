package com.hiar.ar110.viewmodel

import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.HttpResult
import com.hiar.ar110.data.UploadLogResult
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.util.*


/**
 *
 * @author xuchengtang
 * @date 28/05/2021
 * Email: xucheng.tang@hiscene.com
 */
class SettingsViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    val uploadLogResult = EventMutableLiveData<UploadLogResult<Any>>()

    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }

    suspend fun uploadLog(videoFile: File, name:String, pos: Int) {
        kotlin.runCatching {
            val fileRQ: RequestBody = RequestBody.create(null, videoFile)
            val part: MultipartBody.Part  = MultipartBody.Part.createFormData("file", videoFile.name,fileRQ);
            val map: MutableMap<String, String> = HashMap()
            map["imei"] = Util.getAndroidID()

            val result = ar110Api.uploadLog(map,part) ?: return@uploadLog
            result.name = name
            result.position = pos
            uploadLogResult.postValue(result)
            AR110Log.i(TAG, "post res=$result  file=$videoFile")
        }.onFailure {
            AR110Log.e(TAG, it.message)
        }
    }
}

