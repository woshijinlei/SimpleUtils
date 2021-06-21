package com.simple.commonutils.common

import android.view.View

class NoDoubleClickListener(private val onClick: () -> Unit) : View.OnClickListener {
    private var lastClickTime = 0L
    private val doubleClickExitTime = 1200

    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > doubleClickExitTime) {
            lastClickTime = currentTime
            onClick.invoke()
        }
    }
}