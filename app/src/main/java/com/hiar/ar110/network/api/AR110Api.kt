package com.hiar.ar110.network.api

import com.hiar.ar110.config.AppConfigBean
import com.hiar.ar110.config.ModuleConfig
import com.hiar.ar110.data.*
import com.hiar.ar110.data.audio.AudioListData
import com.hiar.ar110.data.cop.CopData
import com.hiar.ar110.data.cop.CopTaskData
import com.hiar.ar110.data.cop.FaceListData
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.login.LoginBean
import com.hiar.ar110.data.people.FaceHisData
import com.hiar.ar110.data.people.PeopleHisResult
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.data.photo.SceneImageList
import com.hiar.ar110.data.vehicle.NodeInfo
import com.hiar.ar110.data.vehicle.VehicleInfo
import com.hiar.ar110.data.vehicle.VehicleRecord
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
interface AR110Api {
    /**
     * 获取警单数量
     */
    @POST("/api/police/v0.1/record/censusCount")
    suspend fun getCopTaskNumber(@Body data: CopTaskNumberReq): HttpResult<TaskNumberDetail>?

    /**
     * 获取巡逻单数量
     */
    @POST("/api/police/v0.1/patrol/count")
    suspend fun refreshPatrolTaskNumber(@Body data: PatrolTaskNumberReq): HttpResult<TaskNumberDetail>?

    /**
     * 获取人口核查单数量
     */
    @POST("/api/police/v0.1/verification/count")
    suspend fun refreshVerificationTaskNumber(@Body data: PatrolTaskNumberReq): HttpResult<TaskNumberDetail>?


    /**
     * 获取时间段内的警单
     */
    @POST("/api/police/v0.1/record/getCjdbh2List")
    suspend fun getRecentCopTasks(@Body data: Map<String, String>): HttpResult<JdListData>?

    /**
     * 获取时间段内的巡逻单
     */
    @POST("/api/police/v0.1/patrol/getPatroNumList")
    suspend fun getRecentPatrolTasks(@Body data: Map<String, String>): HttpResult<JdListData>?


    /**
     *  警单模式， 获取视频上传列表和上传状态
     */
    @POST("/api/police/v0.1/live/videoList")
    suspend fun getCopVideoStatus(@QueryMap data: Map<String, String>): HttpResult<GetVideoUploadedData>?

    /**
     *  巡逻模式,   查询视频记录
     */
    @POST("/api/police/v0.1/patrol/video/search")
    suspend fun getPatrolVideoState(@QueryMap data: Map<String, String>): HttpResult<GetVideoUploadedData>?


    /**
     *  警单模式， 获取图片上传列表和上传状态
     */
    @POST("/api/police/v0.1/live/sceneImageList")
    suspend fun getCopPhotoStatus(@QueryMap data: Map<String, String>): HttpResult<SceneImageList>

    /**
     *  巡逻模式查询图片记录
     */
    @POST("/api/police/v0.1/patrol/sceneImage/search")
    suspend fun getPatrolPhotoState(@QueryMap data: Map<String, String>): HttpResult<SceneImageList>

    /**
     *  创建巡逻单
     */
    @POST("/api/police/v0.1/patrol/create")
    suspend fun createPatrolTask(@QueryMap data: Map<String, String>): HttpResult<PatrolRecord>?

    /**
     *  修改巡逻单
     */
    @POST("/api/police/v0.1/patrol/changeStatus")
    suspend fun modifyPatrolState(@QueryMap data: Map<String, String>): HttpResult<PatrolRecord>?

    /**
     *  获取巡逻单列表
     */
    @POST("/api/police/v0.1/patrol/search")
    suspend fun getPatrolTaskRecord(@QueryMap data: Map<String, String>): HttpResult<ResultData>?


    /**
     *  上传警单视频
     */
    @POST("/api/police/v0.1/live/addVideo")
    @Multipart
    suspend fun upLoadCopVideo(@Part data: List<MultipartBody.Part>): HttpResult<Any>?

    /**
     *  上传巡逻单视频
     */
    @POST("/api/police/v0.1/patrol/video/add")
    @Multipart
    suspend fun upLoadPatrolVideo(@Part data: List<MultipartBody.Part>): HttpResult<Any>?


    /**
     *  警单/巡逻最新人脸记录查询
     */
    @POST
    suspend fun getFaceList(
        @Url url: String,
        @QueryMap data: Map<String, String>
    ): HttpResult<FaceListData>?

    /**
     *  警单/巡逻最新车牌记录查询
     */
    @POST
    suspend fun getCarList(
        @Url url: String,
        @QueryMap data: Map<String, String>
    ): HttpResult<VehicleRecord>?

