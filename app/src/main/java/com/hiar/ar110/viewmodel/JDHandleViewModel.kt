package com.hiar.ar110.viewmodel

import android.graphics.*
import android.os.SystemClock
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.Utils
import com.google.gson.Gson
import com.hiar.ar110.BuildConfig
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.*
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.patrol.CarPatrolAddReqData
import com.hiar.ar110.data.patrol.PatrolFaceRequest
import com.hiar.ar110.data.photo.PhotoUploadReq
import com.hiar.ar110.data.photo.SceneImage
import com.hiar.ar110.data.vehicle.CarRequestData
import com.hiar.ar110.data.vehicle.RecoRecog
import com.hiar.ar110.data.vehicle.VehicleInfo
import com.hiar.ar110.data.vehicle.VehicleRecogList
import com.hiar.ar110.diskcache.PhotoLocationDiskCache
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Base64Util
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.recog.FaceRecognitionInfo
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.BitmapUtils
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.koin.java.KoinJavaComponent
import java.io.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

/**
 * author: liwf
 * date: 2021/5/18 14:35
 */
class JDHandleViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
    val gson = Gson()
    val PLATE_RECOG_INTERVAL = 5 * 1000
    private var mLastPlateRes: LastPlateRecogRes? = null

    data class FetchCarResult(val path: String?, val carInfo: VehicleRecogList?, val plateNum: String?)
    data class PlateResult(val name: String?, val info: VehicleInfo?, val plateNum: String)
    data class LastPlateRecogRes(var plateNum: String?, var genTime: Long)

    val mFaceRecognizerLiveData = EventMutableLiveData<HttpResult<FaceCompData>>()

    companion object {
        const val MAX_ITEM_NUM = 20
        const val TOTAL_FETCH_LIMIT = 10 * MAX_ITEM_NUM
    }

    override fun onNewMessage(p0: Int, p1: MultVal?) {

    }

    //获取历史人脸识别信息
    fun fetchFaceList(mCopTask: CopTaskRecord?, mPatrolTask: PatrolRecord?): Flow<TargetInfo> {
        return flow {
            AR110Log.i(TAG, "fetchFaceList")
            var startReadPos = 0
            var needContinue = false
            do {
                val params = HashMap<String, String>()
                lateinit var url: String
                mCopTask?.let {
                    url = Util.mFaceListUrl //"/api/police/v0.1/live/faceList"
                    params["cjdbh"] = mCopTask.cjdbh
                    params["cjdbh2"] = mCopTask.cjdbh2
                    params["jybh"] = AR110BaseService.mUserInfo!!.account
                    params["deviceId"] = Util.getAndroidID()
                    params["from"] = "" + startReadPos
                    params["limit"] = "" + MAX_ITEM_NUM
                    AR110Log.i(TAG, "fetch facelist params=$params")
                }
                mPatrolTask?.let {
                    url = Util.mPatrolFaceSearchUrl//"/api/police/v0.1/patrol/face/search"
                    params["patrolNumber"] = mPatrolTask.number
                    params["account"] = AR110BaseService.mUserInfo!!.account
                    params["from"] = "" + startReadPos
                    params["limit"] = "" + MAX_ITEM_NUM
                    AR110Log.i(TAG, "fetch facelist params=$params")
                }
                val content = ar110Api.getFaceList(url, params)
                var fetchLen = 0
                needContinue = true
                content?.let {
                    if (content.isSuccess && content.data?.faceList != null && content.data!!.faceList.isNotEmpty()) {
                        Util.mFacePhotoUrlHead = content.data!!.imageUrl
                        fetchLen = content.data!!.faceList.size
                        for (i in 0 until fetchLen) {
                            val item = content.data!!.faceList[i]
                            var conf = 0.1f
                            if (item.similarity != null && !TextUtils.isEmpty(item.similarity)) {
                                conf = item.similarity.toFloat()
                                if (conf <= 1.0f) {
                                    conf *= 100.0f
                                }
                                conf = conf.roundToInt().toFloat()
                            }
                            if (item.faceUrl.isEmpty()) {
                                continue
                            }
                            AR110Log.i(TAG, "item.name=" + item.name + "faceUrl=" + item.faceUrl)
                            if (!item.name.startsWith("http")) {
                                item.name = Util.getPeoplePhotoIpHead() + "/" + item.name
                            }
                            if (!BuildConfig.DEBUG) {
                                item.faceUrl = item.faceUrl.replace("http://41.204.238.137:6120", Util.mPoliceHeadLibUrl)
                            }
                            AR110Log.i(TAG, "after add head item.name=" + item.name + "faceUrl=" + item.faceUrl)
                            item.gpsTime = item.gpsTime.substring(0, item.gpsTime.indexOf("+"))
                            item.gpsTime = item.gpsTime.replace("T".toRegex(), " ")
                            item.gpsTime = item.gpsTime.replace("-".toRegex(), "/")
                            val info = TargetInfo(item.faceName, item.faceUrl, item.cardId,
                                    item.id.toLong(), "$conf%", item.name, item.gpsTime,
                                    item.labelName, item.labelCode)
                            emit(info)
                            needContinue = false
                        }
                    } else {
                        needContinue = false
                    }
                }
                startReadPos += fetchLen
                if (fetchLen < MAX_ITEM_NUM) {
                    needContinue = false
                }
                if (startReadPos > TOTAL_FETCH_LIMIT) {
                    needContinue = false
                }
            } while (needContinue)
            AR110Log.i(TAG, "total face record num=$startReadPos")
        }
    }

    //获取历史车牌识别信息
    suspend fun fetchCarRecord(copTask: CopTaskRecord?, patrolTask: PatrolRecord?): FetchCarResult? {
        AR110Log.i(TAG, "fetchCarRecord")
        var fetchCarResult: FetchCarResult? = null
        withContext(Dispatchers.Default) {
            val mVehicleInfoList = mutableListOf<VehicleRecogList>()
            val mVehicleNotInLibList = mutableListOf<RecoRecog>()
            var mNotInLibNum: Int
            var mInLibNum: Int
            val params = HashMap<String, String>()
            lateinit var url: String
            copTask?.let {
                url = Util.mCarListUrl//"/api/police/v0.1/live/carList"
                params["cjdbh2"] = copTask!!.cjdbh2
                params["sort"] = "desc"
                params["account"] = AR110BaseService.mUserInfo!!.account
            }
            patrolTask?.let {
                url =  Util.mPatrolCarListUrl//"/api/police/v0.1/patrol/car/search"
                params["patrolNumber"] = patrolTask!!.number
                params["sort"] = "desc"
                params["account"] = AR110BaseService.mUserInfo!!.account
            }
            kotlin.runCatching {
                val vResult = ar110Api.getCarList(url, params)
                vResult?.let {
                    if ((vResult.data != null) && vResult.data!!.carRecoList != null && vResult.data!!.carRecoList.isNotEmpty()) {
                        Util.mCarPhotoUrlHead = vResult.data!!.imageUrl
                        mVehicleInfoList.clear()
                        mVehicleNotInLibList.clear()
                        mNotInLibNum = 0
                        mInLibNum = 0
                        val len = vResult.data!!.carRecoList.size
                        for (i in 0 until len) {
                            val item = vResult.data!!.carRecoList[i]
                            if (null != item) {
                                if (item.recoStatus == Util.RECOG_STATUS_INLIB) {
                                    mInLibNum++
                                    mVehicleInfoList.add(item)
                                    break
                                } else if (item.recoStatus == Util.RECOG_STATUS_NOTINLIB) {
                                    if (item.recoRecord != null && item.recoRecord.isNotEmpty()) {
                                        val itemNum = item.recoRecord.size
                                        mNotInLibNum++
                                        if (mNotInLibNum > 1) {
                                            continue
                                        }
                                        for (j in 0 until itemNum) {
                                            mVehicleNotInLibList.add(item.recoRecord[j])
                                        }
                                    }
                                }
                            }
                        }
                        if (mInLibNum > 0) {
                            val info: VehicleRecogList = mVehicleInfoList[0]
                            fetchCarResult = FetchCarResult(info.recoRecord[0].url, info, info.recoRecord[0].carId)
                        } else if (mNotInLibNum > 0) {
                            val recog: RecoRecog = mVehicleNotInLibList[0]
                            fetchCarResult = FetchCarResult(recog.url, null, recog.carId)
                        }
                    }
                }
            }.onFailure {
                AR110Log.e(TAG, "fetchCarRecord onfail it=${it.message} ")
            }
        }
        AR110Log.i(TAG, "fetchCarRecord result ")
        return fetchCarResult
    }

    //获取历史图片
    fun fetchPhotoRecord(copTask: CopTaskRecord?, patrolTask: PatrolRecord?): String? {
        AR110Log.i(TAG, "fetchPhotoRecord")
        var imagePath: String? = null
        var photoDir: String? = null
        copTask?.let {
            photoDir = Util.getPhotoRootDir(copTask.cjdbh2)
        }
        patrolTask?.let {
            photoDir = Util.getPhotoRootDir(patrolTask.number)
        }
        val folder = File(photoDir)
        if (!folder.exists()) {
            return imagePath
        }
        val fileArray = folder.list { _: File?, name: String -> name.endsWith(".jpg") }
                ?: return imagePath
        if (fileArray.isEmpty()) {
            return imagePath
        }
        fileArray.sortWith(Comparator { o1: String, o2: String? -> -o1.compareTo((o2)!!) })
        imagePath = photoDir + fileArray[0]
        return imagePath
    }

    suspend fun tackPicture(data: ByteArray?, width: Int, height: Int, copTask: CopTaskRecord?, patrolTask: PatrolRecord?): File? {
        var pictureFile: File? = null
        withContext(Dispatchers.IO) {
            var photoRoot: String? = null
            copTask?.let {
                photoRoot = Util.getPhotoRootDir(copTask.cjdbh2)
            }
            patrolTask?.let {
                photoRoot = Util.getPhotoRootDir(patrolTask.number)
            }
            val fileName = photoRoot + Util.formatDate() + ".jpg"
            pictureFile = File(fileName)
            try {
                pictureFile!!.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val imgRect = Rect(0, 0, width, height)
            val baos = ByteArrayOutputStream()
            val image1 = YuvImage(data, ImageFormat.NV21, width, height, null)
            image1.compressToJpeg(imgRect, 100, baos)
            var filecon: FileOutputStream? = null
            try {
                filecon = FileOutputStream(pictureFile)
                baos.writeTo(filecon)
                filecon.close()
                //图片文件关联GPS信息
                val videoLoc = MediaLocationData()
                videoLoc.fileName = pictureFile!!.name
                videoLoc.record_time = System.currentTimeMillis().toString()
                val mLocData = AR110BaseService.instance!!.currentLoc
                if (mLocData != null) {
                    videoLoc.altitude = mLocData.altitude
                    videoLoc.latitude = mLocData.latitude
                    videoLoc.longitude = mLocData.longitude
                } else {
                    AR110Log.i(TAG, "beginRecordVideo no location get !!!")
                }
                PhotoLocationDiskCache.getInstance(Utils.getApp()).addPhotoLocation(videoLoc)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e1: IOException) {
                e1.printStackTrace()
                try {
                    baos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return pictureFile
    }

    suspend fun newPlateHandle(carData: ByteArray?, width: Int, height: Int, plateNum: String?, plateColor: String?, copTask: CopTaskRecord?, patrolTask: PatrolRecord?): PlateResult? {
        var platResult: PlateResult? = null
        withContext(Dispatchers.IO) {
            if (plateNum == null) {
                return@withContext
            }
            if (mLastPlateRes != null) {
                if (mLastPlateRes!!.plateNum != null && (mLastPlateRes!!.plateNum == plateNum)) {
                    val curTime = SystemClock.elapsedRealtime()
                    val timePast = curTime - mLastPlateRes!!.genTime
                    if (timePast < PLATE_RECOG_INTERVAL) {
                        return@withContext
                    }
                }
            } else {
                mLastPlateRes = LastPlateRecogRes(plateNum, SystemClock.elapsedRealtime())
            }
            mLastPlateRes!!.plateNum = plateNum
            mLastPlateRes!!.genTime = SystemClock.elapsedRealtime()
            var catchFolder: File? = null
            copTask?.let {
                catchFolder = File("/sdcard/.jwt/com.hiar.ar110/" + copTask.cjdbh2 + "/" + plateNum)
            }
            patrolTask?.let {
                catchFolder = File("/sdcard/.jwt/com.hiar.ar110/" + patrolTask.number + "/" + plateNum)
            }
            catchFolder?.let {
                if (!catchFolder!!.exists()) {
                    catchFolder!!.mkdirs()
                }
            }
            val fileName1 = catchFolder.toString() + "/" + Util.formatDate() + ".jpg"
            val pictureFile1 = File(fileName1)
            try {
                pictureFile1.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val imgRect = Rect(0, 0, width, height)
            val baos1 = ByteArrayOutputStream()
            val image1 = YuvImage(carData, ImageFormat.NV21, width, height, null)
            image1.compressToJpeg(imgRect, 100, baos1)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inSampleSize = 2
            val bmp = BitmapFactory.decodeByteArray(baos1.toByteArray(), 0, baos1.size(), options)
            var filecon1: FileOutputStream? = null
            try {
                filecon1 = FileOutputStream(pictureFile1)
                bmp?.compress(Bitmap.CompressFormat.JPEG, 90, filecon1)
                filecon1.close()
                bmp!!.recycle()
                lateinit var url: String
                if (copTask != null) {
                    url = Util.mAddCarRecogUrl//"/api/police/v0.1/live/addCarReco"
                } else if (patrolTask != null) {
                    url = Util.mPatrolCarAdd//"/api/police/v0.1/patrol/car/add"
                }
                var carReq: CarRequestData? = null
                var carPatrolReq: CarPatrolAddReqData? = null
                val mLocData = AR110BaseService.instance!!.currentLoc
                if (copTask != null) {
                    carReq = CarRequestData(copTask, mLocData, AR110BaseService.mUserInfo, fileName1, plateNum, plateColor)
                } else if (patrolTask != null) {
                    carPatrolReq = CarPatrolAddReqData(patrolTask, mLocData, AR110BaseService.mUserInfo, fileName1, plateNum, plateColor)
                }
                var sendStr = ""
                if (carReq != null) {
                    sendStr = gson.toJson(carReq)
                } else if (carPatrolReq != null) {
                    sendStr = gson.toJson(carPatrolReq)
                }
                val requestBody = sendStr.toRequestBody("application/json".toMediaTypeOrNull())
                kotlin.runCatching {
                    val reqRes = ar110Api.recognizeCarPlate(url, requestBody)
                    reqRes?.let {
                        val info = reqRes.data
                        platResult = if (info != null) {
                            AR110Log.i("vehicle", "get vehicle info")
                            PlateResult(fileName1, info, plateNum)
                        } else {
                            AR110Log.i("vehicle", "can not get vehicle info")
                            PlateResult(fileName1, null, plateNum)
                        }
                    }
                }.onFailure {
                    AR110Log.e("vehicle", "request recognizeCarPlate onfail ${it.message}")
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                AR110Log.i("vehicle", "request vehicle onFinished$e")
            } catch (e1: IOException) {
                AR110Log.i("vehicle", "request vehicle onFinished$e1")
                e1.printStackTrace()
                if (null != baos1) {
                    try {
                        baos1.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return platResult
    }

    //上传单张图片
    suspend fun sendSinglePhoto(photoFile: File, copTask: CopTaskRecord?, patrolTask: PatrolRecord?) {
        withContext(Dispatchers.IO) {
            val upData = PhotoUploadReq()
            copTask?.let {
                upData.cjdbh = copTask.cjdbh
                upData.cjdbh2 = copTask.cjdbh2
                upData.deviceId = Util.getIMEI(1)
                upData.jjdbh = copTask.jjdbh
                upData.copNum = copTask.cops[0].copNum
            }
            patrolTask?.let {
                upData.patrolNumber = patrolTask.number
                upData.deviceId = Util.getIMEI(1)
            }
            upData.jybh = AR110BaseService.mUserInfo!!.account
            upData.jyxm = AR110BaseService.mUserInfo!!.name
            upData.jygh = upData.jybh
            upData.imei = Util.getIMEI(0)
            upData.imsi = upData.imei
            val params = HashMap<String, String>()
            lateinit var url: String
            copTask?.let {
                url = Util.mPhotoUploadUrl//"/api/police/v0.1/live/addSceneImage"
                params["jjdbh"] = copTask.jjdbh
                params["cjdbh"] = copTask.cjdbh
                params["cjdbh2"] = copTask.cjdbh2
                params["deviceId"] = Util.getAndroidID()
                params["jybh"] = AR110BaseService.mUserInfo!!.account
                params["jygh"] = AR110BaseService.mUserInfo!!.account
                params["jyxm"] = AR110BaseService.mUserInfo!!.name
                params["imei"] = Util.getIMEI(0)
                params["imsi"] = Util.getIMEI(0)
            }
            patrolTask?.let {
                url = Util.mPatrolPhotoUploadUrl //"/api/police/v0.1/patrol/sceneImage/add"
                params["patrolNumber"] = upData.patrolNumber
                params["deviceId"] = upData.deviceId
                params["jybh"] = upData.jybh
                params["jygh"] = upData.jygh
                params["jyxm"] = upData.jyxm
                params["imei"] = upData.imei
                params["imsi"] = upData.imsi
            }
            val imgUpload = SceneImage()
            imgUpload.sceneId = photoFile.name
            imgUpload.sceneImage = Base64Util.imageToBase64ByLocal(photoFile.absolutePath)
            val imgsJson = gson.toJson(imgUpload)
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
            requestWithoutExecute({
                ar110Api.uploadPhoto(url, params, imgsJson)
            },{
                AR110Log.i(TAG, "photo upload result: ${it?.isSuccess}")
            },{
                AR110Log.i(TAG, "photo upload onfail: ${it.message}")
            })

        }
    }

    suspend fun createGroup(jdNumber: String): String {
        AR110Log.i(TAG, "createGroup")
        var groupId = ""
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
            val request = JSONObject() //服务器需要传参的json对象
            request.put("cjdbh2", jdNumber) //根据实际需求添加相应键值对
            val jsonString = request.toString()
            AR110Log.i(TAG, "createGroup $jsonString")
            val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
            //接口对象实例调用相关接口，获得Observable对象
            val result = ar110Api.createGroup(requestBody)
                result?.let {
                    AR110Log.i(TAG, "createGroup result: ${result.data!!.groupId} ${result.retCode}")
                    groupId = result.data!!.groupId.toString()
                }
            }.onFailure {
                AR110Log.e(TAG, "createGroup onfail ${it.message}")
            }
        }
        return groupId
    }

    //更新警单/巡逻单状态
    suspend fun updateState(copTask: CopTaskRecord?, patrolTask: PatrolRecord?, state: Int = 0): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            val params = HashMap<String, String>()
            lateinit var url: String
            copTask?.let {
                url = Util.mModifyStateUrl//"/api/police/v0.1/record/changeCopTask"
                params["cjdbh2"] = copTask.cjdbh2
                params.put("cjzt2", state.toString())
            }
            patrolTask?.let {
                url = Util.mModifyPatrolStateUrl//"/api/police/v0.1/patrol/changeStatus"
                params["id"] = patrolTask.id.toString() + ""
                params.put("endTime", System.currentTimeMillis().toString() + "")
            }
            val httpResult = ar110Api.updateState(url, params) ?: return@withContext
            result = httpResult.isSuccess
        }
        return result
    }

    //人脸识别
    fun faceRequest(copTask: CopTaskRecord?, patrolTask: PatrolRecord?, data: ByteArray, width: Int, height: Int, faceList: List<FaceRecognitionInfo>) {
        viewModelScope.launch(Dispatchers.Default) {
            val faces = mutableListOf<FaceRecognitionInfo>()
            faces.addAll(faceList)
            val sendData = FaceSendData()
            val sendDataPatrol = PatrolFaceRequest()
            var addFaceUrl: String? = null
            val image = YuvImage(data, ImageFormat.NV21, width, height, null)
            var sendStr: String? = null
            copTask?.let {
                addFaceUrl = Util.mFaceUploadUrl//"/api/police/v0.1/live/addFace"
                sendData.jjdbh = copTask.jjdbh
                sendData.cjdbh = copTask.cjdbh
                sendData.cjdbh2 = copTask.cjdbh2
                sendData.gpsTime = System.currentTimeMillis().toString()
                val mLocData = AR110BaseService.instance!!.currentLoc
                if (mLocData != null) {
                    sendData.bearing = mLocData.bearing
                    sendData.latitude = mLocData.latitude
                    sendData.longitude = mLocData.longitude
                    sendData.altitude = mLocData.altitude
                    sendData.lastLocation = mLocData.last_location
                    sendData.imei = mLocData.imei
                    sendData.imsi = mLocData.imsi
                    sendData.deviceId = mLocData.imei
                } else {
                    sendData.latitude = ""
                    sendData.longitude = ""
                    sendData.altitude = ""
                    sendData.imei = Util.getAndroidID()
                    sendData.imsi = sendData.imei
                    sendData.bearing = ""
                    sendData.lastLocation = ""
                    sendData.deviceId = sendData.imei
                }
                sendData.jybh = AR110BaseService.mUserInfo!!.account
                sendData.jyxm = AR110BaseService.mUserInfo!!.name
                sendData.minSimilarity = (Util.DEF_FACE_THRESHOOD / 100f).toString()
                sendData.maxResults = 1
                if (BuildConfig.DEBUG) {  //亮风台算法
                    sendData.recoType = 1
                } else {
                    sendData.recoType = 2 //明略海康算法
                }
                sendData.faceType = 0
                sendData.images = genFaceIcons(faces, image, width, height)
                sendStr = gson.toJson(sendData)
            }
            patrolTask?.let {
                addFaceUrl = Util.mPatrolAddFace//"/api/police/v0.1/patrol/face/add"
                sendDataPatrol.patrolNumber = patrolTask.number
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
                sendStr = gson.toJson(sendDataPatrol)
            }
            val requestBody = sendStr!!.toRequestBody("application/json".toMediaTypeOrNull())
            requestWithoutExecute({
                ar110Api.faceRecognize(addFaceUrl!!, requestBody)
            },{
                mFaceRecognizerLiveData.postValue(it!!)
            },{
                AR110Log.i(TAG, "faceRecognize onfail ${it.message}")
            })
        }
    }

    // 增大人脸框
    private fun updateRect(rect: Rect, width: Int, height: Int): Rect {
        val roiWidth: Int = rect.right - rect.left
        val roiHeight: Int = rect.bottom - rect.top
        AR110Log.i(TAG, "runtime rect.left: ${rect.left},rect.right: ${rect.right},rect.top: ${rect.top},rect.bottom: ${rect.bottom}")
        rect.left -= (0.3 * roiWidth).toInt()
        rect.right += (0.3 * roiWidth).toInt()
        rect.top -= (0.45 * roiHeight).toInt()
        rect.bottom += (0.25 * roiHeight).toInt()
        if(rect.left>width||rect.right>width||rect.top>height||rect.bottom>height||rect.left<0||rect.right<0||rect.top<0||rect.bottom<0){
            AR110Log.e(TAG, "runtime updateRect error rect: ${rect},roiWidth=$roiWidth width=$width height=$height")
        }

        val roiFace = Rect()
        roiFace.left = if (rect.left > 0) rect.left else 0
        roiFace.left = if (roiFace.left > width) width-1 else roiFace.left

        roiFace.top = if (rect.top > 0) rect.top else 0
        roiFace.top = if (roiFace.top > height) height-1 else roiFace.top

        roiFace.right = if (rect.right < width) rect.right else width - 1
        roiFace.right = if (roiFace.right < 0) 0 else roiFace.right

        roiFace.bottom = if (rect.bottom < height) rect.bottom else height - 1
        roiFace.bottom = if (roiFace.bottom < 0) 0 else roiFace.bottom

        return roiFace
    }

    private fun genFaceIcons(faces: List<FaceRecognitionInfo>, image: YuvImage, width: Int, height: Int): Array<FaceIcon?> {
        val faceArray = arrayOfNulls<FaceIcon>(faces.size)
        for (index in faces.indices) {
            val baos = ByteArrayOutputStream()
            val calc = ByteArrayOutputStream()
            val faceId = faces[index].face_id.toString()
            AR110Log.i(TAG, "runtime faces[index].bbox: ${faces[index].bbox} ")
            val rect = updateRect(faces[index].bbox, width, height)
            AR110Log.i(TAG, "runtime rect: $rect  width=$width height$height")
            image.compressToJpeg(rect, 90, baos)
            var minEdge: Float = (rect.right - rect.left).coerceAtMost(rect.bottom - rect.top).toFloat()
            AR110Log.i(TAG, "minEdge: $minEdge")
            minEdge = 64 / minEdge
            AR110Log.i(TAG, "minEdge: $minEdge")
            if (1 < minEdge) {
                val imageBytes = baos.toByteArray()
                val imageR = BitmapFactory.decodeByteArray(imageBytes, 0,
                        imageBytes.size)
                if(imageR!=null) {
                    val scaleImg = BitmapUtils.getScaledBitmap(imageR, minEdge, minEdge)
                    scaleImg.compress(Bitmap.CompressFormat.JPEG, 90, calc)
                    val base64 = Base64Util.byteToBase64NoLine(calc.toByteArray())
                    faceArray[index] = FaceIcon(faceId, base64)
                }else{
                    AR110Log.e(TAG, "runtime imageR=null")
                    val base64 = Base64Util.byteToBase64NoLine(baos.toByteArray())
                    faceArray[index] = FaceIcon(faceId, base64)
                }
            } else {
                val base64 = Base64Util.byteToBase64NoLine(baos.toByteArray())
                faceArray[index] = FaceIcon(faceId, base64)
            }
        }
        return faceArray
    }
}