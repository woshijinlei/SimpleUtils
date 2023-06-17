@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
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
/**
 * 默认一个全透明状态栏和导航栏的style
 *
 * @constructor Create empty Base system bar dialog fragment
 */
abstract class BaseSystemBarDialogFragment : DialogFragment() {

    private val translucent = Color.parseColor("#66000000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TransparentSystemBarDialogTheme)
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater = super.onGetLayoutInflater(savedInstanceState)
        this.isCancelable = isCancel()
        dialog?.window?.apply {
            pureDialogStyle(this)
        }
        return layoutInflater
    }

    private fun pureDialogStyle(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        cutoutShortEdges(window)
        systemBarColor(window)
        if (isLightStatusBar) {
            lightStatusBarCompat(window)
        }
        if (isLightNavigationBar) {
            lightNavigationBarCompat(window)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            if (isHideStatusBar) window.decorView.windowInsetsController?.hide(
                WindowInsets.Type.statusBars(),
            )
            if (isHideNavBar) window.decorView.windowInsetsController?.hide(
                WindowInsets.Type.navigationBars(),
            )
        } else {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or (if (isHideStatusBar) View.SYSTEM_UI_FLAG_FULLSCREEN else 0)
                    or (if (isHideNavBar) View.SYSTEM_UI_FLAG_HIDE_NAVIGATION else 0))
        }
    }

    private fun systemBarColor(window: Window) {
        statusBarColor?.let {
            window.statusBarColor = it
        }
        navigationBarColor?.let {
            window.navigationBarColor = it
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
                    // We want to sure that the status bar can be visible, So we translucent the status bar
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

    open val isLightStatusBar = false
    open val isLightNavigationBar = false
    open val statusBarColor: Int? = null
    open val navigationBarColor: Int? = null
    open val isHideStatusBar = false
    open val isHideNavBar = false

    // Use this instead of directly calling Dialog.setCancelable(boolean)
    abstract fun isCancel(): Boolean
}