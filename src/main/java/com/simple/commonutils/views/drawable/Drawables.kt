package com.simple.commonutils.views.drawable

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

fun shapeRippleDrawable(
    content: Drawable,
    mask: Drawable?,
    rippleColor: Int = Color.LTGRAY
): LayerDrawable {
    return LayerDrawable(
        arrayOf(
            content,
            RippleDrawable(ColorStateList.valueOf(rippleColor), null, mask),
        )
    )
}

fun MaterialShapeDrawable.config(
    solidColor: Int,
    elevation: Float,
    topRadius: Float,
    bottomRadius: Float,
    shadowColor: Int? = null,
    needOffset: Boolean = true,
    compat: Boolean = true
): MaterialShapeDrawable {
    return this.apply {
        if (compat) {
            this.shadowCompatibilityMode =
                MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
        }
        this.fillColor = ColorStateList.valueOf(solidColor)
        shadowColor?.let { this.setShadowColor(it) }
        this.elevation = elevation
        if (needOffset) {
            val clz = MaterialShapeDrawable::class.java
            val m = clz.getDeclaredMethod("setShadowVerticalOffset", Int::class.java)
            m.invoke(this, 8)
        }
        this.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(topRadius)
            .setTopRightCornerSize(topRadius)
            .setBottomLeftCornerSize(bottomRadius)
            .setBottomRightCornerSize(bottomRadius)
            .build()
    }
}