package com.simple.commonutils.toast

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.core.view.contains

class ViewToast(private val toastContainer: ViewGroup) : IToast, Handler.Callback {

    private var view: View? = null
    private val handler = Handler(Looper.getMainLooper(), this)
    private val animationDuration = 125L

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == 0) {
            view?.let {
                view = null
                handler.removeCallbacksAndMessages(null)
                it.animation = AlphaAnimation(1f, 0f).apply {
                    this.duration = animationDuration
                }
                toastContainer.removeView(it)
            }
        }
        return true
    }

    override fun toast(view: View, duration: Long) {
        this.view?.let {
            this.view = null
            handler.removeMessages(0)
            if (toastContainer.contains(it)) {
                view.animation = null
                toastContainer.removeView(it)
            }
        }
        this.view = view
        view.animation = AlphaAnimation(0f, 1f).apply {
            this.duration = animationDuration
        }
        toastContainer.addView(view)
        handler.sendEmptyMessageDelayed(0, duration)
    }

    override fun toast(message: String) {
        Toast.makeText(toastContainer.context.applicationContext, message, Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        const val duration = 1500L
    }
}