    /**
     *  警单/巡逻上传拍照图片
     */
    @FormUrlEncoded
    @POST
    suspend fun uploadPhoto(
        @Url url: String,
        @QueryMap data: Map<String, String>,
        @Field("images") img: String
    ): HttpResult<Any>?

    /**
     *  警单/巡逻车牌识别
     */
    @Headers("Content-Type: application/json", "Accept: */*")//需要添加头
    @POST
    suspend fun recognizeCarPlate(
        @Url url: String,
        @Body route: RequestBody
    ): HttpResult<VehicleInfo>?

    /**
     *  创建群组
     */
    @Headers("Content-Type: application/json", "Accept: */*")//需要添加头
    @POST("/api/police/v0.1/group/create")
    suspend fun createGroup(@Body route: RequestBody): HttpResult<GroupInfo>?

    /**
     *  更新警单/巡逻状态
     */
    @POST
    suspend fun updateState(@Url url: String, @QueryMap data: Map<String, String>): HttpResult<Any>?

    @Multipart
    @POST("/api/police/v0.1/test/addFileLog")
    suspend fun uploadLog(
        @PartMap map: Map<String, String>,
        @Part file: MultipartBody.Part
    ): UploadLogResult<Any>?

    /**
     *  人脸识别
     */
    @Headers("Content-Type: application/json", "Accept: */*")//需要添加头
    @POST
    suspend fun faceRecognize(@Url url: String, @Body route: RequestBody): HttpResult<FaceCompData>?

    /**
     *  获取车牌列表
     */
    @POST
    suspend fun fetchCarRecord(
        @Url url: String,
        @Body data: RequestBody
    ): HttpResult<VehicleRecord>?

    /**
     *  addUserInfo
     */
    @POST("/api/police/v0.1/live/addAppUserInfo")
    suspend fun addUserInfo(@QueryMap data: Map<String, String>): HttpResult<UserIdInfo>?

    /**
     *  获取警单列表
     */
    @POST("/api/police/v0.1/record/searchCopTask")
    suspend fun getCopTaskRecord(@QueryMap data: Map<String, String>): HttpResult<CopTaskData>?

    /**
     *  获取人脸识别数据
     */
    @POST
    suspend fun fetchFaceHiscene(
        @Url url: String,
        @QueryMap map: Map<String, String>
    ): HttpResult<PeopleHisResult.FaceRecogHisData>?

    /**
     *  获取人脸识别数据
     */
    @POST
    suspend fun fetchFaceHisData(
        @Url url: String,
        @QueryMap map: Map<String, String>
    ): HttpResult<FaceHisData>?

    /**
     *  同步新警单
     */
    @POST("/api/police/v0.1/live/syncCopTask")
    suspend fun syncCopTask(@QueryMap map: Map<String, String>): HttpResult<CopData>?

    /**
     *  获取账号信息token和emergencyId
     */
    @POST("/api/police/v0.1/token/getToken")
    suspend fun getToken(@QueryMap map: Map<String, String>): HttpResult<AccountData>?

    /**
     *  上传GPS位置信息
     */
    @POST
    suspend fun updateLocation(@Url url: String, @Body data: RequestBody): HttpResult<NodeInfo>?

    /**
     *  获取音频列表
     */
    @POST("/api/police/v0.1/live/audioList")
    suspend fun getAudioList(@QueryMap map: Map<String, String>): HttpResult<AudioListData>?

    /**
     *  巡逻模式获取音频列表
     */
    @POST("/api/police/v0.1/patrol/audio/search")
    suspend fun getPatrolAudioList(@QueryMap map: Map<String, String>): HttpResult<AudioListData>?


    /**
     *  创建人口核查
     */
    @POST("/api/police/v0.1/verification/create")
    suspend fun createPopulationTask(@QueryMap data: Map<String, String>): HttpResult<PopulationRecord>?

    /**
     *  修改人口核查单状态
     */
    @POST("/api/police/v0.1/verification/changeStatus")
    suspend fun modifyPopulationState(@QueryMap data: Map<String, String>): HttpResult<Any?>

    /**
     *  获取人口核查列表
     */
    @POST("/api/police/v0.1/verification/search")
    suspend fun getPopulationTaskRecord(@QueryMap data: Map<String, String>): HttpResult<PopulationResultData>?

    /**
     *  模块配置
     */
    @GET("/api/police/v0.1/env/moduleList")
    suspend fun getModuleConfig(): HttpResult<ModuleConfig.ModuleConfigBean>?

    /**
     *  模块配置
     */
    @GET("/api/police/v0.1/env/app")
    suspend fun getAppConfig(): HttpResult<AppConfigBean>?


    /**
     *  登录
     */
    @Headers("Content-Type: application/json")
    @POST("/api/police/v0.1/thirdParty/teLogin")
    suspend fun loginToken1(@Body route: RequestBody): LoginBean
}
