package com.hiar.mybaselib.recog

import android.graphics.Rect

/**
 *
 * @author wilson
 * @date 26/10/2021
 * Email: haiqin.chen@hiscene.com
 * 车牌检测结果
 */
data class FaceRecognitionInfo(
    @JvmField
    var bbox: Rect = Rect(0, 0, 0, 0),
    var frame_id: Long = 0L,
    var face_id: Long = 0L,
    var collectTime: Long = 0L,
    var faceTime: Long = 0L
)
