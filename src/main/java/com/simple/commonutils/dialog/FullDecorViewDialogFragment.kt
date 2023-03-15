@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.view.setPadding

abstract class FullDecorViewDialogFragment : BaseStyleDialogFragment() {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater = super.onGetLayoutInflater(savedInstanceState)
        dialog?.window?.apply {
            this.decorView.background = null//去除掉默认的InsertDrawable
            this.decorView.setPadding(0)//边缘点击区域问题
            this.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )//将dialog的内容完全由布局来自定义实现
            this.attributes = this.attributes.apply attributes@{
                this@apply.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                this.dimAmount = dimAmount()
            }
            this.decorView.setOnClickListener { }
            this.setWindowAnimations(windowAnimations())
        }
        return layoutInflater
    }

    /**
     * fragment
     */
    override fun onPause() {
        super.onPause()
        dialog?.window?.setWindowAnimations(0)
    }

    override fun onResume() {
        super.onResume()
        handler.post { dialog?.window?.setWindowAnimations(windowAnimations()) }// work
    }

    /**
     * mDialog.hide()->mDecor.setVisibility(View.GONE)
     */
    override fun onStop() {
        super.onStop()
    }

    /**
     * mViewDestroyed = false;
     * mDialog.show();
     * ->
     * mDecor.setVisibility(View.VISIBLE)
     * or
     * mWindowManager.addView(mDecor, l)
     */
    override fun onStart() {
        super.onStart()
    }

    open fun dimAmount(): Float = 0f

    open fun windowAnimations(): Int = 0
}