@file:Suppress("SameParameterValue")

package com.simple.commonutils.sheetDialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.commonutils.R
import com.simple.commonutils.activity.FullScreenActivity.Companion.fitSystemWindowMargin
import com.simple.commonutils.activity.FullScreenActivity.Companion.fitSystemWindowMargins

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
abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    /**
     * 不会被状态栏覆盖，施加了状态栏高度的margin，一般传递一个View，其他view相对这个view布局的
     */
    abstract val topAnchorViews: MutableList<View>?

    /**
     * 不会被导航栏覆盖，施加了导航栏高度的margin，一般传递一个View，其他view相对这个view布局的
     */
    abstract val bottomAnchorViews: MutableList<View>?

    private var mMarginTop = -1
    private var adapter: Adapter? = null
    private val translucent = Color.parseColor("#66000000")

    interface BottomSheetCallback {
        fun onStateChanged(
            who: BottomSheetDialogFragment,
            bottomSheet: View,
            @BottomSheetBehavior.State newState: Int
        )

        fun onSlide(who: BottomSheetDialogFragment, bottomSheet: View, slideOffset: Float)

        fun onDismiss(who: BottomSheetDialogFragment)
    }

    inner class Adapter(val callback: BottomSheetCallback) :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            callback.onStateChanged(this@BaseBottomSheetDialogFragment, bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            callback.onSlide(this@BaseBottomSheetDialogFragment, bottomSheet, slideOffset)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomSheetCallback) {
            adapter = Adapter(context)
        }
        mMarginTop = arguments?.getInt(ExtraMarginTop) ?: -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TransparentSystemBarDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = canCancel

        (dialog as? BottomSheetDialog)?.apply {
            this.behavior.apply {
                this.state = BottomSheetBehavior.STATE_EXPANDED
                this.skipCollapsed = true
                this.isDraggable = canDraggable
                adapter?.let { this.addBottomSheetCallback(it) }
            }
            this.dismissWithAnimation = true//直接影响点击outside是hide dialog还是dismiss dialog
        }
        dialog?.window?.apply {
            this.setBackgroundDrawable(null)
            pureDialogStyle(this)
            this.attributes = this.attributes.apply attributes@{
                this@apply.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                this.dimAmount = dim
            }
            if (windowAnimationStyle != -1) {
                this.setWindowAnimations(windowAnimationStyle)
            }
        }
        view.apply {
            doOnPreDraw {
                this.layoutParams = (this.layoutParams as FrameLayout.LayoutParams).apply {
                    this.width = ViewGroup.LayoutParams.MATCH_PARENT
                    this.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    if (mMarginTop != -1) {
                        val parentHeight =
                            dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.coordinator)?.measuredHeight
                                ?: resources.displayMetrics.heightPixels
                        this.height = parentHeight - mMarginTop
                    }
                }
            }
            dialog?.window?.decorView?.apply {
                findViewById<ViewGroup>(com.google.android.material.R.id.coordinator)
                    ?.apply {
                        this.clipChildren = false
                        this.fitsSystemWindows = false
                        this.setOnApplyWindowInsetsListener { v, insets ->
                            return@setOnApplyWindowInsetsListener insets
                        }
                    }
                findViewById<ViewGroup>(com.google.android.material.R.id.container)
                    ?.apply {
                        this.fitsSystemWindows = false
                        this.setOnApplyWindowInsetsListener { v, insets ->
                            return@setOnApplyWindowInsetsListener insets
                        }
                    }
            }
            (view.parent as ViewGroup).apply {
                this.background = null
            }
        }
        fitSystemWindow()
    }

    private fun pureDialogStyle(window: Window) {
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
    /**
     * remove这个view
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        adapter?.callback?.onDismiss(this)
    }

    fun hide() {
        (dialog as BottomSheetDialog).apply {
            this.behavior.apply {
                this.state = BottomSheetBehavior.STATE_HIDDEN
            }
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

    var canCancel: Boolean = true
        set(value) {
            field = value
            this.isCancelable = canCancel
        }

    open val isLightStatusBar = false
    open val isLightNavigationBar = false
    open val navigationBarColor: Int? = null
    open val isHideStatusBar = false
    open val isHideNavBar = false

    open val windowAnimationStyle = -1
    open val dim = 0f
    open val canDraggable = true

    companion object {

        private const val ExtraMarginTop = "BaseBottomSheetDialogFragment:ExtraMarginTop"

        /**
         * @param marginTop 取0则是铺满高度
         */
        fun <T : BaseBottomSheetDialogFragment> showDialog(
            dialog: T,
            fragmentManager: FragmentManager,
            marginTop: Int = -1
        ): T {
            return dialog.apply {
                if (this.arguments == null) {
                    this.arguments = Bundle()
                }
                this.arguments?.apply {
                    this.putInt(ExtraMarginTop, marginTop)
                }
                show(fragmentManager, "BaseBottomSheetDialogFragment")
            }
        }
    }
}