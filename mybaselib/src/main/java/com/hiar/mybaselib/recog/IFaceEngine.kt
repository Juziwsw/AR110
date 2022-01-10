package com.hiar.mybaselib.recog

import android.content.Context

/**
 *
 * @author wilson
 * @date 26/10/2021
 * Email: haiqin.chen@hiscene.com
 */
interface IFaceEngine {
    /**
     * 初始化
     */
    fun initFaceEngine(context: Context, logPath:String)

    /**
     * 识别结果
     */
    fun run(
        pixArr: ByteArray?,
        width: Int,
        height: Int,
        imageFormat: Int,//21
        flagRecognition: Boolean//true
    ): Array<FaceRecognitionInfo>?

    /**
     * 释放资源
     */
    fun releaseFaceEngine()

}