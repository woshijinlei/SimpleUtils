package com.simple.commonutils.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> Gson.fromJsonList(json: String): MutableList<T>? {
    return fromJson<MutableList<T>>(json, object : TypeToken<MutableList<T>>() {}.type)
}

inline fun <reified T> Gson.fromJsonObject(json: String): T {
    return fromJson(json, T::class.java)
}