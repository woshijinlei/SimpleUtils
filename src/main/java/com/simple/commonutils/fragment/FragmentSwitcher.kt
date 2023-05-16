package com.simple.commonutils.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

interface IFragmentSwitcher {
    /**
     * no page added, will return -1
     */
    val currentItem: Int

    /**
     * activity的onCreate方法执行之前，Fragment已经完成实例初始化，onAttach和onCreate均执行了，
     * 并且supportFragmentManager.fragments可以查询到这个实例Fragment
     */
    fun init()

    fun switch(position: Int, smoothScroll: Boolean = false)

    fun switch(fragment: Class<Fragment>, smoothScroll: Boolean = false)
}

class FragmentIndexViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    fun saveFragmentIndex(index: Int) {
        savedStateHandle[KEY_FRAGMENT_INDEX] = index
    }

    fun getFragmentIndex() = savedStateHandle[KEY_FRAGMENT_INDEX] ?: -1

    companion object {
        const val KEY_FRAGMENT_INDEX = "FragmentIndexViewModel:fragmentIndex"

        fun getInstance(viewModelStoreOwner: ViewModelStoreOwner) =
            ViewModelProvider(viewModelStoreOwner)[FragmentIndexViewModel::class.java]
    }
}