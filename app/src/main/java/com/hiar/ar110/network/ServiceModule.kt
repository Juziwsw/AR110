package com.hiar.ar110.network

import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.network.api.AR110FileApi
import org.koin.dsl.module

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */

val serviceModule = module {
    single { RetrofitHelper.getService(AR110Api::class.java) }
    single { RetrofitHelper.getService(AR110FileApi::class.java,true) }
}