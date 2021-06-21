package com.simple.commonutils.common

import android.graphics.Bitmap

fun Bitmap?.safeRecycle() {
    if (this != null && !this.isRecycled) {
        this.recycle()
    }
}