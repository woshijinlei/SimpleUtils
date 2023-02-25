package com.simple.commonutils.loading

import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.view.contains

class ViewLoading(
    private val content: View,
    private val viewGroup: ViewGroup,
    backPressedDispatcher: OnBackPressedDispatcher,
) : ILoading {

    private var hideCallback: ((isInner: Boolean) -> Unit)? = null

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (backPressedCancelable) {
                isManual = false
                hideLoading()
            } else {
                // toast for wait or nothing
            }
        }
    }

    private var backPressedCancelable = false
    private var isManual = true

    init {
        backPressedDispatcher.addCallback(backPressedCallback.apply { this.isEnabled = false })
        content.setOnTouchListener { v, event -> return@setOnTouchListener true }
    }

    override fun showLoading(backPressedCancelable: Boolean) {
        (viewGroup).apply {
            (this@ViewLoading).backPressedCancelable = backPressedCancelable
            content.animation = AlphaAnimation(0f, 1f).apply {
                this.duration = 200
            }
            this.addView(content)
            backPressedCallback.isEnabled = true
        }
    }

    override fun hideLoading() {
        (viewGroup).apply {
            if (this.contains(content)) {
                content.animation = AlphaAnimation(1f, 0f).apply { this.duration = 100 }
                this.removeView(content)
                hideCallback?.invoke(isManual)
            }
            backPressedCallback.remove()
        }
    }

    override fun setCallback(hideCallback: (isManual: Boolean) -> Unit) {
        this.hideCallback = hideCallback
    }
}