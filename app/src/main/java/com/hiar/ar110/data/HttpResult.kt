package com.hiar.ar110.data

/**
 * author: liwf
 * date: 2021/5/28 9:04
 */
open class HttpResult<T> {
    var retCode = 0
    var data: T? = null
    var setting: T? = null
    val isSuccess: Boolean
        get() = retCode == 0


}