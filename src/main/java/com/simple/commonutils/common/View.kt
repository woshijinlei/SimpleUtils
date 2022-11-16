package com.simple.commonutils.common

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.PathInterpolator

@SuppressLint("ClickableViewAccessibility")
fun View.touchState(consume: Boolean = false, useSlop: Boolean = false, duration: Long = 250) {
    if (this.getTag() == true) return
    val slop = ViewConfiguration.get(context).scaledTouchSlop * 4
    var downX = 0f
    var downY = 0f
    setOnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.y
                downX = event.x
                this.animate().setInterpolator(PathInterpolator(0.25f, 1f)).cancel()
                v.alpha = 0.35f
            }
            MotionEvent.ACTION_MOVE -> {
                if (useSlop && (event.y - downY > slop || event.x - downX > slop)) {
                    this.animate().alpha(1f).setDuration(duration).start()
                    // v.alpha = 1f
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                downX = 0f
                downY = 0f
                this.animate().alpha(1f).setDuration(duration).start()
            }
        }
        return@setOnTouchListener consume
    }
    this.setTag(true)
}

@SuppressLint("ClickableViewAccessibility")
fun View.touchElevation(consume: Boolean = false, duration: Long = 225, translationZ: Float) {
    if (this.getTag() == true) return
    val e = this.resources.displayMetrics.scaledDensity * 10

    setOnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                this.animate().cancel()
                this.translationZ = e
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                this.animate().z(translationZ).setDuration(duration).start()
            }
        }
        return@setOnTouchListener consume
    }

    this.setTag(true)
}