package com.simple.commonutils.common

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

/**
 * android:imeOptions="actionGo"
 */
class OnEditorActionGoListener(val go: () -> Unit) : TextView.OnEditorActionListener {
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            val imm =
                v?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
            go.invoke()
            return true
        }
        return false
    }
}

/**
 * android:imeOptions="actionDone"
 */
class OnEditorActionDoneListener(val go: () -> Unit) : TextView.OnEditorActionListener {
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            val imm =
                v?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
            go.invoke()
            return true
        }
        return false
    }
}