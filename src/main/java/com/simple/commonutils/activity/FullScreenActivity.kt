package com.simple.commonutils.activity

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

/**
 * 内容区域为全屏，同时systemBar为全透明状态
 *
 * 这样我们提供的xml布局(setContentView())就是全屏幕状态，同时可以看到内容区域会延伸到systemBar，是否重写
 * [topAnchorViews] [bottomAnchorViews]可以以margin的形式适应systemBar，一些嵌套的view比如在fragment内部，
 * 无法直接重写这些属性，只能手动调用[fitSystemWindowMargin] [fitSystemWindowMargins]，其他的属性比如
 * systemBar颜色，是否light，是否隐藏，都可以重写对应的属性，这个前提下调整systemBar，相比在系统默认的模式
 * 下调整systemBar，我们能够获得更加直观和可控的效果
 */
abstract class FullScreenActivity : BaseSystemBarActivity() {

    /**
     * 不会被状态栏覆盖，施加了状态栏高度的margin，一般传递一个View，其他view相对这个view布局的
     */
    abstract val topAnchorViews: MutableList<View>?

    /**
     * 不会被导航栏覆盖，施加了导航栏高度的margin，一般传递一个View，其他view相对这个view布局的
     */
    abstract val bottomAnchorViews: MutableList<View>?

    /**
     * 这个属性会影响systemBar是不是light，但是低版本api不支持的情况，systemBar采用半透明或者系统默认样式
     */
    abstract val isWhiteBackground: Boolean

    override val isContentExtendNavbar: Boolean = true

    override val isContentExtendStatusBar: Boolean = true

    override val isLightStatusBar = true

    override val isLightNavBar = true

    override val statusBarColor = Color.TRANSPARENT

    override val navBarColor = navTransparentColor

    override val isSticky = true

    override fun setContentView(view: View?) {
        super.setContentView(view)
        fitSystemWindow()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        fitSystemWindow()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        fitSystemWindow()
    }

    override fun extendContent() {
        val isBlockContentExtentNav =
            isWhiteBackground && Build.VERSION.SDK_INT < Build.VERSION_CODES.O
        when {
            isContentExtendStatusBar && isContentExtendNavbar -> {
                if (isBlockContentExtentNav) {
                    contentExtendStatus()
                } else {
                    contentExtendStatusNav()
                }
            }
            isContentExtendStatusBar -> {
                contentExtendStatus()
            }
            isContentExtendNavbar -> {
                if (!isBlockContentExtentNav) {
                    contentExtendNav()
                } else {
                    cutoutMode()
                }
            }
            else -> {
                cutoutMode()
            }
        }
    }

    override fun colorOrDefaultSystemBar() {
        if (isWhiteBackground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = navTransparentColor
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = Color.TRANSPARENT
            } else {
                window.statusBarColor = translucent
            }
        } else {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = navTransparentColor
        }
        if (statusBarColor != Color.TRANSPARENT) {
            window.statusBarColor = statusBarColor
        }
        if (navBarColor != navTransparentColor) {
            window.navigationBarColor = navBarColor
        }
    }

    private fun fitSystemWindow() {
        val common = topAnchorViews?.filter { bottomAnchorViews?.contains(it) == true }
        common?.forEach {
            it.fitSystemWindowMargins(it.marginTop, it.marginBottom)
        }
        topAnchorViews?.filter { common?.contains(it) != true }
            ?.forEach { it.fitSystemWindowMargin(it.marginTop, true) }
        bottomAnchorViews?.filter { common?.contains(it) != true }
            ?.forEach { it.fitSystemWindowMargin(it.marginBottom, false) }
    }

    companion object {
        private val translucent = Color.parseColor("#40000000")
        private val navTransparentColor = Color.parseColor("#01000000")

        fun View.fitSystemWindowMargin(margin: Int, isAlignTop: Boolean) {
            setOnApplyWindowInsetsListener { v, insets ->
                val i = WindowInsetsCompat.toWindowInsetsCompat(insets)
                val s = i.getInsets(WindowInsetsCompat.Type.statusBars())
                val n = i.getInsets(WindowInsetsCompat.Type.navigationBars())
                (v?.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    if (isAlignTop) {
                        this.updateMarginsRelative(top = s.top + margin)
                    } else {
                        this.updateMarginsRelative(bottom = n.bottom + margin)
                    }
                }
                insets
            }
        }

        fun View.fitSystemWindowMargins(marginTop: Int, marginBottom: Int) {
            setOnApplyWindowInsetsListener { v, insets ->
                val i = WindowInsetsCompat.toWindowInsetsCompat(insets)
                val s = i.getInsets(WindowInsetsCompat.Type.statusBars())
                val n = i.getInsets(WindowInsetsCompat.Type.navigationBars())
                (v?.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    this.updateMarginsRelative(
                        top = s.top + marginTop,
                        bottom = n.bottom + marginBottom
                    )
                }
                insets
            }
        }

        fun ViewGroup.fitSystemWindowPadding(padding: Int, isAlignTop: Boolean) {
            setOnApplyWindowInsetsListener { v, insets ->
                val i = WindowInsetsCompat.toWindowInsetsCompat(insets)
                val s = i.getInsets(WindowInsetsCompat.Type.statusBars())
                val n = i.getInsets(WindowInsetsCompat.Type.navigationBars())
                if (isAlignTop) {
                    this.updatePadding(top = s.top + padding)
                } else {
                    this.updatePadding(bottom = n.bottom + padding)
                }
                insets
            }
        }

        fun ViewGroup.fitSystemWindowPaddings(paddingTop: Int, paddingBottom: Int) {
            setOnApplyWindowInsetsListener { v, insets ->
                val i = WindowInsetsCompat.toWindowInsetsCompat(insets)
                val s = i.getInsets(WindowInsetsCompat.Type.statusBars())
                val n = i.getInsets(WindowInsetsCompat.Type.navigationBars())
                this.updatePadding(top = s.top + paddingTop, bottom = n.bottom + paddingBottom)
                insets
            }
        }
    }
}