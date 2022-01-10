package com.hiar.ar110.base.exception

/**
 * Author:wilson.chen
 * date：5/31/21
 * desc：
 */
class ApiException(
        throwable: Throwable? = null,
        override var message: String? = null,
        val code: Int = 0,
) : Exception(message, throwable) {
}