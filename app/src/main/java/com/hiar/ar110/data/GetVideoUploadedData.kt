package com.hiar.ar110.data

/**
 * Author:wilson.chen
 * date：5/19/21
 * desc：
 */
public data class GetVideoUploadedData(
        val from: Int?,
        val imageUrl: String?,
        val limit: Int?,
        val order: String?,
        val sort: String?,
        val total: Int?,
        val videoList: MutableList<Video>?,
        val videoUrl: String?
)

public data class Video(
        val altitude: String? = null,
        val bearing: String? = null,
        val cjdbh: String? = null,
        val cjdbh2: String? = null,
        val createTime: String? = null,
        val deviceId: String? = null,
        val gpsTime: String? = null,
        val id: Int,
        val isUpload: Int = 0,
        val jjdbh: String? = null,
        val jybh: String? = null,
        val jygh: String? = null,
        val jyxm: String? = null,
        val lastLocation: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val name: String,
        val speed: String? = null,
        val thumbnail: String? = null,
        val videoUrl: String? = null
)