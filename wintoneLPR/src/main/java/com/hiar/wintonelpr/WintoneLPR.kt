package com.hiar.wintonelpr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hiar.mybaselib.recog.ILPREngine
import com.hiar.mybaselib.recog.LPRRecognitionInfo
import com.kernal.plateid.PlateCfgParameter
import com.kernal.plateid.PlateRecognitionParameter
import com.kernal.plateid.RecogService
import java.util.*

/**
 *
 * @author wilson
 * @date 26/10/2021
 * Email: haiqin.chen@hiscene.com
 * 文通车牌识别
 * 使用yuv数据解析
 */
class WintoneLPR : ILPREngine {
    companion object {
        private const val TAG = "ILPREngine"
        private const val OCR_DEV_CODE = "5LQU6AOO5YAW77Y"
    }
    private var mContext: Context? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mSdkInited: Int = -1
    private var mRecogBinder: RecogService.MyBinder? = null
    private val prp: PlateRecognitionParameter by lazy {
        PlateRecognitionParameter().apply {
            devCode = OCR_DEV_CODE
            plateIDCfg.scale = 1
        }
    }

    // 单层武警车牌是否开启:4是；5不是
    // 双层武警车牌是否开启:16是；17不是
    // 使馆车牌是否开启:12是；13不是
    // 是否开启个性化车牌:0是；1不是
    // cfgparameter.nContrast = 9;//
    // 清晰度指数(取值范围0-9,最模糊时设为1;最清晰时设为9)
    // 识别阈值(取值范围0-9,5:默认阈值0:最宽松的阈值9:最严格的阈值)
    // 只定位车牌是否开启:14是；15不是
    // 双层黄色车牌是否开启:2是；3不是
    // 双层军队车牌是否开启:6是；7不是
    // 省份顺序
    // 只识别双层黄牌是否开启:10是；11不是
    // 农用车车牌是否开启:8是；9不是
    // 是否夜间模式：1是；0不是
    //新能源车牌开启
    //领事馆车牌开启
    //厂内车牌是否开启     18:是  19不是
    //民航车牌是否开启  20是 21 不是
    private val plateCfgParameter: PlateCfgParameter
        get() {
            val cfgparameter = PlateCfgParameter()
            cfgparameter.armpolice = 4 // 单层武警车牌是否开启:4是；5不是
            cfgparameter.armpolice2 = 16 // 双层武警车牌是否开启:16是；17不是
            cfgparameter.embassy = 12 // 使馆车牌是否开启:12是；13不是
            cfgparameter.individual = 0 // 是否开启个性化车牌:0是；1不是
            // cfgparameter.nContrast = 9;//
            // 清晰度指数(取值范围0-9,最模糊时设为1;最清晰时设为9)
            cfgparameter.nOCR_Th = 0
            cfgparameter.nPlateLocate_Th = 5 // 识别阈值(取值范围0-9,5:默认阈值0:最宽松的阈值9:最严格的阈值)
            cfgparameter.onlylocation = 15 // 只定位车牌是否开启:14是；15不是
            cfgparameter.tworowyellow = 2 // 双层黄色车牌是否开启:2是；3不是
            cfgparameter.tworowarmy = 6 // 双层军队车牌是否开启:6是；7不是
            cfgparameter.szProvince = "" // 省份顺序
            cfgparameter.onlytworowyellow = 11 // 只识别双层黄牌是否开启:10是；11不是
            cfgparameter.tractor = 8 // 农用车车牌是否开启:8是；9不是
            cfgparameter.bIsNight = 0 // 是否夜间模式：1是；0不是
            cfgparameter.newEnergy = 24 //新能源车牌开启
            cfgparameter.consulate = 22 //领事馆车牌开启
            cfgparameter.Infactory = 18 //厂内车牌是否开启     18:是  19不是
            cfgparameter.civilAviation = 20 //民航车牌是否开启  20是 21 不是
            return cfgparameter
        }

