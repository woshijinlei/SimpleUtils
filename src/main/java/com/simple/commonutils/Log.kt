package com.simple.commonutils

import android.util.Log

fun Any?.log(tag: Any?, msg: Any? = null) {
    if (BuildConfig.DEBUG) {
        val clazzTag = if (this == null) {
            "null."
        } else {
            this::class.java.simpleName + "."
        }
        Log.d("woshijinlei", "${clazzTag}${tag}:$msg")
    }
}

fun Any?.logE(tag: Any?, msg: Any? = null) {
    if (BuildConfig.DEBUG) {
        val clazzTag = if (this == null) {
            "null."
        } else {
            this::class.java.simpleName + "."
        }
        Log.e("woshijinlei", "${clazzTag}${tag}:$msg")
    }
}