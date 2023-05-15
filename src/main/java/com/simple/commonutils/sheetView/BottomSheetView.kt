package com.simple.commonutils.sheetView

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.customview.widget.ViewDragHelper

/**
 * [content]和[dragView]是父子关系
 */
class BottomSheetView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), IBottomSheetView {

    override lateinit var content: View

    override lateinit var dragView: View

    override var callback: IBottomSheetView.Callback? = null

    private var dragViewOriginTop = -1

    private var offsetY: Int = 0

    private var downX = -1f
    private var downY = -1f

    private val mCallback = object : ViewDragHelper.Callback() {

        override fun getViewVerticalDragRange(child: View): Int {
            return content.height
        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == content
                    && child.top >= dragViewOriginTop
                    && dragView.areaToParent(this@BottomSheetView)
                .contains(downX.toInt(), downY.toInt())
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return top.coerceAtLeast(dragViewOriginTop)
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            offsetY += dy
            callback?.onSlide(offsetY.toFloat() / content.height, dy < 0)
            if (top == height) {
                visibility = View.GONE
                callback?.onDismiss()
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (yvel > 0) {
                viewDragHelper.settleCapturedViewAt(releasedChild.left, height)
            } else {
                viewDragHelper.smoothSlideViewTo(
                    releasedChild,
                    releasedChild.left,
                    dragViewOriginTop
                )
            }
            invalidate()
        }
    }

    private val viewDragHelper: ViewDragHelper = ViewDragHelper.create(this, 1f, mCallback)

    init {
        viewDragHelper.apply {
            this.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)
            this.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM)
            val density = resources.displayMetrics.density
            val minVel = 400 * density
            this.minVelocity = minVel
        }
    }

    override fun show() {
        visibility = View.VISIBLE
        doOnLayout {
            if (offsetY == 0) {
                offsetY = (height - dragViewOriginTop)
                content.offsetTopAndBottom(offsetY)
            }
            viewDragHelper.smoothSlideViewTo(content, content.left, dragViewOriginTop)
            invalidate()
        }
    }

    override fun dismiss(anim: Boolean) {
        viewDragHelper.smoothSlideViewTo(content, content.left, height)
        invalidate()
    }

    override fun computeScroll() {
        val compute = viewDragHelper.continueSettling(true)
        if (compute) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        dragViewOriginTop = content.top
        content.offsetTopAndBottom(offsetY)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            downX = ev.x
            downY = ev.y
            val s = viewDragHelper.continueSettling(false)
            if (s) return true
        }
        val consume = viewDragHelper.shouldInterceptTouchEvent(ev)
        return consume || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val s = viewDragHelper.continueSettling(false)
            if (s) return true
        }
        viewDragHelper.processTouchEvent(event)
        super.onTouchEvent(event)
        return true
    }

    fun View.areaToParent(targetParentView: View = this.parent as View): Rect {
        var p = parent as View
        var l = 0
        var t = 0
        while (p != targetParentView) {
            l += p.left
            t += p.top
            p = p.parent as View// ViewRoot will crash, but it is ok
        }
        return Rect(
            this.left + l, this.top + t,
            this.left + l + width, this.top + t + height
        )
    }
}