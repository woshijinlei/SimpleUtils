package com.simple.commonutils.anim

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View

fun View.animatorScaleXY() {
    this.animate()
        .scaleX(1.2f)
        .scaleY(1.2f)
        .setDuration(450)
        .withEndAction {
            this.animate().scaleX(1f)
                .scaleY(1f)
                .setDuration(450)
                .start()
        }.start()
}

object AnimatorUtils {
    fun scaleXYAuto(view: View, startScale: Float, targetScale: Float) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, startScale, targetScale).apply {
            this.repeatCount = ValueAnimator.INFINITE
            this.repeatMode = ValueAnimator.REVERSE
        }

        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, startScale, targetScale).apply {
            this.repeatCount = ValueAnimator.INFINITE
            this.repeatMode = ValueAnimator.REVERSE
        }
        val animator = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = 300
            start()
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                animator.resume()
            }

            override fun onViewDetachedFromWindow(v: View) {
                animator.pause()
            }
        })
    }

    fun scaleXYManuel(view: View, targetScale: Float, startScale: Float = 1f): AnimatorSet {
        val scaleX =
            ObjectAnimator.ofFloat(view, View.SCALE_X, startScale, targetScale).apply {
                this.repeatCount = ValueAnimator.INFINITE
                this.repeatMode = ValueAnimator.REVERSE
            }

        val scaleY =
            ObjectAnimator.ofFloat(view, View.SCALE_Y, startScale, targetScale).apply {
                this.repeatCount = ValueAnimator.INFINITE
                this.repeatMode = ValueAnimator.REVERSE
            }
        return AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = 250
        }
    }
}