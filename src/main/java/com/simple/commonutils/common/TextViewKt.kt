package com.simple.commonutils.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.text.TextUtils
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
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        this@marquee.ellipsize = TextUtils.TruncateAt.END
                    }
                })
                this.startDelay = 550
                this.start()
            }
    }
}
