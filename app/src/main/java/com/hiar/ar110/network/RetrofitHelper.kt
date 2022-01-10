package com.hiar.ar110.network

import com.frogsing.libutil.network.http.HttpClientHelper
import com.frogsing.libutil.network.http.convert.*

import com.hiar.ar110.BuildConfig
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
object RetrofitHelper {
    private val originalBaseUrl: String = getHttpBaseUrl()
    private val TAG = javaClass.simpleName
    private val normalClient: OkHttpClient = buildHttpClient(false)
    private val fileClient: OkHttpClient = buildHttpClient(true)

    private var normalRetrofit: Retrofit = buildHttpRetrofit(false)
    private var fileRetrofit: Retrofit = buildHttpRetrofit(true)

    fun <T> getService(serviceClass: Class<T>, isFileCall: Boolean = false): T {
        return if (isFileCall) fileRetrofit.create(serviceClass) else normalRetrofit.create(serviceClass)
    }

    private fun getHttpBaseUrl(): String {
        return if (BuildConfig.DEBUG) {
            Util.mBaseIp
        } else Util.mBaseIpPolice
    }

    /**
     * 构造http 请求的OkHttClient
     */
    private fun buildHttpClient(isFileClient: Boolean): OkHttpClient {
        return HttpClientHelper.getOkHttpClient(isFileClient){
            addInterceptor(httpHeaderInterceptor)
        }
    }

    private fun buildHttpRetrofit(isFile: Boolean): Retrofit {
        return Retrofit.Builder().client(if (isFile) fileClient else normalClient)
                .addConverterFactory(MoshiConverterFactory.create(buildMoshi()))
                .baseUrl(originalBaseUrl)
                .build()
    }

    /**
     * 防止请求复用导致的：java.io.IOException: unexpected end of stream on okhttp3.Address
     * 第一次请求成功后，客户端复用了原来的连接，但服务器此时已经处在TCP连接中的FIN_WAIT2状态，因此连接不成功
     */
    private object httpHeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            AR110Log.e(TAG, "httpHeaderInterceptor url= ${request.url}")
            val builder = request.newBuilder().addHeader(
                    "Connection","close"
            )
            return chain.proceed(builder.build())
        }
    }

    /**
     *
     * 解析null字符串
     */
    private fun buildMoshi(): Moshi {
        return Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(NullStringDefaultAdapter)
                .add(IntegerDefault0Adapter)
                .add(BooleanDefaultAdapter)
                .add(LongDefault0Adapter)
                .add(DoubleDefault0Adapter)
                .build()
    }
}