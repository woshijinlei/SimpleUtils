@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.core.view.setPadding

abstract class BaseDialogFragment : StyleBaseDialogFragment() {

    private val handler = Handler(Looper.getMainLooper())

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
            this.setWindowAnimations(customWindowAnimations())
        }
        return layoutInflater
    }

    override fun onResume() {
        super.onResume()
        handler.post { setWindowAnimation(customWindowAnimations()) }
    }

    override fun onPause() {
        clearWindowAnimation()
        super.onPause()
    }

    private fun clearWindowAnimation() {
        dialog?.window?.setWindowAnimations(0)
    }

    private fun setWindowAnimation(windowAnimationStyle: Int) {
        dialog?.window?.setWindowAnimations(windowAnimationStyle)
    }

    open fun dimAmount(): Float = 0f
    open fun customWindowAnimations(): Int = android.R.style.Animation_Dialog
}