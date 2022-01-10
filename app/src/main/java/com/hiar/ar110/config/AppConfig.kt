package com.hiar.ar110.config

/**
 *
 * @author wilson
 * @date 08/11/2021
 * Email: haiqin.chen@hiscene.com
 * 后台管理配置项
 *
 */
object AppConfig {
    /**
     * 默认使用文通:WINTONE
     */
    val lprEngine: LPREngine
        get() = if (appConfigBean == null) LPREngine.WINTONE else fromValue(appConfigBean!!.appCarPlatform)

    var appConfigBean: AppConfigBean? = null

    private fun fromValue(value: String): LPREngine {
        return when (value.toLowerCase()) {
            LPREngine.WINTONE.value -> LPREngine.WINTONE
            LPREngine.HISCENE.value -> LPREngine.HISCENE
            else -> LPREngine.WINTONE
        }
    }

    enum class LPREngine(val value: String) {
        WINTONE("wintonelpr"),
        HISCENE("hiar");
    }
}