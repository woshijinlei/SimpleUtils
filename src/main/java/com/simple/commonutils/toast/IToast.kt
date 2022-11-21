package com.simple.commonutils.toast

import android.view.View

interface IToast {

    fun toast(view: View, duration: Long)

    fun toast(message: String)
}
