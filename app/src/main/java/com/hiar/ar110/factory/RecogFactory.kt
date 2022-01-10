package com.hiar.ar110.factory

import android.util.Log
import com.hiar.ar110.config.AppConfig
import com.hiar.mybaselib.recog.IFaceEngine
import com.hiar.mybaselib.recog.ILPREngine
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.sdk.face.HisceneFaceEngine
import com.hiar.sdk.lpr.core.HisceneLPR
import com.hiar.wintonelpr.WintoneLPR

/**
 *
 * @author wilson
 * @date 01/11/2021
 * Email: haiqin.chen@hiscene.com
 * 人脸和车牌检测工厂,统一提供对外使用的检测引擎
 */
object RecogFactory {

    fun getLPREngine(): ILPREngine {
        Log.d("getLPREngine","AppConfig.lprEngine=${AppConfig.lprEngine.name}")
        if (AppConfig.lprEngine == AppConfig.LPREngine.HISCENE) {
            return HisceneLPR()
        }
        return WintoneLPR()
    }

    fun getFaceEngine(): IFaceEngine {
        return HisceneFaceEngine()
    }
}