package com.frogsing.libutil.network.http.convert

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

object NullStringDefaultAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): String {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextString()
        }
        reader.nextNull<Unit>()
        return ""
    }
}
