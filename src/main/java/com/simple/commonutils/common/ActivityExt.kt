package com.simple.commonutils.common

import android.app.Activity
import android.content.Intent

fun Activity.start(java: Class<*>) {
    startActivity(Intent(this, java))
}

inline fun <reified T> Activity.start(config: Intent.() -> Unit = {}) {
    Intent(this, T::class.java).apply {
        config()
        startActivity(this)
    }
}