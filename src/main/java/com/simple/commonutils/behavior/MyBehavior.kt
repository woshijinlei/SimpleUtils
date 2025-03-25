package com.simple.commonutils.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import java.lang.reflect.Field

class MyBehavior(context: Context?, attributeSet: AttributeSet?) :
    AppBarLayout.Behavior(context, attributeSet) {
    /* renamed from: a  reason: collision with root package name */
    private var f9937a = false
    private var b = false

    @Throws(NoSuchFieldException::class)
    private fun a(): Field {
        return try {
            javaClass.superclass.superclass.getDeclaredField("mFlingRunnable")
        } catch (unused: NoSuchFieldException) {
            MyBehavior::class.java.superclass.superclass.superclass.getDeclaredField("flingRunnable")
        }
    }

    @Throws(NoSuchFieldException::class)
    private fun b(): Field {
        return try {
            javaClass.superclass.superclass.getDeclaredField("mScroller")
        } catch (unused: NoSuchFieldException) {
            MyBehavior::class.java.superclass.superclass.superclass.getDeclaredField("scroller")
        }
    }

    private fun e(appBarLayout: AppBarLayout) {
        try {
            val a2 = a()
            val b2 = b()
            a2.isAccessible = true
            b2.isAccessible = true
            val runnable = a2[this] as Runnable
            val overScroller = b2[this] as OverScroller
            if (runnable != null) {
                appBarLayout.removeCallbacks(runnable)
                a2[this] = null as Any?
            }
            if (overScroller != null && !overScroller.isFinished) {
                overScroller.abortAnimation()
            }
        } catch (e2: NoSuchFieldException) {
            e2.printStackTrace()
        } catch (e3: IllegalAccessException) {
            e3.printStackTrace()
        }
    }

    /* renamed from: c */
    override fun onInterceptTouchEvent(
        coordinatorLayout: CoordinatorLayout,
        appBarLayout: AppBarLayout,
        motionEvent: MotionEvent
    ): Boolean {
        this.b = false
        if (this.f9937a) {
            this.b = true
        }
        if (motionEvent.actionMasked == 0) {
            e(appBarLayout)
        }
        return super.onInterceptTouchEvent(coordinatorLayout, appBarLayout, motionEvent)
    }

    /* renamed from: d */
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        appBarLayout: AppBarLayout,
        view: View,
        i: Int,
        i2: Int,
        i3: Int,
        i4: Int,
        i5: Int
    ) {
        if (!this.b) {
            super.onNestedScroll(coordinatorLayout, appBarLayout, view, i, i2, i3, i4, i5)
        }
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        appBarLayout: AppBarLayout,
        view: View,
        i: Int,
        i2: Int,
        iArr: IntArray,
        i3: Int
    ) {
        if (i3 == 1) {
            this.f9937a = true
        }
        if (!this.b) {
            super.onNestedPreScroll(coordinatorLayout, appBarLayout, view, i, i2, iArr, i3)
        }
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        appBarLayout: AppBarLayout,
        view: View,
        view2: View,
        i: Int,
        i2: Int
    ): Boolean {
        e(appBarLayout)
        return super.onStartNestedScroll(coordinatorLayout, appBarLayout, view, view2, i, i2)
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        appBarLayout: AppBarLayout,
        view: View,
        i: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, appBarLayout, view, i)
        this.f9937a = false
        this.b = false
    }

    companion object {
        private const val c = "CustomAppbarLayoutBehavior"

        /* renamed from: d  reason: collision with root package name */
        private const val f9936d = 1
    }
}
