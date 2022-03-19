@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import com.simple.commonutils.R

//androidx:
//onStart-activity
//onAttach
//onCreate
//onGetLayoutInflater(mDialog = onCreateDialog(savedInstanceState))
//onCreateView
//onViewCreated
//onActivityCreated(mDialog.setContentView(view)mDialog.setCancelable(mCancelable); mDialog.setOnCancelListener(mOnCancelListener); mDialog.setOnDismissListener(mOnDismissListener);)
//onStart
//onStart-activity
//onResume-activity
//onResume
/**
 * 内容区全屏(自定义中间的dialog)，前后台切换动画消除，背景没有dim值，状态栏和导航栏设置
 */
abstract class BaseDialogFragment : DialogFragment() {

    private val handler = Handler(Looper.getMainLooper())

    enum class Style {
        /**
         * 默认状态栏和导航栏
         */
        Default,

        /**
         * 全透明状态栏和默认导航栏
         */
        Transparent,

        /**
         * 半透明状态栏和默认导航栏
         */
        TransLucent,

        /**
         * 全透明状态栏和导航栏，内容区域也是全屏
         * 全透明会造成icon图标在白色背景下变得不可见，可以重写[isLightNavigationBar] [isLightStatusBar]，但是底版本不支持
         */
        PureDialog
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                Style.TransLucent -> {
                    translucentStyle(this)
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

    override fun onResume() {
        super.onResume()
        handler.post { setWindowAnimation(customWindowAnimations()) }
    }

    override fun onStop() {
        clearWindowAnimation()
        super.onStop()
    }

    private fun clearWindowAnimation() {
        dialog?.window?.setWindowAnimations(0)
    }

    private fun setWindowAnimation(windowAnimationStyle: Int) {
        dialog?.window?.setWindowAnimations(windowAnimationStyle)
    }

    private fun transparentStyle(window: Window) {
        window.apply {
            cutoutCompat(this)
            if (isLightStatusBar()) {
                lightStatusBarCompat(this)
            }
            this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun translucentStyle(window: Window) {
        window.apply {
            cutoutCompat(this)
            this.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (isLightStatusBar()) {
                lightStatusBarCompat(this)
            }
            this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun pureDialogStyle(window: Window) {
        window.apply {
            this.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            this.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            this.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            cutoutCompat(this)
            if (isLightStatusBar()) {
                lightStatusBarCompat(this)
            }
            if (isLightNavigationBar()) {
                lightNavigationBarCompat(this)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
            } else {
                this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
        }
    }

    private fun lightStatusBarCompat(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.decorView.windowInsetsController?.show(WindowInsets.Type.statusBars())
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility =
                        window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }
    }

    private fun lightNavigationBarCompat(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.decorView.windowInsetsController?.show(WindowInsets.Type.statusBars())
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                            or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                }
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
    open fun isLightStatusBar() = false
    open fun dimAmount(): Float = 0f
    open fun customWindowAnimations(): Int = android.R.style.Animation_Dialog
    abstract fun isCancel(): Boolean
    abstract fun styleType(): Style
}