@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.setPadding

abstract class FullScreenDialogFragment : BaseSystemBarDialogFragment() {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater = super.onGetLayoutInflater(savedInstanceState)
        dialog?.window?.apply {
            this.decorView.background = null//去除掉默认的InsertDrawable
            this.decorView.setPadding(0)//边缘点击区域问题
            this.setWindowAnimations(0)
            this.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )//将dialog的内容完全由布局来自定义实现
            this.attributes = this.attributes.apply attributes@{
                this@apply.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                this.dimAmount = dimAmount()
            }
        }
        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onBlockBackPressed()
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }
        return layoutInflater
    }

    override fun isCancel(): Boolean {
        return true
    }

    abstract fun onBlockBackPressed()

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
}