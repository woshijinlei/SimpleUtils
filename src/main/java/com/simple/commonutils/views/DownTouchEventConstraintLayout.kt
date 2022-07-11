package com.simple.commonutils.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * 层级嵌套，子view可能会消耗touch事件，这个会保证无论点击任何位置都可以接受到DOWN事件
 */
class DownTouchEventConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    fun interface OnTouchEventDispatch {
        fun onDown()
    }

    var onTouchEventDispatch: OnTouchEventDispatch? = null

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            onTouchEventDispatch?.onDown()
        }
        return super.onInterceptTouchEvent(ev)
    }
}