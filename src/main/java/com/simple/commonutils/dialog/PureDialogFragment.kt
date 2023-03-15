@file:Suppress("SameParameterValue")

package com.simple.commonutils.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater

abstract class PureDialogFragment : BaseStyleDialogFragment() {

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater = super.onGetLayoutInflater(savedInstanceState)
        dialog?.apply {
            simpleDialogConfig(this)
        }
        return layoutInflater
    }

    abstract fun simpleDialogConfig(dialog: Dialog): SimpleDialogConfig
}
