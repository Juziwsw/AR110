package com.hiar.sdk.face

import android.content.Context
import com.hiar.mybaselib.recog.FaceRecognitionInfo
import com.hiar.mybaselib.recog.IFaceEngine

/**
 *
 * @author wilson
 * @date 28/10/2021
 * Email: haiqin.chen@hiscene.com
 */
class HisceneFaceEngine : IFaceEngine {
    companion object {
        private const val TAG = "IFaceEngine"
    }

    /**
     * 初始化
     */
    override fun initFaceEngine(context: Context, logPath: String) {
        FaceEngine.getInstance().initFaceEngine(context, logPath)
    }

    /**
     * 识别结果
     */
    override fun run(
        pixArr: ByteArray?,
        width: Int,
        height: Int,
        imageFormat: Int,
        flagRecognition: Boolean
    ): Array<FaceRecognitionInfo>? {
        val result = FaceEngine.getInstance().run(pixArr, width, height, imageFormat, flagRecognition) ?: return null
        return result.mapNotNull {
            FaceRecognitionInfo(bbox = it.bbox, it.frame_id, it.face_id)
        }.toTypedArray()
    }

    /**
     * 释放资源
     */
    override fun releaseFaceEngine() {
        FaceEngine.getInstance().releaseFaceEngine()
    }
}