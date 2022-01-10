package com.frogsing.libutil.network.http

import com.blankj.utilcode.util.JsonUtils
import com.hiar.ar110.network.http.HttpConstant
import com.hiar.mybaselib.utils.AR110Log
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
object HttpClientHelper {
    private const val TAG = "🚀 AR110_OkHttp"

    fun getOkHttpClient(isFileClient: Boolean = false, block: OkHttpClient.Builder.() -> Unit = {}): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        return builder.apply {
            addInterceptor(getHttpLoggingInterceptor(isFileClient))
            connectTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(HttpConstant.READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            connectionSpecs(listOf(
                    ConnectionSpec.MODERN_TLS,
                    ConnectionSpec.COMPATIBLE_TLS,
                    ConnectionSpec.CLEARTEXT)
            )
            retryOnConnectionFailure(false) // 错误重连
            block()
        }.build()
    }

    fun getHttpLoggingInterceptor(fileClient: Boolean) = HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
                    if (message.startsWith("{") && message.endsWith("}")
                            || message.startsWith("[") && message.endsWith("]")) {
                        AR110Log.i(TAG, "\n${JsonUtils.formatJson(message)}\n")
                    } else {
                        AR110Log.i(TAG, message)
                    }
                }
            }
    ).apply {
        level = if (fileClient) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.BODY
    }
}