    private var mRecogConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mRecogConnection = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mRecogBinder = service as RecogService.MyBinder
            mSdkInited = mRecogBinder!!.initPlateIDSDK
            if (mSdkInited != 0) {
                Toast.makeText(mContext, "服务OCR初始化失败 mSdkInited:${mSdkInited}", Toast.LENGTH_SHORT).show()
            } else {
                initParam()
            }
        }
    }

    /*绑定服务*/
    private fun initService() {
        val recogIntent = Intent(mContext, RecogService::class.java)
        mContext?.bindService(recogIntent, mRecogConnection!!, AppCompatActivity.BIND_AUTO_CREATE)
    }

    private fun initParam() {
        Log.d(TAG,"initParam")
        mRecogBinder!!.setRecogArgu(plateCfgParameter, 6)
    }

    private fun getRecogResult(fieldvalue: Array<String?>?, datas: ByteArray): LPRRecognitionInfo? {
        val resultString: Array<String?>
        var boolString: String? = fieldvalue!![0]
        if (!boolString.isNullOrEmpty()) {
            resultString = boolString.split(";").toTypedArray()
            val lenght = resultString.size
            // 一次只识别一个车牌
            if (lenght == 1) {
                return LPRRecognitionInfo(result = fieldvalue, imageData = datas)
            }
        }
        return null
    }

    /**
     * 设置横屏时的真实识别区域
     * configManager.cameraResolution.x：预览分辨率的宽
     * configManager.cameraResolution.y：预览分辨率的高
     */
    private fun setHorizontalRegion(prp: PlateRecognitionParameter) {
        prp.plateIDCfg.left = 0
        prp.plateIDCfg.top = 0
        prp.plateIDCfg.right = mWidth
        prp.plateIDCfg.bottom = mHeight
    }

    /**
     * 设置竖屏时的真实识别区域
     * configManager.cameraResolution.x：预览分辨率的宽
     * configManager.cameraResolution.y：预览分辨率的高
     */
    private fun setLinearRegion(prp: PlateRecognitionParameter) {
        prp.plateIDCfg.left = 0
        prp.plateIDCfg.top = 0
        prp.plateIDCfg.right = mWidth
        prp.plateIDCfg.bottom = mHeight
    }

    private fun stopOcr() {
        if (null != mRecogBinder) {
            mContext?.unbindService(mRecogConnection!!)
            mRecogBinder = null
        }
    }

    /**
     * 初始化
     */
    override fun initEngine(context: Context) {
        Log.d(TAG,"initEngine")
        mContext = context
        initService()
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
        mWidth = width
        mHeight = height
        prp.width = width
        prp.height = height
        prp.picByte = yuvData

        //向左旋转
        var rotate_left = true
        //向右旋转
        var rotate_right = false
        //正向旋转
        var rotate_top = false
        //倒置旋转
        var rotate_bottom = false
        if (rotate_left) {
            // 通知识别核心,识别前图像应先旋转的角度
            prp.plateIDCfg.bRotate = 0
            setHorizontalRegion(prp)
            rotate_left = false
        } else if (rotate_top) {
            prp.plateIDCfg.bRotate = 1
            setLinearRegion(prp)
            rotate_top = false
        } else if (rotate_right) {
            prp.plateIDCfg.bRotate = 2
            setHorizontalRegion(prp)
            rotate_right = false
        } else if (rotate_bottom) {
            prp.plateIDCfg.bRotate = 3
            setLinearRegion(prp)
            rotate_bottom = false
        }
        val recogDetail = mRecogBinder?.doRecogDetail(prp)?:return result
        Log.d(TAG, "recogDetail:${Arrays.toString(recogDetail)}")
        if (recogDetail.isNotEmpty()) {
            for (i in recogDetail.indices) {
                if (null != recogDetail[i]) {
                    Log.d(TAG, recogDetail[i])
                }
            }
        }
        if (recogDetail!![0] != null && "" != recogDetail[0]) {
            /**
             * 当拍照识别和视频流识别时，如果有结果就执行如下代码
             */
            getRecogResult(recogDetail, yuvData)?.apply {
                result.add(this)
            }
        }
        return result
    }

    /**
     * 释放资源
     */
    override fun release() {
        stopOcr()
    }
}