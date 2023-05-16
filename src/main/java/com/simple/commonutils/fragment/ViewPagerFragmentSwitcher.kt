package com.simple.commonutils.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewpager.widget.ViewPager

class ViewPagerFragmentSwitcher private constructor(
    private val viewPager: ViewPager,
    private val fm: FragmentManager,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val fragments: MutableList<Class<out Fragment>>,
    private val limit: Int,
    private val onPageChangeCallback: ViewPager.OnPageChangeListener?,
) : IFragmentSwitcher {
    private val fragmentIndexViewModel by lazy {
        FragmentIndexViewModel.getInstance(viewModelStoreOwner)
    }

    override val currentItem: Int
        get() = fragmentIndexViewModel.getFragmentIndex()

    override fun init() {
        viewPager.apply {
            this.adapter = MyFragmentStateViewPagerAdapter(
                fragments,
                fm
            )
            this.offscreenPageLimit = limit
            this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    onPageChangeCallback?.onPageScrolled(
                        position,
                        positionOffset,
                        positionOffsetPixels
                    )
                }

                override fun onPageSelected(position: Int) {// 初始化不调用
                    fragmentIndexViewModel.saveFragmentIndex(position)
                    onPageChangeCallback?.onPageSelected(position)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    onPageChangeCallback?.onPageScrollStateChanged(state)
                }
            })
            if (this@ViewPagerFragmentSwitcher.currentItem == -1) {
                fragmentIndexViewModel.saveFragmentIndex(0)
            }
        }
    }

    override fun switch(position: Int, smoothScroll: Boolean) {
        viewPager.setCurrentItem(position, smoothScroll)
    }

    override fun switch(fragment: Class<Fragment>, smoothScroll: Boolean) {
        val index = fragments.indexOfFirst {
            it.name == fragment.name// todo is ok?
        }
        viewPager.setCurrentItem(index, smoothScroll)
    }

    private class MyFragmentStateViewPagerAdapter(
        private val fragments: MutableList<Class<out Fragment>>,
        fragmentManager: FragmentManager,
    ) : FragmentPagerAdapter(fragmentManager) {
        override fun getCount(): Int = fragments.size

        override fun getItem(position: Int): Fragment {
            return fragments[position].newInstance()
        }
    }

    class Builder {
        private var limit = 1
        private var onPageChangeCallback: ViewPager.OnPageChangeListener? = null

        fun setOffscreenPageLimit(limit: Int) = apply {
            this.limit = limit
        }

        fun setOnPageChangeCallback(onPageChangeCallback: ViewPager.OnPageChangeListener) = apply {
            this.onPageChangeCallback = onPageChangeCallback
        }

        fun build(
            viewPager: ViewPager,
            fragmentManager: FragmentManager,
            viewModelStoreOwner: ViewModelStoreOwner,
            fragments: MutableList<Class<out Fragment>>
        ) = ViewPagerFragmentSwitcher(
            viewPager = viewPager,
            fm = fragmentManager,
            viewModelStoreOwner = viewModelStoreOwner,
            fragments = fragments,
            limit = limit,
            onPageChangeCallback = onPageChangeCallback
        )
    }
}