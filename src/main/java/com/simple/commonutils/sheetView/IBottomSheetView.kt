package com.simple.commonutils.sheetView

import android.view.View

interface IBottomSheetView {

    interface Callback {
        fun onSlide(fraction: Float)
        fun onDismiss()
    }

    var content: View

    var dragView: View

    var callback: Callback?

    fun show()

    fun dismiss()
}
