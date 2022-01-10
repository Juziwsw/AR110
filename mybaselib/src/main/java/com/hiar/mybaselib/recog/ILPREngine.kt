package com.hiar.mybaselib.recog

import android.content.Context

/**
 *
 * @author wilson
 * @date 26/10/2021
 * Email: haiqin.chen@hiscene.com
 */
interface ILPREngine {
    /**
     * 初始化
     */
    fun initEngine(context: Context)

    /**
     * 算法识别结果的最低可信度0~100,默认80
     */
    fun getMiniConfidence():Int{
        return 80
    }

    /**
     * 识别结果
     */
    fun doRecog(width:Int,height:Int, yuvData: ByteArray):MutableList<LPRRecognitionInfo>

    /**
     * 释放资源
     */
    fun release()

}