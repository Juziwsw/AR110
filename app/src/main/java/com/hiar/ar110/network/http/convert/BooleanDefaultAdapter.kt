package com.frogsing.libutil.network.http.convert

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

object BooleanDefaultAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Boolean {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextBoolean()
        }
        reader.nextNull<Unit>()
        return false
    }
}