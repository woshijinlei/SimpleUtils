@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Px
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

abstract class PureDialogFragment : DialogFragment() {

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater = super.onGetLayoutInflater(savedInstanceState)
        dialog?.apply {
            simpleDialogConfig(this)
        }
        return layoutInflater
    }

    abstract fun simpleDialogConfig(dialog: Dialog): SimpleDialogConfig

    // Use this instead of directly calling Dialog.setCancelable(boolean)
    abstract fun isCancel(): Boolean
}

class SimpleDialogConfig(private val dialog: Dialog) {

    fun setContentView(view: View): SimpleDialogConfig {
        dialog.apply {
            this.window?.decorView?.background = null//去除掉默认的InsertDrawable
            this.window?.decorView?.setPadding(0)//边缘点击区域问题
            this.setContentView(view)
        }
        return this@SimpleDialogConfig
    }

    fun setContentView(res: Int): SimpleDialogConfig {
        dialog.apply {
            this.window?.decorView?.background = null//去除掉默认的InsertDrawable
            this.window?.decorView?.setPadding(0)//边缘点击区域问题
            this.setContentView(res)
        }
        return this@SimpleDialogConfig
    }

    fun setWidthByMarginHorizontal(@Px margin: Int): SimpleDialogConfig {
        dialog.apply {
            val point = Point()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.context.display?.getRealSize(point)
            } else {
                val wm = this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.defaultDisplay.getRealSize(point)
            }
            this.window?.setLayout(
                if (margin == 0) ViewGroup.LayoutParams.MATCH_PARENT else point.x - margin * 2,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return this@SimpleDialogConfig
    }

    fun setCorner(
        @Px corner: Float, decorBackgroundColor: Int = Color.WHITE
    ): SimpleDialogConfig {
        dialog.apply {
            setCorner(corner, corner, corner, corner, decorBackgroundColor)
        }
        return this@SimpleDialogConfig
    }

    fun setCorner(
        @Px cornerLeft: Float = 0f,
        @Px cornerTop: Float = 0f,
        @Px cornerRight: Float = 0f,
        @Px cornerBottom: Float = 0f,
        decorBackgroundColor: Int = Color.WHITE
    ): SimpleDialogConfig {
        dialog.apply {
            this.window?.decorView!!.apply {
                val s = ShapeAppearanceModel.Builder().setTopLeftCornerSize(cornerLeft)
                    .setTopRightCornerSize(cornerTop).setBottomLeftCornerSize(cornerRight)
                    .setBottomRightCornerSize(cornerBottom).build()
                this.background = MaterialShapeDrawable(s).apply {
                    this.fillColor = ColorStateList.valueOf(decorBackgroundColor)
                }
            }
        }
        return this@SimpleDialogConfig
    }

    fun setElevation(@Px elevation: Float): SimpleDialogConfig {
        dialog.apply {
            this.window?.decorView!!.elevation = elevation
        }
        return this@SimpleDialogConfig
    }

    fun setDimAmount(dimAmount: Float): SimpleDialogConfig {
        dialog.apply {
            this.window?.attributes?.apply {
                this.flags = this.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                this.dimAmount = dimAmount
            }
        }
        return this@SimpleDialogConfig
    }

    fun setXY(x: Int, y: Int): SimpleDialogConfig {
        dialog.apply {
            this.window?.attributes?.apply {
                this.x = x
                this.y = y
            }
        }
        return this@SimpleDialogConfig
    }

    fun setWindowAnimations(anim: Int): SimpleDialogConfig {
        dialog.apply {
            this.window?.setWindowAnimations(anim)
        }
        return this@SimpleDialogConfig
    }

    fun setGravity(gravity: Int): SimpleDialogConfig {
        dialog.apply {
            this.window?.setGravity(gravity)
        }
        return this@SimpleDialogConfig
    }

    fun setCancelable(cancel: Boolean): SimpleDialogConfig {
        dialog.apply {
            this.setCancelable(cancel)
        }
        return this@SimpleDialogConfig
    }

    fun setCanceledOnTouchOutside(cancel: Boolean): SimpleDialogConfig {
        dialog.apply {
            this.setCanceledOnTouchOutside(cancel)
        }
        return this@SimpleDialogConfig
    }
}
