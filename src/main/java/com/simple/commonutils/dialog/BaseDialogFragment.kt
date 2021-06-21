@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import com.simple.commonutils.R
import com.simple.commonutils.log

/**
 * 内容区全屏(自定义中间的dialog)，前后台切换动画消除，背景没有dim值，状态栏和导航栏设置
 */
abstract class BaseDialogFragment : DialogFragment() {

    enum class Style {
        /**
         * 黑色状态栏和导航栏
         */
        Default,

        /**
         * 全透明状态栏和黑色导航栏
         */
        Transparent,

        /**
         * 1.内容非全屏，类似AlertDialog
         * 2.内容全屏，全透明状态栏和导航栏
         * 全透明会造成icon图标在白色背景下变得不可见，可以重写[isLightNavigationBar]，但是底版本不支持
         */
        PureDialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.apply {
            isCancelable = isCancel()
            this.decorView.background = null//去除掉默认的InsertDrawable
            this.decorView.setPadding(0)//边缘点击区域问题
            when (styleType()) {
                Style.Default -> {

                }
                Style.Transparent -> {
                    transparentStyle(this)
                }
                Style.PureDialog -> {
                    pureDialogStyle(this)
                }
            }
            this.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )//将dialog的内容完全由布局来自定义实现
            this.attributes = this.attributes.apply attributes@{
                this@apply.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                this.dimAmount = dimAmount()
            }
            this.setWindowAnimations(customWindowAnimations())
        }
    }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (styleType() == Style.Transparent) {
            setStyle(STYLE_NO_TITLE, R.style.FloatingDialogTheme)
        } else {
            setStyle(STYLE_NO_TITLE, 0)
        }
    }

    override fun onStart() {
        super.onStart()
        log("BaseDialogFragment", "onStart")
        Handler(Looper.getMainLooper()).post {
            setWindowAnimation(customWindowAnimations())
        }
    }

    override fun onStop() {
        clearWindowAnimation()
        super.onStop()
        log("BaseDialogFragment", "onStop")
    }

    private fun clearWindowAnimation() {
        dialog?.window?.setWindowAnimations(0)
    }

    private fun setWindowAnimation(windowAnimationStyle: Int) {
        dialog?.window?.setWindowAnimations(windowAnimationStyle)
    }

    private fun transparentStyle(window: Window) {
        window.apply {
            lightStatusBarCompat(this)
            cutoutCompat(this)
            this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun pureDialogStyle(window: Window) {
        window.apply {
            this.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            this.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            lightStatusBarCompat(this)
            cutoutCompat(this)
            lightNavigationBarCompat(this)
            this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun lightStatusBarCompat(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun lightNavigationBarCompat(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isLightNavigationBar()) {
                this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            }
        }
    }

    private fun cutoutCompat(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                this.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    open fun isLightNavigationBar() = false
    open fun dimAmount(): Float = 0f
    open fun customWindowAnimations(): Int = android.R.style.Animation_Dialog
    abstract fun isCancel(): Boolean
    abstract fun styleType(): Style
}