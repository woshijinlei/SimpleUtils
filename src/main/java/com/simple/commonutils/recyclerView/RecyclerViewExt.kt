package com.simple.commonutils.recyclerView

import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.sqrt

fun RecyclerView.scrolledToCenter(
    position: Int,
    isSmooth: Boolean = false
) {
    fun action() {
        layoutManager?.findViewByPosition(position)?.also { child ->
            if ((layoutManager as LinearLayoutManager).orientation == LinearLayoutManager.HORIZONTAL) {
                smoothScrollToHorizontalCenter(child)
            } else {
                smoothScrollToVerticalCenter(child)
            }
        }
    }

    val lm = (layoutManager as LinearLayoutManager)
    if (lm.findFirstVisibleItemPosition() <= position
        && position <= lm.findLastVisibleItemPosition()
    ) {
        action()
    } else {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if ((dx == 0 && dy == 0)) {//scroll
                    action()
                    recyclerView.removeOnScrollListener(this)
                }
            }

            @Suppress("CascadeIf")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {//smoothScroll
                    action()
                    recyclerView.removeOnScrollListener(this)
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    recyclerView.removeOnScrollListener(this)
                }
            }
        })
        if (!isSmooth) {
            scrollToPosition(position)
        } else {
            smoothScrollToPosition(position)
        }
    }

}

private val linearInterpolator: Interpolator by lazy {
    LinearInterpolator()
}

fun RecyclerView.smoothScrollToHorizontalCenter(
    child: View,
    isLinearSmooth: Boolean = false
) {
    val childMid = (child.left + child.right) / 2
    val recyclerViewMid = width / 2
    if (childMid < recyclerViewMid && !this.canScrollHorizontally(-1)) {
        return
    }
    if (childMid > recyclerViewMid && !this.canScrollHorizontally(1)
    ) {
        return
    }
    smoothScrollBy(
        childMid - recyclerViewMid,
        0,
        if (isLinearSmooth) linearInterpolator else null
    )
}

fun RecyclerView.smoothScrollToVerticalCenter(
    child: View,
    isLinearSmooth: Boolean = false
) {
    val childMid = (child.top + child.bottom) / 2
    val recyclerViewMid = height / 2
    if (childMid < recyclerViewMid && !this.canScrollVertically(-1)) {//Negative to check scrolling (look) up, positive to check scrolling down
        return
    }
    if (childMid > recyclerViewMid && !this.canScrollVertically(1)) {
        return
    }
    smoothScrollBy(
        0, childMid - recyclerViewMid,
        if (isLinearSmooth) linearInterpolator else null
    )
}

fun RecyclerView.scrolledToStart(
    position: Int,
    isSmooth: Boolean = false
) {
    fun action() {
        layoutManager?.findViewByPosition(position)?.also { child ->
            if ((layoutManager as LinearLayoutManager).orientation == LinearLayoutManager.HORIZONTAL) {
                smoothScrollToLeft(child)
            } else {
                smoothScrollToTop(child)
            }
        }
    }

    val lm = (layoutManager as LinearLayoutManager)
    if (lm.findFirstVisibleItemPosition() <= position
        && position <= lm.findLastVisibleItemPosition()
    ) {
        action()
    } else {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if ((dx == 0 && dy == 0)) {//scroll
                    action()
                    recyclerView.removeOnScrollListener(this)
                }
            }

            @Suppress("CascadeIf")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {//smoothScroll
                    action()
                    recyclerView.removeOnScrollListener(this)
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    recyclerView.removeOnScrollListener(this)
                }
            }
        })
        if (!isSmooth) {
            scrollToPosition(position)
        } else {
            smoothScrollToPosition(position)
        }
    }

}

private fun RecyclerView.smoothScrollToLeft(child: View) {
    smoothScrollBy(
        this.layoutManager?.getDecoratedLeft(child) ?: 0, 0
    )
}

private fun RecyclerView.smoothScrollToTop(child: View) {
    smoothScrollBy(
        0, this.layoutManager?.getDecoratedTop(child) ?: 0
    )
}

/**
 * 如果只是需要对item做一次整体监听
 * 避免adapter里面反复给item设置setOnClickListener
 */
private inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.setSimpleItemClickListener(
    crossinline onSimpleClick: RecyclerView.(viewHolder: T, position: Int) -> Unit
) {

    val listener = object : RecyclerView.SimpleOnItemTouchListener() {
        var downX = 0f
        var downY = 0f
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.x
                    downY = e.y
                }
                MotionEvent.ACTION_UP -> {
                    val distanceX = abs(e.x - downX)
                    val distanceY = abs(e.y - downY)
                    if (sqrt(distanceX * distanceX + distanceY * distanceY) < 8)
                        findChildViewUnder(e.x, e.y)?.let {
                            val viewHolder = findContainingViewHolder(it) as T
                            onSimpleClick.invoke(
                                this@setSimpleItemClickListener,
                                viewHolder,
                                getChildAdapterPosition(it)
                            )
                        }

                }
            }
            return false
        }
    }
    this.addOnItemTouchListener(listener)
    this.doOnDetach {
        if (ViewCompat.isAttachedToWindow(it)) {
            this.removeOnItemTouchListener(listener)
        }
    }
}