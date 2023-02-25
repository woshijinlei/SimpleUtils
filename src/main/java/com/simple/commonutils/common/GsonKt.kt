package com.simple.commonutils.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GsonKt {
    inline fun <reified T> fromJsonList(json: String): MutableList<T>? {
        return Gson().fromJson<MutableList<T>>(json, object : TypeToken<MutableList<T>>() {}.type)
    }

    inline fun <reified T> fromJsonObject(json: String): T {
        return Gson().fromJson(json, T::class.java)
    }
}