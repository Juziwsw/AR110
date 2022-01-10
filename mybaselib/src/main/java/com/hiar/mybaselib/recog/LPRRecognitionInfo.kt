package com.hiar.mybaselib.recog

import android.graphics.Rect

/**
 *
 * @author wilson
 * @date 26/10/2021
 * Email: haiqin.chen@hiscene.com
 * 车牌检测结果
 */
data class LPRRecognitionInfo(
    /**
     * 车牌小图在
     */
    val box: Rect? = null,

    /**
     * 车牌信息
     * 0:车牌字符串 苏E991N1
     * 1：车牌的颜色 蓝
     * 4：识别结果最低准确率
     */
    val result: Array<String?>? =null,

    /**
     * 原始图
     */
    val imageData: ByteArray? = null,

    /**
     * 车牌小图
     */
    val carData: ByteArray? = null,

    /**
     * 检测时长
     */
    val recognitionTime: Long = 0L,
)
