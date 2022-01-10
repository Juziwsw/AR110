package com.hiar.ar110.viewmodel

import android.graphics.ImageFormat
import android.graphics.YuvImage
import com.hiar.ar110.BuildConfig
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.FaceInfoList
import com.hiar.ar110.data.FaceSendData
import com.hiar.ar110.data.HttpResult
import com.hiar.ar110.data.patrol.PopulationFaceRequest
import com.hiar.ar110.data.people.FaceCompareBaseInfo
import com.hiar.ar110.data.people.FaceHisData
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.extension.toJson
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.ar110.util.genFaceIcons
import com.hiar.mybaselib.recog.FaceRecognitionInfo
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.java.KoinJavaComponent.inject


/**
 *
 * @author xuchengtang
 * @date 28/05/2021
 * Email: xucheng.tang@hiscene.com
 */
class PopulationResultViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    val peopleHisResult = EventMutableLiveData<FaceHisData>()
    val mFaceRecognizerAdd = EventMutableLiveData<MutableList<FaceCompareBaseInfo>>()
    val mFaceUnRecognizerAdd = EventMutableLiveData<MutableList<FaceCompareBaseInfo>>()
    val modifyState = EventMutableLiveData<HttpResult<Any?>>()
    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }

    fun modifyState(id: String) {
        val params = HashMap<String, String>()
        params.put("id", id)
        params.put("endTime", System.currentTimeMillis().toString())
        requestWithoutExecute({
            ar110Api.modifyPopulationState(params)
        },{
            modifyState.postValue(it)
        },{
            httpErrorMessage.postValue(it)
        })
    }

    fun fetchFaceHiscene(populationRecord: PopulationRecord?) {
        AR110Log.i(TAG, "fetchFaceHiscene  mPatrolRecord=$populationRecord")
        if (null == populationRecord) {
            return
        }
        val map = HashMap<String, String>()
        var url = "/api/police/v0.1/verification/face/searchGroup"
        map["verificationNumber"] = populationRecord.number ?: ""
        map["account"] = populationRecord.account ?: ""
        request({
            ar110Api.fetchFaceHisData(url, map)
        }, {
            peopleHisResult.postValue(it)
        }, {
            httpErrorMessage.postValue(it)
        })
    }

    fun faceRequest(populationRecord: PopulationRecord?,data: ByteArray, width: Int, height: Int, faceList: List<FaceRecognitionInfo>) {
        val faces = mutableListOf<FaceRecognitionInfo>()
        faces.addAll(faceList)
        val sendData = FaceSendData()
        val image = YuvImage(data, ImageFormat.NV21, width, height, null)
        val sendDataPatrol = PopulationFaceRequest()
        populationRecord?.let {
            val addFaceUrl = "/api/police/v0.1/verification/face/add"
            sendDataPatrol.verificationNumber = it.number
            sendDataPatrol.gpsTime = System.currentTimeMillis().toString()
            val mLocData = AR110BaseService.instance!!.currentLoc
            if (mLocData != null) {
                sendDataPatrol.bearing = mLocData.bearing
                sendDataPatrol.latitude = mLocData.latitude
                sendDataPatrol.longitude = mLocData.longitude
                sendDataPatrol.altitude = mLocData.altitude
                sendDataPatrol.lastLocation = mLocData.last_location
                sendDataPatrol.imei = mLocData.imei
                sendDataPatrol.imsi = mLocData.imsi
                sendDataPatrol.deviceId = mLocData.imei
            } else {
                sendDataPatrol.latitude = ""
                sendDataPatrol.longitude = ""
                sendDataPatrol.altitude = ""
                sendDataPatrol.imei = Util.getAndroidID()
                sendDataPatrol.imsi = sendDataPatrol.imei
                sendDataPatrol.bearing = ""
                sendDataPatrol.lastLocation = ""
                sendDataPatrol.deviceId = sendDataPatrol.imei
            }
            sendDataPatrol.jybh = AR110BaseService.mUserInfo!!.account
            sendDataPatrol.jyxm = AR110BaseService.mUserInfo!!.name
            sendDataPatrol.minSimilarity = (Util.DEF_FACE_THRESHOOD / 100f).toString()
            sendDataPatrol.maxResults = 1
            if (BuildConfig.DEBUG) {  //亮风台算法
                sendDataPatrol.recoType = 1
            } else {
                sendDataPatrol.recoType = 2 //明略海康算法
            }
            sendDataPatrol.images = genFaceIcons(faces, image, width, height)
            val sendStr = sendDataPatrol.toJson()
            val requestBody = sendStr!!.toRequestBody("application/json".toMediaTypeOrNull())
            request({
                ar110Api.faceRecognize(addFaceUrl, requestBody)
            }, {
                it?.faceInfoList?.let { it1 -> transferFaceData(it1) }
            }, {

            })
        }
    }

    private fun transferFaceData(faceCompData: MutableMap<String, Array<FaceInfoList>>) {
        faceCompData.forEach {
            val faceInfoList =it.value.mapNotNull {
                FaceCompareBaseInfo(it)
            }.toMutableList()
            val data =faceInfoList[0]
            if (data.cardId.isNullOrEmpty()){
                mFaceUnRecognizerAdd.postValue(faceInfoList)
            }else{
                mFaceRecognizerAdd.postValue(faceInfoList)
            }
        }
    }
}

