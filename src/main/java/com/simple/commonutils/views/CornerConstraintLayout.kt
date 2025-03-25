package com.simple.commonutils.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout
import com.simple.commonutils.R
import com.simple.commonutils.log
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate")
class CornerConstraintLayout : ConstraintLayout {
    // 代码设置不支持百分比
    var cornerSize: Float = 0f
        set(value) {
            log("set", value)
            valueType[0] = true
            field = value
        }

    // 代码设置不支持百分比
    var topLeftCornerSize: Float = 0f
        set(value) {
            valueType[1] = true
            field = value
        }

    // 代码设置不支持百分比
    var topRightCornerSize: Float = 0f
        set(value) {
            valueType[2] = true
            field = value
        }

    // 代码设置不支持百分比
    var bottomLeftCornerSize: Float = 0f
        set(value) {
            valueType[3] = true
            field = value
        }

    // 代码设置不支持百分比
    var bottomRightCornerSize: Float = 0f
        set(value) {
            valueType[4] = true
            field = value
        }
    var clipColor: Int = Int.MIN_VALUE

    private val valueType = BooleanArray(5)
    private val floatArray = FloatArray(8) { 0f }// 0f need?

    private val path = Path()

    private val outline = object : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if ((configFloatArray().maxOrNull() ?: 0f) > 0f) {
                outline?.setRoundRect(
                    0, 0,
                    width, height,
                    floatArray.maxOrNull() ?: 0f
                )
            } else {
                view?.let {
                    val background = it.background
                    if (background != null) {
                        outline?.let { it1 -> background.getOutline(it1) }
                    } else {
                        outline?.setRect(0, 0, view.width, view.height)
                        outline?.alpha = 0.0f
                    }
                }
            }
        }
    }

    init {
        setWillNotDraw(false)
    }

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val typeArray =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.CornerConstraintLayout,
                defStyleAttr,
                0
            )
        clipColor =
            typeArray.getColor(
                R.styleable.CornerConstraintLayout_shape_background_color,
                -1
            )
        // cornerSize
        var attr = R.styleable.CornerConstraintLayout_shape_corner
        var type: TypedValue
        if (typeArray.hasValue(attr)) {
            type = typeArray.peekValue(attr)
            cornerSize = if (type.type == TypedValue.TYPE_DIMENSION) {
                typeArray.getDimension(attr, 0f)
            } else {
                typeArray.getFloat(attr, 0f)
            }
            valueType[0] = type.type == TypedValue.TYPE_DIMENSION
        }
        // topLeftCornerSize
        attr = R.styleable.CornerConstraintLayout_shape_top_left_corner
        if (typeArray.hasValue(attr)) {
            type = typeArray.peekValue(attr)
            topLeftCornerSize = if (type.type == TypedValue.TYPE_DIMENSION) {
                typeArray.getDimension(attr, 0f)
            } else {
                typeArray.getFloat(attr, 0f)
            }
            valueType[1] = type.type == TypedValue.TYPE_DIMENSION
        }
        // topLeftCornerSize
        attr = R.styleable.CornerConstraintLayout_shape_top_right_corner
        if (typeArray.hasValue(attr)) {
            type = typeArray.peekValue(attr)
            topRightCornerSize = if (type.type == TypedValue.TYPE_DIMENSION) {
                typeArray.getDimension(attr, 0f)
            } else {
                typeArray.getFloat(attr, 0f)
            }
            valueType[2] = type.type == TypedValue.TYPE_DIMENSION
        }
        // topRightCornerSize
        attr = R.styleable.CornerConstraintLayout_shape_bottom_left_corner
        if (typeArray.hasValue(attr)) {
            type = typeArray.peekValue(attr)
            bottomLeftCornerSize = if (type.type == TypedValue.TYPE_DIMENSION) {
                typeArray.getDimension(attr, 0f)
            } else {
                typeArray.getFloat(attr, 0f)
            }
            valueType[3] = type.type == TypedValue.TYPE_DIMENSION
        }
        // bottomRightCornerSize
        attr = R.styleable.CornerConstraintLayout_shape_bottom_right_corner
        if (typeArray.hasValue(attr)) {
            type = typeArray.peekValue(attr)
            bottomRightCornerSize = if (type.type == TypedValue.TYPE_DIMENSION) {
                typeArray.getDimension(attr, 0f)
            } else {
                typeArray.getFloat(attr, 0f)
            }
            valueType[4] = type.type == TypedValue.TYPE_DIMENSION
        }
        outlineProvider = outline
        typeArray.recycle()
    }

    private fun configFloatArray(): FloatArray {
        val min = min(width, height)
        if (cornerSize == 0f) {
            val tl = if (valueType[1]) topLeftCornerSize else topLeftCornerSize * min
            val tr = if (valueType[2]) topRightCornerSize else topRightCornerSize * min
            val bl = if (valueType[3]) bottomRightCornerSize else bottomRightCornerSize * min
            val br = if (valueType[4]) bottomLeftCornerSize else bottomLeftCornerSize * min
            floatArray[0] = tl
            floatArray[1] = tl
            floatArray[2] = tr
            floatArray[3] = tr
            floatArray[4] = bl
            floatArray[5] = bl
            floatArray[6] = br
            floatArray[7] = br
        } else {
            val cc = if (valueType[0]) cornerSize else cornerSize * min
            val size = floatArray.size
            for (i in 0 until size) {
                floatArray[i] = cc
            }
        }
        return floatArray
    }

    override fun onDraw(canvas: Canvas) {
        val floatArray = configFloatArray()
        if ((floatArray.maxOrNull() ?: 0f) > 0f) {
            canvas?.clipPath(
                path.apply {
                    path.reset()
                    this.fillType = Path.FillType.WINDING
                    this.addRoundRect(
                        0f, 0f, width / 1f, height / 1f,
                        floatArray,
                        Path.Direction.CCW
                    )
                }
            )
            if (clipColor != Int.MIN_VALUE) {
                canvas?.drawColor(clipColor)
            }
        }
        super.onDraw(canvas)
    }
}