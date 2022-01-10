package com.hiar.ar110.base.exception

import com.blankj.utilcode.util.Utils
import com.hiar.ar110.R
import com.hiar.mybaselib.utils.AR110Log
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CancellationException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException

object ExceptionEngine {
    //对应HTTP的状态码
    private const val UNAUTHORIZED = 401
    private const val FORBIDDEN = 403
    private const val NOT_FOUND = 404
    private const val REQUEST_TIMEOUT = 408
    private const val INTERNAL_SERVER_ERROR = 500
    private const val BAD_GATEWAY = 502
    private const val SERVICE_UNAVAILABLE = 503
    private const val GATEWAY_TIMEOUT = 504

    fun handleException(e: Throwable): ApiException {
        AR110Log.e("before handleException", e.message)
        return when (e) {
            is HttpException -> ApiException(e, code = e.code()).apply {
                message = when (code) {
                    UNAUTHORIZED, FORBIDDEN, NOT_FOUND, REQUEST_TIMEOUT, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE -> "网络错误"
                    else -> Utils.getApp().getString(R.string.request_failed)
                }
            }
            is JSONException,
            is ParseException,
            is JsonDataException -> ApiException(e, code = ERROR.PARSE_ERROR).apply { message = Utils.getApp().getString(R.string.json_parse_failed) }
            is UnknownHostException -> ApiException(e, code = ERROR.NETWORK_ERROR).apply { message = Utils.getApp().getString(R.string.network_host_unknown) }
            is SocketTimeoutException,
            is ConnectException -> ApiException(e, code = ERROR.NETWORK_ERROR).apply { message = Utils.getApp().getString(R.string.request_failed) }
            is ApiException -> e
            is CancellationException -> ApiException(e, code = ERROR.UNKNOWN).apply { message = Utils.getApp().getString(R.string.unknown_error) }
            else -> ApiException(e, code = ERROR.NETWORK_ERROR).apply { message = Utils.getApp().getString(R.string.unknown_error) }
        }.also {
            AR110Log.e("after handleException", e.message)
        }
    }
}

/**
 * 客户端约定异常
 */
object ERROR {
    /**
     * 未知错误
     */
    const val UNKNOWN = 1000

    /**
     * 解析错误
     */
    const val PARSE_ERROR = 1001

    /**
     * 网络错误
     */
    const val NETWORK_ERROR = 1002
}