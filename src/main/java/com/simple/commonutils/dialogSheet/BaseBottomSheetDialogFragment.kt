@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialogSheet

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.commonutils.R

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
open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var mMarginTop = -1
    private var adapter: BottomSheetBehavior.BottomSheetCallback? = null

    interface BottomSheetCallback {
        fun onStateChanged(
            who: BottomSheetDialogFragment,
            bottomSheet: View,
            @BottomSheetBehavior.State newState: Int
        )

        fun onSlide(who: BottomSheetDialogFragment, bottomSheet: View, slideOffset: Float)
    }

    inner class Adapter(private val callback: BottomSheetCallback) :
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
                adapter?.let { this.addBottomSheetCallback(it) }
            }
            this.dismissWithAnimation = true//直接影响点击outside是hide dialog还是dismiss dialog
        }
        dialog?.window?.apply {
            this.setBackgroundDrawable(null)
            this.attributes = this.attributes.apply attributes@{
                this@apply.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                this.dimAmount = dim
            }
            if (isNoLimits) {
                this.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            }
            if (windowAnimationStyle != -1) {
                this.setWindowAnimations(windowAnimationStyle)
            }
            if (naviBarColor != -1) {
                this.navigationBarColor = naviBarColor
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
            (view.parent as View).background = null
        }
    }

    fun hide() {
        (dialog as BottomSheetDialog).apply {
            this.behavior.apply {
                this.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    /**
     * WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS [naviBarColor]不生效
     */
    open val isNoLimits = false
    open val naviBarColor = -1
    open val windowAnimationStyle = -1
    open val dim = 0f
    var canCancel: Boolean = true
        set(value) {
            field = value
            this.isCancelable = canCancel
        }

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