package com.hiar.ar110.extension

import cn.com.cybertech.pdk.utils.GsonUtils
import com.google.gson.reflect.TypeToken

/**
 * Author:wilson.chen
 * date：5/18/21
 * desc：
 */

/**
 * Json str to object
 */
inline fun <reified T> String.fromJson(): T? {
    return kotlin.runCatching {
        GsonUtils.fromJson(this,T::class.java)
    }.getOrNull()
}

/**
 * Json str to list
 */
inline fun <reified T> String.fromJsonList(): MutableList<T>? {
    val typeToken = TypeToken.get(T::class.java) as TypeToken<T>
    return kotlin.runCatching {
        GsonUtils.fromJson<T>(this, typeToken.type)
    }.getOrNull()
}

/**
 * Object to json str
 */
inline fun <reified T> T.toJson(): String? {
    return kotlin.runCatching {
        GsonUtils.toJson(this)
    }.getOrNull()
}

inline fun <reified T> T.toJson(replaceStr:String): String? {
    return kotlin.runCatching {
        var message=GsonUtils.toJson(this)
        message.replace("reponseField",replaceStr.toLowerCase())
    }.getOrNull()
}

/**
 * MutableList to Json
 */
inline fun <reified T> MutableList<T>.toJson(): String? {
    return kotlin.runCatching {
        GsonUtils.toJson(this)
    }.getOrNull()
}


