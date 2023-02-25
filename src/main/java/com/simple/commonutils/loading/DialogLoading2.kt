//package com.simple.commonutils.loading
//
//import android.content.DialogInterface
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.FragmentManager
//import com.android.dos.aiart.loading.ILoading
//import com.android.dos.databinding.LoadingArtBinding
//import com.android.dos.aiart.views.touchState
//import com.simple.commonutils.dialog.BaseDialogFragment
//
//class DialogLoading2(
//    private val fragmentManager: FragmentManager,
//) : ILoading {
//
//    private var hideCallback: ((isInner: Boolean) -> Unit)? = null
//
//    private var dialogFragment: DialogFragment? = null
//
//    override fun showLoading(backPressedCancelable: Boolean) {
//        dialogFragment = DialogFragment.show(fragmentManager, backPressedCancelable)
//        if (hideCallback != null) {
//            dialogFragment!!.hideCallback = hideCallback
//        }
//    }
//
//    override fun hideLoading() {
//        dialogFragment?.isInner = false
//        dialogFragment?.dismissAllowingStateLoss()
//        dialogFragment = null
//    }
//
//    override fun setCallback(hideCallback: (isInner: Boolean) -> Unit) {
//        this.hideCallback = hideCallback
//        dialogFragment?.hideCallback = hideCallback
//    }
//
//    class DialogFragment : BaseDialogFragment() {
//        private val bind by lazy { LoadingArtBinding.inflate(layoutInflater) }
//        private val backPressedCancelable by lazy { arguments?.getBoolean("isCancel") ?: false }
//
//        var hideCallback: ((isInner: Boolean) -> Unit)? = null
//        var isInner = false
//
//        override fun isCancel(): Boolean {
//            return backPressedCancelable
//        }
//
//        override fun styleType(): Style {
//            return Style.PureDialog
//        }
//
//        override fun onDismiss(dialog: DialogInterface) {
//            super.onDismiss(dialog)
//            hideCallback?.invoke(isInner)
//            hideCallback = null
//        }
//
//        override fun onCreateView(
//            inflater: LayoutInflater,
//            container: ViewGroup?,
//            savedInstanceState: Bundle?
//        ): View? {
//            return bind.root
//        }
//
//        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//            super.onViewCreated(view, savedInstanceState)
//            view.setOnClickListener { }
//            bind.imageView27.touchState(duration = 55)
//            bind.imageView27.setOnClickListener {
//                if (backPressedCancelable) {
//                    isInner = true
//                    dismissAllowingStateLoss()
//                }
//            }
//        }
//
//        companion object {
//            fun show(fragmentManager: FragmentManager, isCancel: Boolean): DialogFragment {
//                return DialogFragment().apply {
//                    this.arguments = Bundle().apply {
//                        this.putBoolean("isCancel", isCancel)
//                    }
//                    show(fragmentManager, "dialogFragment")
//                }
//            }
//        }
//    }
//}