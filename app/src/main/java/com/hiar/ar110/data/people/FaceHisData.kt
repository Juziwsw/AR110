package com.hiar.ar110.data.people

/**
 * Author:wilson.chen
 * date：6/25/21
 * desc：
 */
data class FaceHisData (
        val imageUrl:String,
        var recoFace :MutableList<MutableList<FaceCompareBaseInfo>>?,
        var unrecoFace :MutableList<MutableList<FaceCompareBaseInfo>>?,
)