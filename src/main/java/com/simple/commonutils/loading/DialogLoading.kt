package com.simple.commonutils.loading

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.simple.commonutils.R
import com.simple.commonutils.dialog.FullDecorViewDialogFragment

class DialogLoading(
    private val layout: Int,
    private val fragmentManager: FragmentManager,
) : ILoading {

    private var hideCallback: ((isInner: Boolean) -> Unit)? = null

    private var dialogFragment: DialogFragment? = null

    override fun showLoading(backPressedCancelable: Boolean) {
        dialogFragment = DialogFragment.show(fragmentManager, backPressedCancelable, layout)
        if (hideCallback != null) {
            dialogFragment!!.hideCallback = hideCallback
        }
    }

    override fun hideLoading() {
        dialogFragment?.isInner = false
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = null
    }

    override fun setCallback(hideCallback: (isInner: Boolean) -> Unit) {
        this.hideCallback = hideCallback
        dialogFragment?.hideCallback = hideCallback
    }

    class DialogFragment : FullDecorViewDialogFragment() {
        private val backPressedCancelable by lazy { arguments?.getBoolean("isCancel") ?: false }

        var hideCallback: ((isInner: Boolean) -> Unit)? = null
        var isInner = false

        override fun isCancel(): Boolean {
            return backPressedCancelable
        }

        override fun styleType(): Style {
            return Style.PureDialog
        }

        override fun windowAnimations(): Int {
            return R.style.AnimationWindowFade
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            hideCallback?.invoke(isInner)
            hideCallback = null
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(arguments?.getInt("layout")!!, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.setOnTouchListener { v, event -> return@setOnTouchListener true }
        }

        companion object {
            fun show(
                fragmentManager: FragmentManager,
                isCancel: Boolean,
                layout: Int
            ): DialogFragment {
                return DialogFragment().apply {
                    this.arguments = Bundle().apply {
                        this.putBoolean("isCancel", isCancel)
                        this.putInt("layout", layout)
                    }
                    show(fragmentManager, "dialogFragment")
                }
            }
        }
    }
}