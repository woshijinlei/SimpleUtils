@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
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
//onViewStateRestored
//onStart
//onStart-activity
//onResume-activity
//onResume
abstract class BaseStyleDialogFragment : DialogFragment() {

    private val translucent = Color.parseColor("#66000000")

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
         * 全透明状态栏和导航栏，内容区域全屏
         * 根布局需要配合android:fitsSystemWindows="true"使用
         */
        PureDialog
    }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            styleType() == Style.Transparent -> {
                setStyle(STYLE_NO_TITLE, R.style.TransparentStatusBarDialogTheme)
            }
            styleType() == Style.TransLucent -> {
                setStyle(STYLE_NO_TITLE, R.style.TranslucentStatusBarDialogTheme)
            }
            styleType() == Style.PureDialog -> {
                setStyle(STYLE_NO_TITLE, R.style.TransparentSystemBarDialogTheme)
            }
            else -> setStyle(STYLE_NO_TITLE, R.style.DialogThemeDefault)
        }
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater = super.onGetLayoutInflater(savedInstanceState)
        this.isCancelable = isCancel()
        dialog?.window?.apply {
            when (styleType()) {
                Style.Default -> {}
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
        }
        return layoutInflater
    }

    private fun transparentStyle(window: Window) {
        window.apply {
            cutoutShortEdges(this)
            if (isLightStatusBar() || (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && isWhiteContent())) {
                lightStatusBarCompat(this)
            }
            this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun translucentStyle(window: Window) {
        window.apply {
            cutoutShortEdges(this)
            this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun pureDialogStyle(window: Window) {
        window.apply {
            this.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            this.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            cutoutShortEdges(this)
            if (isLightStatusBar() || (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && isWhiteContent())) {
                lightStatusBarCompat(this)
            }
            if (isLightNavigationBar() || (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && isWhiteContent())) {
                lightNavigationBarCompat(this)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.setDecorFitsSystemWindows(false)
            } else {
                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.O)) {
                    this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                    if (!isWhiteContent()) {
                        this.decorView.systemUiVisibility =
                            this.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        window.navigationBarColor = Color.TRANSPARENT
                    }
                } else {
                    this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                }
            }
        }
    }

    /**
     * If the SDK_INT is less than M, We set the status bar color translucent.
     */
    private fun lightStatusBarCompat(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility =
                        window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    // We want to sure that the status bar can visible, So we translucent the status bar
                    window.statusBarColor = translucent
                }
            }
        }
    }

    /**
     * If the SDK_INT is less than O, The navigation bar color is default.
     */
    private fun lightNavigationBarCompat(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.decorView.systemUiVisibility = (this.decorView.systemUiVisibility
                            or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                } else {
                    // default nav color
                }
            }
        }
    }

    private fun cutoutShortEdges(window: Window) {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                this.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    open fun isWhiteContent() = false
    open fun isLightStatusBar() = false
    open fun isLightNavigationBar() = false

    // Use this instead of directly calling Dialog.setCancelable(boolean)
    abstract fun isCancel(): Boolean
    abstract fun styleType(): Style
}