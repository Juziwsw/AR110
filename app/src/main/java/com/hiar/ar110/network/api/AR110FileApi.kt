package com.hiar.ar110.network.api

import com.hiar.ar110.data.HttpResult
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
interface AR110FileApi {

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
     *  警单/巡逻上传拍照图片
     */
    @FormUrlEncoded
    @POST
    suspend fun uploadPhoto(@Url url: String, @QueryMap data: Map<String, String>, @Field("images") img: String): HttpResult<Any>?

    /**
     *  上传Audio
     */
    @Multipart
    @POST  //("/api/police/v0.1/live/addAudio")
    suspend fun addAudio(@Url url: String,@Part data: List<MultipartBody.Part>): HttpResult<Any>?


//    /**
//     *  上传PatrolAudio
//     */
//    @Multipart
//    @POST("/api/police/v0.1/patrol/audio/add")
//    suspend fun addPatrolAudio(@Part data: List<MultipartBody.Part>):  HttpResult<Any>?
}