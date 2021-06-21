package com.simple.commonutils.common

import android.content.Context
import android.widget.Toast

object ExitUtils {

    private var lastClickTime = 0L
    private const val doubleClickExitTime = 2000

    fun canExitDoubleClick(context: Context): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastClickTime <= doubleClickExitTime) {
            true
        } else {
            Toast.makeText(context,"Double click to exit.",Toast.LENGTH_SHORT).show()
            lastClickTime = System.currentTimeMillis()
            false
        }
    }
}
