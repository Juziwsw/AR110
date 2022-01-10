package com.frogsing.libutil.network.http.convert

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

object LongDefault0Adapter {
    @FromJson
    fun fromJson(reader: JsonReader): Long {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextLong()
        }
        reader.nextNull<Unit>()
        return 0L
    }
}