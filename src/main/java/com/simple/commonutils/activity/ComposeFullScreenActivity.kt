package com.simple.commonutils.activity

import android.view.View
import android.view.ViewGroup
import android.view.Window

/**
 * ComposeView设置为全屏状态
 */
abstract class ComposeFullScreenActivity : FullScreenActivity() {

    override val topAnchorViews: MutableList<View>?
        get() = null

    override val bottomAnchorViews: MutableList<View>?
        get() = null

    override val isSticky: Boolean
        get() = false

    companion object {

        fun Window.contentView(): View? {
            return decorView.findViewById<ViewGroup>(android.R.id.content)
                ?: (decorView as ViewGroup).getChildAt(0)
        }
    }
}