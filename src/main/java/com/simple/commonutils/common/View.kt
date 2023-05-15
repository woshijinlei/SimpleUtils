package com.simple.commonutils.common

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import com.simple.commonutils.R

@SuppressLint("ClickableViewAccessibility")
fun View.touchAlpha(
    consume: Boolean = false,
    useSlop: Boolean = false,
    duration: Long = 250,
    alpha: Float = 0.45f
) {
    if (this.getTag(R.id.sp_touch_state) == true) return
    val slop = ViewConfiguration.get(context).scaledTouchSlop * 4
    var downX = 0f
    var downY = 0f
    setOnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.y
                downX = event.x
                this.animate().cancel()
                v.alpha = alpha
            }
            MotionEvent.ACTION_MOVE -> {
                if (useSlop && (event.y - downY > slop || event.x - downX > slop)) {
                    this.animate().alpha(1f).setDuration(duration).start()
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
    this.setTag(R.id.sp_touch_state, true)
}

@SuppressLint("ClickableViewAccessibility")
fun View.touchElevation(
    consume: Boolean = false,
    defaultTranslationZ: Float,
    targetTranslationZ: Float,
    duration: Long = 75
) {
    if (this.getTag(R.id.sp_touch_elevation) == true) return
    val e = targetTranslationZ
    setOnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                this.animate().z(e).setDuration(duration / 2).start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                this.animate().z(defaultTranslationZ).setDuration(duration).start()
            }
        }
        return@setOnTouchListener consume
    }

    this.setTag(R.id.sp_touch_elevation, true)
}

inline fun View.doOnDetach2(crossinline action: (view: View) -> Unit) {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {}

        override fun onViewDetachedFromWindow(view: View) {
            removeOnAttachStateChangeListener(this)
            action(view)
        }
    })
}

fun View.updateRatio(
    ratio: String,
) = updateLayoutParams<ConstraintLayout.LayoutParams> {
    this.dimensionRatio = ratio
}

/**
 * @param layoutParams (view.layoutParams as ConstraintLayout.LayoutParams)
 */
fun View.updateConstraintLayoutParams(layoutParams: ConstraintLayout.LayoutParams) {
    val c = ConstraintSet()
    var p = parent
    while (p !is ConstraintLayout) {
        p = p.parent// crash is ok in the root
    }
    c.clone(p)
    c.applyToLayoutParams(id, layoutParams)
    c.applyTo(p)
}

fun View.updateConstraintLayoutParams(param: ConstraintLayout.LayoutParams.() -> Unit) {
    val c = ConstraintSet()
    var p = parent
    while (p !is ConstraintLayout) {
        p = p.parent// crash is ok in the root
    }
    val sp = this.layoutParams as ConstraintLayout.LayoutParams
    param.invoke(sp)
    c.clone(p)
    c.applyToLayoutParams(id, sp)
    c.applyTo(p)
}