package com.simple.commonutils.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

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

    protected open val useWindowInsetsController = true

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
        hideOrShowNavBar(isHideNavBar)
        hideOrShowStatusBar(isHideStatusBar)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
            checkLightR()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or (if (!isLightStatusBar) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                    or (if (!isLightNavBar) 0 else View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or (if (!isLightStatusBar) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR))
        }
        // checkLightR() // 不是都设置
    }

    private fun checkLightR() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
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

    protected fun hideOrShowNavBar(isHideNavBar: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
            if (isHideNavBar) {
                window.decorView.windowInsetsController?.hide(WindowInsets.Type.navigationBars())
            }
        } else {
            window.decorView.systemUiVisibility = (
                    if (isHideNavBar) window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    // or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    else window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv())
        }
    }

    protected fun hideOrShowStatusBar(isHideStatusBar: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
            if (isHideStatusBar) {
                window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
            }
        } else {
            window.decorView.systemUiVisibility = (
                    if (isHideStatusBar) window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
                    // or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    else window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN.inv())
        }
    }

    private fun stickySystemBar(stickyInterval: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
            window.decorView.setOnApplyWindowInsetsListener { v, insets ->
                if (viabilityHandler == null) {
                    viabilityHandler = object : Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            when (msg.what) {
                                0 -> {
                                    window.decorView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
                                }

                                1 -> {
                                    window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
                                }

                                2 -> {
                                    window.decorView.windowInsetsController?.hide(WindowInsets.Type.navigationBars())
                                }
                            }
                        }
                    }
                }
                var type = -1
                when {
                    isHideStatusBar && isHideNavBar -> {
                        if (insets.isVisible(WindowInsets.Type.statusBars())
                            || insets.isVisible(WindowInsets.Type.navigationBars())
                        ) {
                            type = 0
                        }
                    }

                    isHideStatusBar -> {
                        if (insets.isVisible(WindowInsets.Type.statusBars())) {
                            type = 1
                        }
                    }

                    isHideNavBar -> {
                        if (insets.isVisible(WindowInsets.Type.navigationBars())) {
                            type = 2
                        }
                    }
                }
                if (type != -1) {
                    viabilityHandler!!.removeCallbacksAndMessages(null)
                    viabilityHandler!!.sendMessageDelayed(
                        Message.obtain().apply { this.what = type }, stickyInterval
                    )
                }
                return@setOnApplyWindowInsetsListener insets
            }
        } else {
            sysVisibility = window.decorView.systemUiVisibility
            window.decorView.setOnSystemUiVisibilityChangeListener {
                if (it != sysVisibility) {
                    if (viabilityHandler == null) {
                        viabilityHandler = Handler(Looper.getMainLooper())
                    }
                    if (resetRunnable == null) {
                        resetRunnable = Runnable {
                            window.decorView.systemUiVisibility = sysVisibility
                        }
                    }
                    viabilityHandler!!.removeCallbacksAndMessages(null)
                    viabilityHandler!!.postDelayed(resetRunnable!!, stickyInterval)
                }
            }
        }
    }

    protected fun contentExtendStatusNav() {
        cutoutMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
            window.setDecorFitsSystemWindows(false)
            window.attributes.isFitInsetsIgnoringVisibility = true
            window.attributes.fitInsetsTypes = WindowInsets.Type.systemBars()
        } else {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    // 注意配合getInsetsIgnoringVisibility，如果不使用SYSTEM_UI_FLAG_LAYOUT_STABLE，则使用getInsets，代表systemBar会跟随移动
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    protected fun contentExtendStatus() {
        cutoutMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
            window.setDecorFitsSystemWindows(false)
            window.attributes.isFitInsetsIgnoringVisibility = true
            window.attributes.fitInsetsTypes = WindowInsets.Type.statusBars()
        } else {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    // 注意配合getInsetsIgnoringVisibility，如果不使用SYSTEM_UI_FLAG_LAYOUT_STABLE，则使用getInsets，代表systemBar会跟随移动
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    protected fun contentExtendNav() {
        cutoutMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useWindowInsetsController) {
            window.setDecorFitsSystemWindows(false)
            window.attributes.isFitInsetsIgnoringVisibility = true
            window.attributes.fitInsetsTypes = WindowInsets.Type.navigationBars()
        } else {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    // 注意配合getInsetsIgnoringVisibility，如果不使用SYSTEM_UI_FLAG_LAYOUT_STABLE，则使用getInsets，代表systemBar会跟随移动
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    protected fun cutoutMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        viabilityHandler?.removeCallbacksAndMessages(null)
    }
}