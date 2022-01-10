package com.hiar.sdk.lpr.core

import android.content.Context
import android.graphics.*
import android.util.Log
import com.google.gson.Gson
import com.hiar.mybaselib.recog.ILPREngine
import com.hiar.mybaselib.recog.LPRRecognitionInfo
import com.hiar.sdk.lpr.util.WindowUtil
import com.hiscene.hiarlp.core.GoLP
import com.hiscene.hiarlp.model.LPCharacter
import com.hiscene.hiarlp.model.LicensePlateInfo
import java.util.*

/**
 *
 * @author wilson
 * @date 28/10/2021
 * Email: haiqin.chen@hiscene.com
 * 自研车牌识别
 */
class HisceneLPR : ILPREngine {
    private var mContext: Context? = null

    /**
     * 翻译车牌数组
     */
    private fun translateLP(lpType: Int, lpNumberIds: IntArray): LicensePlateInfo? {
        val type = LPType.getTypeByCode(lpType)
        if (lpNumberIds.size <= 1) return null
        val resArray = CharArray(type.size)
        resArray[0] = LPCharacter.LP_PREFIX[lpNumberIds[0]]
        for (index in 1 until type.size) {
            resArray[index] = LPCharacter.LP_SUFFIX[lpNumberIds[index]]
        }
        return LicensePlateInfo(
            lp = String(resArray),
            lpCharArray = resArray,
            lpNumArray = lpNumberIds,
            lpType,
            lpSize = type.size,
            lpDesc = type.desc,
        )
    }


    /**
     * 初始化
     */
    override fun initEngine(context: Context) {
        mContext = context
        GoLP.initGoLP(context)
    }

    override fun getMiniConfidence(): Int {
        return 85
    }

    /**
     * 识别结果
     */
    override fun doRecog(
        width: Int,
        height: Int,
        yuvData: ByteArray
    ): MutableList<LPRRecognitionInfo> {
        val result = mutableListOf<LPRRecognitionInfo>()
        val goTime = Date().time
        val screenRotation = 0
        val resArray = GoLP.detect(width, height, screenRotation, yuvData)
        Log.d(TAG, "resArray resArray:${Gson().toJson(resArray)} screenRotation=${screenRotation}")
        // 一次只识别一个车牌
        if (resArray.size != 1) return result
        val detectedTime = Date().time
        resArray.forEach {
            // 翻译车牌
            val licensePlateInfo = translateLP(it.Type, it.LPNumberIdxs) ?: return@forEach
            // 创建box
            val left = it.Position.x
            val top = it.Position.y
            val right = it.Position.x + it.Position.width
            val bottom = it.Position.y + it.Position.height
            val rect = Rect(left, top, right, bottom)
            result.add(
                LPRRecognitionInfo(
                    box = rect, result = arrayOf(
                        licensePlateInfo.lp,
                        licensePlateInfo.lpDesc,
                        "",
                        "",
                        (it.Confidence * 100).toInt().toString()
                    ), recognitionTime = (detectedTime - goTime),
                    imageData = yuvData
                )
            )
        }
        return result
    }

    /**
     * 释放资源
     */
    override fun release() {
    }

    companion object {
        private const val TAG = "ILPREngine"
    }
}