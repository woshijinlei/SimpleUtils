package com.simple.commonutils.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.simple.commonutils.log
import kotlin.math.max

class ImageScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        const val Duration = 500L
    }

    private val suiteBitmap = false
    private var srcRect: Rect? = null
    private var destRectFRight: RectF? = null
    private var destRectFLeft: RectF? = null
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null
    private var lastAnimatorValue = 0f

    var bitmap: Bitmap? = null

    fun start() {
        post {
            startAnimator()
        }
    }

    fun stop() {
        animator?.cancel()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmap == null || bitmap!!.isRecycled) return
        if (srcRect == null) {
            srcRect = Rect(0, 0, bitmap!!.width, bitmap!!.height)
        }
        if (destRectFRight == null) {
            val suite = if (suiteBitmap) height - bitmap!!.height.toFloat() else 0f
            destRectFRight = RectF(
                0f, suite,
                width.toFloat(), height.toFloat()
            )
            destRectFLeft = RectF(
                -width.toFloat(), suite,
                0f, height.toFloat()
            )
        }
        val tX = (if (animator?.isRunning == true) animator!!.animatedValue as Float else 0f)
        // offset
        val offset = max(tX - lastAnimatorValue, 0f)
        destRectFRight!!.offset(offset, 0f)
        destRectFLeft!!.offset(offset, 0f)
        // reset
        val sequence = if (destRectFRight!!.left > destRectFLeft!!.left) {
            destRectFRight
        } else {
            destRectFLeft
        }
        if (sequence!!.left >= width - 0.5f) {
            sequence.offset(-2 * width.toFloat(), 0f)
        }
        // draw
        canvas.drawBitmap(bitmap!!, srcRect, destRectFRight!!, bitmapPaint)
        canvas.drawBitmap(bitmap!!, srcRect, destRectFLeft!!, bitmapPaint)
        lastAnimatorValue = tX
    }

    //参看drawable
    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        log("onVisibilityAggregated", "isVisible $isVisible ")
        if (isVisible) {
            animator?.resume()
        } else {
            animator?.pause()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        log("onDetachedFromWindow", "onDetachedFromWindow")
        animator?.cancel()
        animator = null
    }

    private fun startAnimator() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, width.toFloat()).apply {
                this.duration = Duration
                this.repeatCount = ValueAnimator.INFINITE
                this.repeatMode = ValueAnimator.RESTART
                this.interpolator = LinearInterpolator()
                this.addUpdateListener {
                    invalidate()
                }
                this.start()
            }
        } else {
            animator?.apply {
                this.cancel()
                this.start()
            }
        }
    }

}