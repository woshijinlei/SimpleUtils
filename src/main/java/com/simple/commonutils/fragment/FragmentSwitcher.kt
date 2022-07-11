package com.simple.commonutils.fragment

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*

class FragmentIndexViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    fun saveFragmentIndex(index: Int) {
        savedStateHandle.set(KEY_FRAGMENT_INDEX, index)
    }

    fun getFragmentIndex() = savedStateHandle.get(KEY_FRAGMENT_INDEX) ?: -1

    companion object {
        const val KEY_FRAGMENT_INDEX = "FragmentIndexViewModel:fragmentIndex"

        fun getInstance(viewModelStoreOwner: ViewModelStoreOwner) =
            ViewModelProvider(viewModelStoreOwner).get(FragmentIndexViewModel::class.java)
    }
}

/**
 * 杀死进程的重启，ViewModel实例会改变，但是保存的bundle值依然是系统传递过来的
 *
 * @param fragments 组件重建时，fragment实例会创建多个(主要activity的fragment数组和自动创建的fragment)，
 * 尽量不要采用变量的直接初始化方式，同时会根据supportFragmentManager管理的fragment替换掉fragments中对应的元素，
 * fragments只有一个元素时，也能够支持
 */
class FragmentSwitcher(
    viewModelStoreOwner: ViewModelStoreOwner,
    private val supportFragmentManager: FragmentManager,
    private val fragments: MutableList<androidx.fragment.app.Fragment>
) {

    private val fragmentIndexViewModel by lazy {
        FragmentIndexViewModel.getInstance(viewModelStoreOwner)
    }

    fun currentFragmentIndex(): Int {
        return fragmentIndexViewModel.getFragmentIndex()
    }

    fun initFragments(fragmentContainer: Int, position: Int = 0) {
        val currentIndex = fragmentIndexViewModel.getFragmentIndex()
        supportFragmentManager.let { fragmentManager ->
            for (i in fragments.size - 1 downTo 0) {
                fragmentManager.findFragmentByTag("$i")?.let {
                    fragments.removeAt(i)
                    fragments.add(i, it)
                }
            }
        }
        val p = if (currentIndex == -1) {
            position
        } else {
            currentIndex
        }
        switchFragmentByShow(p, fragmentContainer)
    }

    fun switchFragmentByShow(targetPosition: Int, fragmentContainer: Int) {
        val currentIndex = fragmentIndexViewModel.getFragmentIndex()
        if (fragments.size == 1) {
            val ft = supportFragmentManager.beginTransaction()
            val targetFragment = fragments[targetPosition]
            if (!targetFragment.isAdded) {
                ft.add(fragmentContainer, targetFragment, "$targetPosition")
            } else {
                ft.show(targetFragment)
            }
            ft.commitAllowingStateLoss()
            fragmentIndexViewModel.saveFragmentIndex(targetPosition)
        } else {
            if (currentIndex == targetPosition) return
            val ft = supportFragmentManager.beginTransaction()
            val targetFragment = fragments[targetPosition]
            if (currentIndex == -1) {
                ft.add(fragmentContainer, targetFragment, "$targetPosition")
            } else {
                val currentFragment = fragments[currentIndex]
                if (!targetFragment.isAdded) {
                    if (currentFragment.isAdded) {
                        ft.hide(currentFragment)
                            .add(fragmentContainer, targetFragment, "$targetPosition")
                    } else {
                        ft.add(fragmentContainer, targetFragment, "$targetPosition")
                    }
                } else {
                    if (currentFragment.isAdded) {
                        ft.hide(currentFragment)
                    }
                    ft.show(targetFragment)
                }
            }
            ft.commitAllowingStateLoss()
            fragmentIndexViewModel.saveFragmentIndex(targetPosition)
        }

    }

    fun switchFragmentByReplace(targetPosition: Int, fragmentContainer: Int) {
        val currentIndex = fragmentIndexViewModel.getFragmentIndex()
        if (currentIndex == targetPosition) return
        val targetFragment = fragments[targetPosition]
        supportFragmentManager.beginTransaction().let {
            it.replace(fragmentContainer, targetFragment)
            it.commitAllowingStateLoss()
            fragmentIndexViewModel.saveFragmentIndex(targetPosition)
        }
    }
}