package com.simple.commonutils.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelStoreOwner
import com.simple.commonutils.R

/**
 * 杀死进程的重启，ViewModel实例会改变，但是保存的bundle值依然是系统传递过来的
 *
 * @param fragments 组件重建时，会根据supportFragmentManager管理的fragment替换掉fragments中对应的元素，
 * fragments只有一个元素时，也能够支持
 */
class ViewGroupFragmentSwitcher(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val fragmentContainer: Int,
    private val supportFragmentManager: FragmentManager,
    private val fragments: MutableList<Fragment>
) : IFragmentSwitcher {
    private val fragmentIndexViewModel by lazy {
        FragmentIndexViewModel.getInstance(viewModelStoreOwner)
    }

    override val currentItem: Int
        get() = fragmentIndexViewModel.getFragmentIndex()

    override fun init() {
        supportFragmentManager.let { fragmentManager ->
            for (i in fragments.size - 1 downTo 0) {
                fragmentManager.findFragmentByTag("$i")?.let {
                    fragments.removeAt(i)
                    fragments.add(i, it)
                }
            }
        }
        val currentIndex = fragmentIndexViewModel.getFragmentIndex()
        val show = if (currentIndex == -1) 0 else currentIndex
        switch(show, false)
    }

    override fun switch(position: Int, smoothScroll: Boolean) {
        if (fragments.size == 1) {
            val ft = supportFragmentManager.beginTransaction()
            val targetFragment = fragments[position]
            if (!targetFragment.isAdded) {
                ft.add(fragmentContainer, targetFragment, "$position")
            } else {
                ft.show(targetFragment)
            }
            ft.commitAllowingStateLoss()
            fragmentIndexViewModel.saveFragmentIndex(position)
        } else {
            val currentIndex = fragmentIndexViewModel.getFragmentIndex()
            if (currentIndex == position) return
            val ft = supportFragmentManager.beginTransaction()
            val targetFragment = fragments[position]
            if (currentIndex == -1) {
                if (smoothScroll) {
                    ft.setCustomAnimations(
                        R.anim.anim_activity_right_open_enter, 0,
                    )
                }
                ft.add(fragmentContainer, targetFragment, "$position")
            } else {
                val currentFragment = fragments[currentIndex]
                if (!targetFragment.isAdded) {
                    if (currentFragment.isAdded) {
                        if (smoothScroll) {
                            ft.setCustomAnimations(
                                R.anim.anim_activity_right_open_enter,
                                R.anim.anim_activity_left_open_exit
                            )
                        }
                        ft.hide(currentFragment)
                        ft.add(fragmentContainer, targetFragment, "$position")
                    } else {
                        if (smoothScroll) {
                            ft.setCustomAnimations(0, 0)
                        }
                        ft.add(fragmentContainer, targetFragment, "$position")
                    }
                } else {
                    if (currentIndex > position) {
                        if (smoothScroll) {
                            ft.setCustomAnimations(
                                R.anim.anim_activity_left_close_enter,
                                R.anim.anim_activity_right_close_exit
                            )
                        }
                    } else {
                        if (smoothScroll) {
                            ft.setCustomAnimations(
                                R.anim.anim_activity_right_open_enter,
                                R.anim.anim_activity_left_open_exit
                            )
                        }
                    }
                    if (currentFragment.isAdded) {
                        ft.hide(currentFragment)
                    }
                    ft.show(targetFragment)
                }
            }
            ft.commitAllowingStateLoss()
            fragmentIndexViewModel.saveFragmentIndex(position)
        }
    }

    override fun switch(fragment: Class<Fragment>, smoothScroll: Boolean) {
        val i = fragments.indexOfFirst {
            fragment.name == it::class.java.name// todo is ok?
        }
        switch(i, smoothScroll)
    }
}