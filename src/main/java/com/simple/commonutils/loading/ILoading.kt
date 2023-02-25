package com.simple.commonutils.loading

interface ILoading {
    fun showLoading(backPressedCancelable: Boolean)
    fun hideLoading()
    fun setCallback(hideCallback: (isManual:Boolean) -> Unit)
}