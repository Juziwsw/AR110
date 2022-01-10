package com.hiar.ar110.config

/**
 * Author:wilson.chen
 * date：6/30/21
 * desc：全局的模块控制
 */
object ModuleConfig {
    /**
     * 处警模块
     */
    var isCopTaskEnable: Boolean = false

    /**
     * 巡逻模块
     */
    var isPatrolTaskEnable: Boolean = true

    /**
     * 人口核查模块
     */
    var isPopulationTaskEnable: Boolean = false

    /**
     * 合成作战模块
     */
    var isChattingTaskEnable: Boolean = false

    fun initModule(configBean: ModuleConfigBean) {
        isCopTaskEnable = configBean.alarm
        isPatrolTaskEnable = configBean.patrol
        isPopulationTaskEnable = configBean.verification
        isChattingTaskEnable = configBean.synthetic
    }

    /**
     * "verification": true, // 流动人口清查
     * "synthetic": true, // 合成作战模块
     * "camera": true, // 摄像头模块
     * "alarm": true, // 接处警模块
     * "patrol": true // 巡逻模块
     *
     */
    data class ModuleConfigBean(
            val alarm: Boolean,
            val camera: Boolean,
            val patrol: Boolean,
            val synthetic: Boolean,
            val verification: Boolean
    )

    var loginName:String? = ""
}