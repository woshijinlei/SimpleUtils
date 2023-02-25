//package com.simple.commonutils.loading
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.view.animation.AlphaAnimation
//import androidx.activity.OnBackPressedCallback
//import androidx.activity.OnBackPressedDispatcher
//import com.android.dos.R
//import com.android.dos.databinding.LoadingArtBinding
//import com.android.dos.aiart.views.touchState
//import com.android.dos.databinding.LoadingProgressBinding
//
//class ViewProgressLoading(
//    private val decorView: ViewGroup,
//    backPressedDispatcher: OnBackPressedDispatcher,
//) : ILoading {
//
//    private var hideCallback: ((isInner: Boolean) -> Unit)? = null
//
//    private val backPressedCallback = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//            if (backPressedCancelable) {
//                isInner = true
//                hideLoading()
//            } else {
//                // toast for wait or nothing
//            }
//        }
//    }
//
//    private var bind: LoadingProgressBinding
//
//    private var backPressedCancelable = false
//    private var isInner = false
//
//    init {
//        backPressedDispatcher.addCallback(backPressedCallback.apply { this.isEnabled = false })
//        bind = LoadingProgressBinding.inflate(// todo lazy inflate
//            LayoutInflater.from(decorView.context),
//            decorView,
//            false
//        )
//        bind.root.setOnClickListener { }// consume
//    }
//
//    override fun showLoading(backPressedCancelable: Boolean) {
//        (decorView).apply {
//            (this@ViewProgressLoading).backPressedCancelable = backPressedCancelable
//            bind.root.animation = AlphaAnimation(0f, 1f).apply {
//                this.duration = 200
//            }
//            this.addView(bind.root)
//            backPressedCallback.isEnabled = true
//        }
//    }
//
//    override fun hideLoading() {
//        (decorView).apply {
//            val c = this.childCount - 1
//            if (this.getChildAt(c) == bind.root) {
//                bind.root.animation = AlphaAnimation(1f, 0f).apply {
//                    this.duration = 200
//                }
//                this.removeViewAt(c)
//                hideCallback?.invoke(isInner)
//            }
//            backPressedCallback.isEnabled = false
//        }
//    }
//
//    override fun setCallback(hideCallback: (isInner: Boolean) -> Unit) {
//        this.hideCallback = hideCallback
//    }
//}