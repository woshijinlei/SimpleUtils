package com.simple.commonutils.systemUI

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.RequiresApi
import com.simple.commonutils.log

/**
 * Theme中不需要设置SystemUI相关的配置
 * 部分api兼容R版本
 */
class SystemUIHelper(private val window: Window) {
    private val handler = Handler(Looper.getMainLooper())
    private val commonDelay = 3000L

    private val TRANSLUCENT = Color.parseColor("#66000000")

    /**
     * 1.隐藏statusBar
     * 2.内容延伸到statusBar
     * 3.下滑statusBar内容区并不是stable，但是保证内容不被遮盖
     */
    fun hideStatusBarResizeR(
        isSticky: Boolean = true,
        stickyDuration: Long = commonDelay
    ) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        cutoutShortEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hideStatusBarApiR(isSticky, stickyDuration)
        } else {
            hideStatusBarByDecorView(
                isSticky = isSticky,
                isContentExtendNav = false,
                stickyDuration = stickyDuration,
                instanceShow = true
            )
        }
    }

    /**
     * 1.隐藏statusBar
     * 2.内容延伸到statusBar
     * 3.内容区并是stable的，不会跟随statusBar下滑
     */
    fun hideStatusBar(
        isSticky: Boolean = true,
        stickyDuration: Long = commonDelay
    ) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        cutoutShortEdge()
        hideStatusBarByDecorView(isSticky, false, stickyDuration, false)
    }

    /**
     * 1.隐藏statusBar和navigationBar
     * 2.全屏显示内容
     */
    fun hideStatusNavigationBarR(isSticky: Boolean = true, stickyDuration: Long = commonDelay) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        cutoutShortEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hideStatusNavigationBarApiR(isSticky)
        } else {
            hideStatusNavigationByDecorView(isSticky, stickyDuration)
        }
    }

    /**
     * 1.statusBar半透明
     * 2.内容区延伸到状态栏
     */
    fun translucentStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = TRANSLUCENT
        contentStableExtendStatusBar()
    }

    /**
     * 1.statusBar全透明
     * 2.内容区延伸到状态栏
     */
    fun transparentStatusBar(isLightStatusBar: Boolean = true) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        cutoutShortEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = if (isLightStatusBar) {
                (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            } else {
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
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
     * 1.statusBar全透明
     * 2.内容区不会延伸到状态栏，这是和[transparentStatusBar]主要区别
     * 3.这个方法主要是因为内容区背景为白色时，状态栏会重叠
     */
    fun lightStatusBarR(statusBarColor: Int = Color.TRANSPARENT) {
        window.statusBarColor = statusBarColor
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.show(WindowInsets.Type.statusBars())
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            var s = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                s = s or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                if (window.statusBarColor == Color.TRANSPARENT) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
            window.decorView.systemUiVisibility = s
        }
    }

    /**
     * 1.使statusBar和navigationBar变得完全透明
     * 2.布局内容全部充满
     * 用在启动页，其他场景不好控制(无法设置状态栏和导航栏颜色)
     */
    fun transparentStatusNavigationBar(isHideNavigationBar: Boolean = false) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (isHideNavigationBar) hideNavigationBar()
    }

    /**
     * 触摸边界，会重新出现状态栏和导航栏，并且会自动显示隐藏（区别： lean模式是点击屏幕）
     * statusBar和navigationBar都为半透明颜色
     */
    fun immersiveMode() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        cutoutShortEdge()
        val ui =
            // immersive
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    // hide systemBar
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    // layout
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.decorView.systemUiVisibility = ui
    }

    /**
     * 点击屏幕，就会恢复状态栏和导航栏
     * statusBar和navigationBar颜色为指定的默认颜色
     */
    @Deprecated("R版本已经没有作用")
    private fun leanBackMode(isSticky: Boolean = true, stickyDuration: Long = commonDelay) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        cutoutShortEdge()
        val visibly =
            // hide systemBar
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    // layout
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.decorView.systemUiVisibility = visibly
        if (isSticky) {
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility != visibly) {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            window.decorView.systemUiVisibility = visibly
                        }
                    }, stickyDuration)
                }
            }
        }
    }

    /**
     * clear systemUiVisibility
     */
    fun clear() {
        window.decorView.systemUiVisibility = 0//恢复常态
    }

    /**
     * 系统会自动隐藏statusBar且为半透明
     * 内容延伸到statusBar
     * 不会隐藏navigationBar
     * 发现statusBar会有黑边
     */
    private fun hideStatusBarByWindowFlag() {
        cutoutShortEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 1.statusBar半透明
     * 2.内容区延伸到状态栏
     */
    private fun translucentStatusBarByWindowFlag() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        contentStableExtendStatusBar()
    }

    private fun cutoutShortEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideStatusBarApiR(isSticky: Boolean = true, stickyDuration: Long) {
        window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
        if (isSticky) {
            window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                log("windowInsets", insets.toString())
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    if (insets.isVisible(WindowInsets.Type.statusBars())) {
                        window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
                    }
                }, stickyDuration)
                return@setOnApplyWindowInsetsListener window.decorView.onApplyWindowInsets(insets)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideStatusNavigationBarApiR(isSticky: Boolean = true) {
        window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
        window.decorView.windowInsetsController?.hide(WindowInsets.Type.navigationBars())
        window.setDecorFitsSystemWindows(false)
        if (isSticky) {
            window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                log("windowInsets", insets.toString())
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    if (insets.isVisible(WindowInsets.Type.statusBars())) {
                        window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
                    }
                    if (insets.isVisible(WindowInsets.Type.navigationBars())) {
                        window.decorView.windowInsetsController?.hide(WindowInsets.Type.navigationBars())
                    }
                }, commonDelay)
                return@setOnApplyWindowInsetsListener window.decorView.onApplyWindowInsets(insets)
            }
        }
    }

    private fun contentStableExtendStatusBar() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun hideStatusNavigationByDecorView(
        isSticky: Boolean,
        stickyDuration: Long
    ) {
        val visibly = (
                // Enables regular immersive mode.
                // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
                // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                View.SYSTEM_UI_FLAG_IMMERSIVE// SYSTEM_UI_FLAG_HIDE_NAVIGATION没有这个FLAG配合使用时，点击交互会先弹出来systemBar
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
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            window.decorView.systemUiVisibility = visibly
                        }
                    }, stickyDuration)
                }
            }
        }
    }

    private fun hideStatusBarByDecorView(
        isSticky: Boolean,
        isContentExtendNav: Boolean,
        stickyDuration: Long,
        instanceShow: Boolean = false
    ) {
        val visibly = if (isContentExtendNav)
            (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        else (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = visibly
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility != visibly) {
                if (instanceShow) {
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        window.decorView.systemUiVisibility = 0
                    }
                }
                if (isSticky) {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            window.decorView.systemUiVisibility = visibly
                        }
                    }, stickyDuration)
                }
            }
        }
    }

    private fun hideNavigationBar() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }


    fun getNavigationBarHeight(context: Context): Int {
        val rid = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (rid != 0) {
            context.resources.getDimensionPixelSize(
                context.resources.getIdentifier(
                    "navigation_bar_height",
                    "dimen", "android"
                )
            )
        } else 0
    }

    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    fun readSystemBarColor(context: Context): Pair<Int, Int> {
        // Reads the colors for navigation and status bar from resources.
        val typedArray = context.obtainStyledAttributes(
            intArrayOf(
                android.R.attr.statusBarColor,
                android.R.attr.navigationBarColor
            )
        )
        val statusBarColor = typedArray.getColor(0, 0)
        val navigationBarColor = typedArray.getColor(1, 0)
        typedArray.recycle()
        return statusBarColor to navigationBarColor
    }

}