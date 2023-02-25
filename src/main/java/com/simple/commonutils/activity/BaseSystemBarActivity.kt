package com.simple.commonutils.activity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.CallSuper

/**
 * 默认情况下对system bar不施加任何影响
 */
abstract class BaseSystemBarActivity : AppCompatActivity() {

    protected open val isContentExtendNavbar: Boolean = false

    protected open val isContentExtendStatusBar: Boolean = false

    protected open val isLightStatusBar = false

    protected open val isLightNavBar = false

    protected open val statusBarColor: Int? = null

    protected open val navBarColor: Int? = null

    protected open val isHideStatusBar = false

    protected open val isHideNavBar = false

    protected open val isSticky = false

    private var sysVisibility = 0

    private val stickyInterval = 3000L

    private var viabilityHandler: Handler? = null

    private var resetRunnable: Runnable? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extendContent()
        lightOrDefaultSystemBar()
        colorOrDefaultSystemBar()
        hideOrShowSystemBar()
        interceptSystemBarStyle()
        if (isSticky) {
            stickySystemBar(stickyInterval)
        }
    }

    protected open fun extendContent() {
        when {
            isContentExtendStatusBar && isContentExtendNavbar -> {
                contentExtendStatusNav()
            }
            isContentExtendStatusBar -> {
                contentExtendStatus()
            }
            isContentExtendNavbar -> {
                contentExtendNav()
            }
        }
    }

    protected open fun lightOrDefaultSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or (if (!isLightStatusBar) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                    or (if (!isLightNavBar) 0 else View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or (if (!isLightStatusBar) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR))
        }
        checkLightR()
    }

    private fun checkLightR() {// todo is ok?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isLightStatusBar)
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            if (isLightNavBar)
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
        }
    }

    protected open fun colorOrDefaultSystemBar() {
        statusBarColor?.let {
            window.statusBarColor = it
        }
        navBarColor?.let {
            window.navigationBarColor = it
        }
    }

    open fun interceptSystemBarStyle() {

    }

    private fun hideOrShowSystemBar() {
        window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                or (if (isHideStatusBar) View.SYSTEM_UI_FLAG_FULLSCREEN else 0)
                or (if (isHideNavBar) View.SYSTEM_UI_FLAG_HIDE_NAVIGATION else 0))
    }

    private fun stickySystemBar(stickyInterval: Long) {
        sysVisibility = window.decorView.systemUiVisibility
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it != sysVisibility) {
                if (viabilityHandler == null) {
                    viabilityHandler = Handler(Looper.getMainLooper())
                }
                if (resetRunnable == null) {
                    resetRunnable = Runnable {
                        window.decorView.systemUiVisibility = sysVisibility
                        checkLightR()
                    }
                }
                viabilityHandler!!.removeCallbacks(resetRunnable!!)
                viabilityHandler!!.postDelayed(resetRunnable!!, stickyInterval)
            }
        }
    }

    protected fun contentExtendStatusNav() {
        cutoutMode()
        window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    protected fun contentExtendStatus() {
        cutoutMode()
        window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }


    protected fun contentExtendNav() {
        cutoutMode()
        window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    protected fun cutoutMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
}