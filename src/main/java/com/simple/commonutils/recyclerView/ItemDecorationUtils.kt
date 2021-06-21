package com.simple.commonutils.recyclerView

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import androidx.core.graphics.withSave
import androidx.core.view.forEach
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalStateException

/**
 * 左侧和右侧item距离边界间距为item之间间距的1/2
 */
class LinearItemDecoration(
    private var borderWith: Int = 0,
    private var borderExtraWith: Int = 0,
    private var itemBgColor: Int? = null,
    private var isSurroundItem: Boolean = false
) :
    RecyclerView.ItemDecoration() {
    private val decoratedBounds = Rect()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (itemBgColor == null) return
        parent.forEach {
            parent.getDecoratedBoundsWithMargins(it, decoratedBounds)
            c.withSave {
                c.drawRect(decoratedBounds, Paint().apply {
                    color = itemBgColor as Int
                })
            }

        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val lm = parent.layoutManager as? LinearLayoutManager
        when {
            lm?.orientation == LinearLayoutManager.HORIZONTAL -> setHorizontalOutRect(
                outRect,
                view,
                parent
            )
            lm?.orientation == LinearLayoutManager.VERTICAL -> setVerticalOutRect(
                outRect,
                view,
                parent
            )
            else -> outRect.setEmpty()
        }
    }

    private fun setHorizontalOutRect(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        var left = borderWith
        val right: Int
        val position = parent.getChildAdapterPosition(view)
        when {
            position == 0 -> {
                left = borderWith + borderExtraWith
                right = borderWith / 2
            }
            position < parent.adapter!!.itemCount - 1 -> {
                left = borderWith / 2
                right = borderWith / 2
            }
            else -> {
                left = borderWith / 2
                right = borderWith + borderExtraWith
            }
        }
        val s = if (isSurroundItem) borderWith else 0
        outRect.set(left, s, right, s)
    }

    private fun setVerticalOutRect(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        var top = borderWith
        val bottom: Int
        val position = parent.getChildAdapterPosition(view)
        when {
            position == 0 -> {
                top = borderWith + borderExtraWith
                bottom = borderWith / 2
            }
            position < parent.adapter!!.itemCount - 1 -> {
                top = borderWith / 2
                bottom = borderWith / 2
            }
            else -> {
                top = borderWith / 2
                bottom = borderWith + borderExtraWith
            }
        }
        val s = if (isSurroundItem) borderWith else 0
        outRect.set(s, top, s, bottom)
    }
}

/**
 * 左侧和右侧item距离边界间距近似item之间间距的2倍
 * 限制列数2,3,4列情形
 *
 * todo 实现类似[GridItemDecoration]的通用情形
 */
