package com.simple.commonutils.sheetView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.customview.widget.ViewDragHelper

/**
 * [content]和[dragView]是平级关系
 */
class BottomSheetView2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), IBottomSheetView {

    private var dragViewOriginTop = -1

    private var offsetY: Int = 0

    override lateinit var content: View

    override lateinit var dragView: View

    override var callback: IBottomSheetView.Callback? = null

    private val mCallback = object : ViewDragHelper.Callback() {

        override fun getViewVerticalDragRange(child: View): Int {
            return content.height
        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == dragView
                    && child.top >= dragViewOriginTop
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
            callback?.onSlide(offsetY.toFloat() / height, dy < 0)
            offsetContent(dy)
            if (top == height + dragViewOriginTop) {
                visibility = View.GONE
                callback?.onDismiss()
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (yvel > 0) {
                viewDragHelper.settleCapturedViewAt(
                    releasedChild.left,
                    height + dragViewOriginTop
                )
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

    private fun offsetContent(dy: Int, self: View? = null) {
        self?.offsetTopAndBottom(dy)
        content.offsetTopAndBottom(dy)
    }

    private fun offsetOtherSiblings(dy: Int, self: View? = null) {
        children.forEach {
            if (it == self) return@forEach
            it.offsetTopAndBottom(dy)
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
                offsetY = (height)
                offsetContent(offsetY, dragView)
            }
            viewDragHelper.smoothSlideViewTo(
                dragView,
                dragView.left,
                dragViewOriginTop
            )
            invalidate()
        }
    }

    override fun dismiss(anim: Boolean) {
        if (anim) {
            viewDragHelper.smoothSlideViewTo(dragView, dragView.left, height + dragViewOriginTop)
            invalidate()
        } else {
            offsetY = height
            callback?.onSlide(1f, false)
            callback?.onDismiss()
            visibility = View.GONE
        }
    }

    override fun computeScroll() {
        val compute = viewDragHelper.continueSettling(true)
        if (compute) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        dragViewOriginTop = dragView.top
        offsetContent(offsetY, dragView)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
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
}