package com.simple.commonutils.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.text.TextUtils
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.view.doOnPreDraw

fun TextView.marquee() {
    var abj: ObjectAnimator? = null
    this.ellipsize = null
    this.doOnDetach2 {
        abj?.removeAllListeners()
        abj?.cancel()
    }
    this.doOnPreDraw {
        val w = this.paint.measureText(this.text.toString())
        if (w >= this.maxWidth)
            abj = ObjectAnimator.ofInt(this, "scrollX", 0, (w).toInt()).apply {
                this.duration = 2500
                this.interpolator = LinearInterpolator()
                this.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        this@marquee.ellipsize = TextUtils.TruncateAt.END
                    }
                })
                this.startDelay = 550
                this.start()
            }
    }
}

// 系统自带的缩放机制是针对动态文字(但是比如倒计时的时候，文字会忽大忽小)，这个方法是固定文字长度的最大文字尺寸
fun TextView.setStableMaxTextSize(maxTextSize: Float, stableCharsCount: String = "000000") {
    val min = 1f
    if (stableCharsCount.isBlank() || maxTextSize <= min) return
    var start = min
    paint.textSize = start
    val step = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1f, resources.displayMetrics)
    doOnPreDraw {
        while (true) {
            val s = paint.measureText(stableCharsCount)
            if (s >= measuredWidth || start >= maxTextSize) {
                break
            }
            start += step
        }
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, start)
    }
}

