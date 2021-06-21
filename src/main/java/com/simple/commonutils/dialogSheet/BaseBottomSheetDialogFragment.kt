@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialogSheet

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
    private var mCallback: Callback? = null

    interface Callback {
        fun onSuccess(data: Any?)
        fun onFailed(data: Any?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback) {
            this.mCallback = context
        }
        mMarginTop = arguments?.getInt(ExtraMarginTop) ?: -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.apply {
            this.setBackgroundDrawable(null)
            this.attributes = this.attributes.apply attributes@{
                this@apply.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                this.dimAmount = 0.4f
            }
        }
        (dialog as? BottomSheetDialog)?.apply {
            this.behavior.apply {
                this.state = BottomSheetBehavior.STATE_EXPANDED
                this.skipCollapsed = true
            }
            this.dismissWithAnimation = false//直接影响点击outside是hide dialog还是dismiss dialog
        }
        view?.apply {
            this.layoutParams = (this.layoutParams as FrameLayout.LayoutParams).apply {
                this.width = ViewGroup.LayoutParams.MATCH_PARENT
                if (mMarginTop != -1) {
                    this.height = resources.displayMetrics.heightPixels - mMarginTop
                }
            }
            (this.parent as View).background = null
        }

    }

    /**
     * 这个方法中调用了dialog.show()方法（包含内在的dialog的onCreate(),onStart(),mWindowManager.addView(mDecor, l),onShow()流程）
     * 显示decor
     *
     * 我们在dialog.show()方法后改变window属性
     * default:
     * window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
     * window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            this.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            this.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    /**
     * 隐藏decor
     */
    override fun onStop() {
        super.onStop()
    }

    /**
     * remove这个view
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    /**
     * 如果可以cancel，发生在dismiss之前执行
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    fun sendSuccessMessage(data: Any?) {
        mCallback?.onSuccess(data)
    }

    fun sendFailedMessage(data: Any?) {
        mCallback?.onFailed(data)
    }

    companion object {

        private const val ExtraMarginTop = "BaseBottomSheetDialogFragment:ExtraMarginTop"

        fun <T : BaseBottomSheetDialogFragment> showDialog(
            dialog: T,
            fragmentManager: FragmentManager,
            marginTop: Int = -1
        ): T {
            return dialog.apply {
                this.arguments = Bundle().apply {
                    this.putInt(ExtraMarginTop, marginTop)
                }
                show(fragmentManager, "BaseBottomSheetDialogFragment")
            }
        }
    }

}