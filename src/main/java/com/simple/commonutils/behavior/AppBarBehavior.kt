package com.simple.commonutils.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

class SimpleCoordinatorLayout : CoordinatorLayout {
    var rejectNestScroll = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onStopNestedScroll(target: View) {
        rejectNestScroll = false
        super.onStopNestedScroll(target)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        rejectNestScroll = false
        super.onStopNestedScroll(target, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (rejectNestScroll) return
        super.onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
    }
}

/**
 * 将此Behavior设置给AppBarLayout
 */
class AppBarBehavior : AppBarLayout.Behavior {
    private var overScroller: OverScroller? = null

    constructor() : super()
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        val intercept = super.onInterceptTouchEvent(parent, child, ev)
        obtainOverScroller()
        if (ev.actionMasked == MotionEvent.ACTION_DOWN
            && overScroller?.isFinished == false
        ) {
            overScroller?.abortAnimation()
        }
        if (parent is SimpleCoordinatorLayout) {
            parent.rejectNestScroll = ev.actionMasked == MotionEvent.ACTION_DOWN
        }
        return intercept
    }

    private fun obtainOverScroller() {
        if (overScroller == null) {
            val clazz = Class.forName("com.google.android.material.appbar.HeaderBehavior")
            val field = clazz.getDeclaredField("scroller")
            field.isAccessible = true
            overScroller = (field.get(this) as? OverScroller)
        }
    }
}