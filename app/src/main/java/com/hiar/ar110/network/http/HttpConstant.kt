package com.hiar.ar110.network.http

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
object HttpConstant {
    const val HEADER_USER_ID = "uid"
    const val HEADER_OS_TYPE = "x-os-type"
    const val HEADER_DEVICE_TOKEN = "x-device-token"
    const val HEADER_ACCESS_TOKEN = "x-access-token"
    const val HEADER_REQUEST_ID = "x-wa-request-id"
    const val HEADER_API_VERSION = "x-api-version"
    const val HEADER_X_WA_UA = "x-wa-ua"
    const val HEADER_X_WA_REQUEST_LANGUAGE = "x-wa-request-language"
    const val HEADER_X_WA_REFER = "x-wa-refer"
    const val HEADER_ENV_CATEGORY = "x-wa-env-category"
    const val HEADER_CLIENT_TYPE = "x-wa-client-type"
    const val HEADER_IGNORE_BASE_URL = "x-wa-ignore-base-url"

    const val DEFAULT_TIMEOUT: Long = 15
    const val READ_TIMEOUT: Long = 60
}