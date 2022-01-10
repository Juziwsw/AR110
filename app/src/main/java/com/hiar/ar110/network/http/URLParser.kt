package com.frogsing.libutil.network.http

import com.hileia.common.utils.XLog
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
object URLParser {
    private val urlCache: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    fun parseUrl(baseUrl: HttpUrl?, requestUrl: HttpUrl, originalBaseUrlPathSize: Int): HttpUrl {
        if (baseUrl == null) {
            return requestUrl
        }

        val builder = requestUrl.newBuilder()
        val key = getKey(baseUrl, requestUrl, originalBaseUrlPathSize)
        val cachedPath = urlCache[key]
        if (cachedPath == null) {
            for (i in 0 until requestUrl.pathSize) {
                builder.removePathSegment(0)
            }

            val newPathSegments: MutableList<String> = mutableListOf()
            newPathSegments.addAll(baseUrl.encodedPathSegments)

            if (requestUrl.pathSize > originalBaseUrlPathSize) {
                val encodedPathSegments = requestUrl.encodedPathSegments
                for (j in originalBaseUrlPathSize until encodedPathSegments.size) {
                    newPathSegments.add(encodedPathSegments[j])
                }
                for (segment in newPathSegments) {
                    builder.addEncodedPathSegment(segment)
                }
            } else if (requestUrl.pathSize < originalBaseUrlPathSize) {
                XLog.e("URLParser", " -> parseUrl failed")
            }

        } else {
            builder.encodedPath(cachedPath)
        }

        val resultUrl = builder.scheme(baseUrl.scheme).host(baseUrl.host).port(baseUrl.port).build()

        if (cachedPath == null) {
            urlCache[key] = resultUrl.encodedPath
        }

        return resultUrl
    }

    private fun getKey(baseUrl: HttpUrl, requestUrl: HttpUrl, originalBaseUrlPathSize: Int): String {
        return baseUrl.encodedPath + requestUrl.encodedPath + originalBaseUrlPathSize
    }
}