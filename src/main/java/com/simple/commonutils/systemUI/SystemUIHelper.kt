package com.simple.commonutils.systemUI

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.RequiresApi

/**
 * Theme中不需要设置任何SystemUI相关的配置,Theme.AppCompat.Light.NoActionBar即可
 */
class SystemUIHelper(private val window: Window) {
    private val handler = Handler(Looper.getMainLooper())
    private val commonDelay = 3500L

    /**
     * 控制statusBar的显示和隐藏
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun hideStatusBar(
        isSticky: Boolean = true,
        isContentExtendNav: Boolean = false,
        stickyDuration: Long = commonDelay
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        hideStatusBarByDecorView(isSticky, isContentExtendNav, stickyDuration)
    }

    /**
     * 隐藏掉statusBar和navigationBar,并且全屏显示,statusBar和navigationBar颜色为指定的默认颜色
     * 可以自定义指定控制状态栏和导航栏呈现时间
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun hideStatusNavigationBar(isSticky: Boolean = true, stickyDuration: Long = commonDelay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        hideStatusNavigationByDecorView(isSticky, stickyDuration)
    }

    /**
     * 隐藏掉statusBar和navigationBar,并且全屏显示,statusBar和navigationBar都为半透明颜色
     * 由系统控制状态栏和导航栏呈现时间
     */
    fun hideStatusNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    /**
     * 类似hideStatusNavigation(),但是由系统控制,statusBar和navigationBar都为半透明颜色
     * 触摸边界，会重新出现状态栏和导航栏（区别： lean模式是点击屏幕）
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun immersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        val ui =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.decorView.systemUiVisibility = ui
    }

    /**
     * 点击屏幕，就会恢复,statusBar和navigationBar颜色为指定的默认颜色
     */
    fun leanBackMode(isSticky: Boolean = true, stickyDuration: Long = commonDelay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        val visibly = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.decorView.systemUiVisibility = visibly
        if (isSticky) {
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility != visibly) {
                    handler.postDelayed({
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            window.decorView.systemUiVisibility = visibly
                        }
                    }, stickyDuration)
                } else {
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
                }
            }
        }
    }

    /**
     * 1.使statusBar和navigationBar变得完全透明
     * 2.布局内容全部充满
     * main主页效果(无法设置状态栏和导航栏颜色)
     */
    fun transparentStatusNavigationBar(isHideNavigationBar: Boolean = false) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (isHideNavigationBar) {
            hideNavigationBar()
        }
    }

    /**
     * 默认内容区延伸到状态栏
     */
    fun translucentStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        contentStableFullStatus()
    }

    /**
     * 默认内容区延伸到状态栏
     */
    fun transparentStatusBar(isLightStatusBar: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (isLightStatusBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        )
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
        window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * 内容全屏
     */
    @SuppressLint("InlinedApi")
    fun contentStableFull() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    /**
     * 内容延伸到状态栏
     */
    fun contentStableFullStatus() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    /**
     * 内容在content区域
     */
    @SuppressLint("InlinedApi")
    fun contentStable() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    /**
     * 系统会自动隐藏statusBar,但是不会隐藏navigationBar
     * bug，发现statusBar会有黑边
     */
    fun fullScreenByWindowFlag() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun hideNavigationBar(isContentExtendNav: Boolean = false) {
        window.decorView.systemUiVisibility =
            if (isContentExtendNav) {
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            } else {
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            }
    }

    /**
     * 设置状态栏的颜色
     * main主页效果(推荐theme中设置)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarColor(color: Int, isHideNavigationBar: Boolean = false) {
        window.statusBarColor = color
        val temp = if (color == Color.WHITE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else 0
        } else 0
        if (isHideNavigationBar) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or temp
        } else {
            window.decorView.systemUiVisibility = temp
        }

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun hideStatusNavigationByDecorView(
        isSticky: Boolean,
        stickyDuration: Long
    ) {
        val visibly = (
                // Enables regular immersive mode.
                // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
                // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        window.decorView.systemUiVisibility = visibly
        if (isSticky) {
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility != visibly) {
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
                    handler.postDelayed({
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            window.decorView.systemUiVisibility = visibly
                        }
                    }, stickyDuration.toLong())
                } else {
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun hideStatusBarByDecorView(
        isSticky: Boolean,
        isContentFull: Boolean,
        stickyDuration: Long
    ) {
        val visibly = if (isContentFull) (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                ) else (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        window.decorView.systemUiVisibility = visibly
        if (isSticky) {
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                val v = if (isContentFull) {
                    (View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            )
                } else {
                    (View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
                }
                if (visibility != v) {
                    handler.postDelayed({
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            window.decorView.systemUiVisibility = v
                        }
                    }, stickyDuration)
                }

            }
        }
    }

    /**
     * clear
     */
    fun clear() {
        window.decorView.systemUiVisibility = 0//恢复常态
    }

    fun getNavigationBarHeight(context: Context): Int {
        val rid = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (rid != 0) {
            context.resources.getDimensionPixelSize(
                context.resources.getIdentifier(
                    "navigation_bar_height",
                    "dimen",
                    "android"
                )
            )
        } else 0
    }

    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    //-----------------------------------下面的方法真机上效果都不能正常运行（华为）--------------------------------------------

    //api19 -- 29（状态栏或隐藏一些图标，并可变暗）
    private fun dimLowProfile() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
    }

    /**
     * 显示导航栏和状态栏，content全屏显示
     */
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }


}