class GridItemDecorationSurround(
    private val borderWith: Int = 0,
    private var itemBgColor: Int? = null
) : RecyclerView.ItemDecoration() {
    private val decoratedBounds = Rect()
    private val paint = Paint()
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        itemBgColor?.let { borderColor ->
            parent.forEach {
                parent.getDecoratedBoundsWithMargins(it, decoratedBounds)
                c.drawRect(decoratedBounds, paint.apply {
                    color = borderColor
                })
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val lm = parent.layoutManager as? GridLayoutManager
        if (lm?.orientation == GridLayoutManager.VERTICAL) {
            val gm = parent.layoutManager as GridLayoutManager
            val spanCount = gm.spanCount
            when (spanCount) {
                2 -> {
                    val borderWith = this.borderWith / 2
                    setVerticalOutRect2(outRect, view, parent, borderWith)
                }
                3 -> {
                    val borderWith = this.borderWith / 1.125f
                    setVerticalOutRect3(outRect, view, parent, borderWith)
                }
                4 -> {
                    val borderWith = this.borderWith / (base * 4 / 3)
                    setVerticalOutRect4(outRect, view, parent, borderWith)
                }
            }
        }
    }

    private fun setVerticalOutRect2(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        borderWith: Int
    ) {
        val left: Float
        val right: Float
        val gm = parent.layoutManager as GridLayoutManager
        val spanCount = gm.spanCount
        val top = borderWith * 2f
        val bottom = borderWith * 0f
        val adapterPosition = parent.getChildAdapterPosition(view)
        when (checkPosition(spanCount, adapterPosition)) {
            Gravity.START -> {
                left = borderWith * 4f
                right = borderWith * 1f
            }
            Gravity.END -> {
                left = borderWith * 1f
                right = borderWith * 4f
            }
            else -> {
                left = borderWith * 1.5f
                right = borderWith * 1.5f
            }
        }
        outRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun setVerticalOutRect3(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        borderWith: Float
    ) {
        val left: Float
        val right: Float
        val gm = parent.layoutManager as GridLayoutManager
        val spanCount = gm.spanCount
        val top = borderWith * 1.125f
        val bottom = borderWith * 0f
        val adapterPosition = parent.getChildAdapterPosition(view)
        when (checkPosition(spanCount, adapterPosition)) {
            Gravity.START -> {
                left = borderWith * 2.25f
                right = borderWith * 0f
            }
            Gravity.END -> {
                left = borderWith * 0f
                right = borderWith * 2.25f
            }
            else -> {
                left = borderWith * 1.125f
                right = borderWith * 1.125f
            }
        }
        outRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private val base = 1.0625f
    private val r = mutableListOf(
        base + 1f / 3 * base,
        base - 1f / 3 * base,
        base - 1f / 3 * base,
        base + 1f / 3 * base
    )

    private fun setVerticalOutRect4(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        borderWith: Float
    ) {
        val left: Float
        val right: Float
        val gm = parent.layoutManager as GridLayoutManager
        val spanCount = gm.spanCount
        val top = borderWith * (base * 4 / 3)
        val bottom = borderWith * 0f
        val adapterPosition = parent.getChildAdapterPosition(view)
        val gravity = checkPosition(spanCount, adapterPosition)
        when (gravity) {
            Gravity.START -> {
                left = borderWith * base * 2
                right = 0f
            }
            Gravity.END -> {
                left = 0f
                right = borderWith * base * 2
            }
            else -> {
                left = (r.get(((adapterPosition) % spanCount - 1)) * borderWith).toFloat()
                right = (r.get(((adapterPosition) % spanCount + 1)) * borderWith).toFloat()
            }
        }
        outRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun checkPosition(
        spanCount: Int,
        position: Int
    ): Int {
        return when ((position + 1) % spanCount) {
            1 -> Gravity.START
            0 -> Gravity.END
            else -> Gravity.CENTER
        }
    }
}

/**
 * 默认borderExtraWith=0,左侧和右侧item距离边界等于item之间间距
 * 限制列数2列情形
 *
 * todo 实现类似[GridItemDecoration]的通用情形
 */
class GridItemDecorationSurround2(
    private val borderWith: Int = 0,
    private var borderExtraWith: Int = 0,
    private var itemBgColor: Int? = null
) : RecyclerView.ItemDecoration() {
    private val decoratedBounds = Rect()
    private val paint = Paint()
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        itemBgColor?.let { borderColor ->
            parent.forEach {
                parent.getDecoratedBoundsWithMargins(it, decoratedBounds)
                c.drawRect(decoratedBounds, paint.apply {
                    color = borderColor
                })
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val lm = parent.layoutManager as? GridLayoutManager
        if (lm?.orientation == GridLayoutManager.VERTICAL) {
            val gm = parent.layoutManager as GridLayoutManager
            val spanCount = gm.spanCount
            when (spanCount) {
                2 -> {
                    val borderWith = this.borderWith / 2
                    setVerticalOutRect2(outRect, view, parent, borderWith)
                }
            }
        }
    }

    private fun setVerticalOutRect2(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        borderWith: Int
    ) {
        val left: Float
        val right: Float
        val gm = parent.layoutManager as GridLayoutManager
        val spanCount = gm.spanCount
        val top = borderWith * 1f
        val bottom = borderWith * 1f
        val adapterPosition = parent.getChildAdapterPosition(view)
        when (checkPosition(spanCount, adapterPosition)) {
            Gravity.START -> {
                left = borderWith * 2f + borderExtraWith
                right = borderWith * 1f
            }
            Gravity.END -> {
                left = borderWith * 1f
                right = borderWith * 2f + borderExtraWith
            }
            else -> {
                left = borderWith * 1.5f
                right = borderWith * 1.5f
            }
        }
        outRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun checkPosition(
        spanCount: Int,
        position: Int
    ): Int {
        return when ((position + 1) % spanCount) {
            1 -> Gravity.START
            0 -> Gravity.END
            else -> Gravity.CENTER
        }
    }
}

/**
 * 左侧和右侧item距离边界0px
 * 不限制列数,但存在误差
 */
class GridItemDecoration(
    private val borderWith: Int = 0,
    private var itemBgColor: Int? = null
) : RecyclerView.ItemDecoration() {

    private val decoratedBounds = Rect()
    private val compensate = mutableListOf<Float>()
    private val paint = Paint()
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        itemBgColor?.let { borderColor ->
            parent.forEach {
                parent.getDecoratedBoundsWithMargins(it, decoratedBounds)
                c.drawRect(decoratedBounds, paint.apply {
                    color = borderColor
                })
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val lm = parent.layoutManager as? GridLayoutManager
        if (lm?.orientation == GridLayoutManager.VERTICAL) {
            setVerticalOutRect(outRect, view, parent)
        }
    }

    private fun setVerticalOutRect(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        val left: Float
        val right: Float
        val gm = parent.layoutManager as GridLayoutManager
        val spanCount = gm.spanCount
        for (i in 0 until spanCount) {
            compensate.add(((i.toFloat()) / (spanCount - 1)))
        }
        val top = borderWith / 2f
        val bottom = borderWith / 2f
        val adapterPosition = parent.getChildAdapterPosition(view)
        val gravity = checkPosition(spanCount, adapterPosition)
        val horizontalPositionRotail = (compensate[(adapterPosition) % spanCount])
        (view.layoutParams as RecyclerView.LayoutParams).apply {
            topMargin =
                (borderWith * compensate.getOrElse(1) { 1 / 2f }).toInt()
        }
        when (gravity) {
            Gravity.START -> {
                left = borderWith * horizontalPositionRotail
                right = borderWith * (1 - horizontalPositionRotail)
            }
            Gravity.END -> {
                left = borderWith * horizontalPositionRotail
                right = borderWith * (1 - horizontalPositionRotail)
            }
            else -> {
                left = borderWith * horizontalPositionRotail
                right = borderWith * (1 - horizontalPositionRotail)
            }
        }
        outRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun checkPosition(
        spanCount: Int,
        position: Int
    ): Int {
        return when ((position + 1) % spanCount) {
            1 -> Gravity.START
            0 -> Gravity.END
            else -> Gravity.CENTER
        }
    }
}

/**
 * 左侧和右侧item距离边界0px
 * 限制列数2,3,4列情形
 * item之间间距为:
 *   如果是2列，真实间距为borderWith*2（可以实现无整数误差）
 *   如果是3列，真实间距为borderWith*3（可以实现无整数误差）
 *   如果是4列，真实间距为borderWith*4（可以实现无整数误差）
 */
class GridItemDecoration234(
    private val borderWith: Int = 0,
    private var itemBgColor: Int? = null
) : RecyclerView.ItemDecoration() {

    private val decoratedBounds = Rect()

    private val paint = Paint()
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        itemBgColor?.let { borderColor ->
            parent.forEach {
                parent.getDecoratedBoundsWithMargins(it, decoratedBounds)
                c.drawRect(decoratedBounds, paint.apply {
                    color = borderColor
                })
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val lm = parent.layoutManager as? GridLayoutManager
        if (lm?.orientation == GridLayoutManager.VERTICAL) {
            setVerticalOutRect(outRect, view, parent)
        }
    }

    private fun setVerticalOutRect(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        val left: Float
        val right: Float
        val gm = parent.layoutManager as GridLayoutManager
        val spanCount = gm.spanCount
        val isSpecialColumnsSmallSize = spanCount == 2 || spanCount == 3 || spanCount == 4
        val top: Float
        var bottom: Float = borderWith / 2f
        if (isSpecialColumnsSmallSize) {
            if (spanCount == 3) {
                top = borderWith.toFloat()
                bottom = borderWith.toFloat()
            } else if (spanCount == 4) {
                top = borderWith * 1f
                bottom = borderWith * 2f
            } else {
                top = borderWith.toFloat()
                bottom = 0f
            }
        } else {
            throw IllegalStateException("only accept 2,3,4 spanCount")
        }
        val adapterPosition = parent.getChildAdapterPosition(view)
        val gravity = checkPosition(spanCount, adapterPosition)
        (view.layoutParams as RecyclerView.LayoutParams).apply {
            if (spanCount == 3) {
                topMargin = borderWith
            } else if (spanCount == 4) {
                topMargin = borderWith
            } else {
                topMargin = borderWith
            }
        }
        when (gravity) {
            Gravity.START -> {
                if (spanCount == 3) {
                    left = 0f
                    right = borderWith * 2f
                } else if (spanCount == 4) {
                    left = 0f
                    right = borderWith * 3f
                } else {
                    left = 0f
                    right = borderWith * 1f
                }
            }
            Gravity.END -> {
                if (spanCount == 3) {
                    left = borderWith * 2f
                    right = 0f
                } else if (spanCount == 4) {
                    left = borderWith * 3f
                    right = 0f
                } else {
                    left = borderWith * 1f
                    right = 0f
                }
            }
            else -> {
                if (spanCount == 3) {
                    left = borderWith.toFloat()
                    right = borderWith.toFloat()
                } else if (spanCount == 4) {//0 1 2 3
                    left = (((adapterPosition) % spanCount) * borderWith).toFloat()
                    right = 3 * borderWith - left
                } else {
                    left = borderWith.toFloat()
                    right = borderWith.toFloat()
                }
            }
        }
        outRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun checkPosition(
        spanCount: Int,
        position: Int
    ): Int {
        return when ((position + 1) % spanCount) {
            1 -> Gravity.START
            0 -> Gravity.END
            else -> Gravity.CENTER
        }
    }
}