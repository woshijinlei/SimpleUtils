package com.simple.commonutils.common

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CoroutineClickListener(
    private val onClick: suspend () -> Unit
) : View.OnClickListener {

    override fun onClick(v: View) {
        v.findViewTreeLifecycleOwner()!!.lifecycleScope.launch {
            onClick.invoke()
        }
    }
}