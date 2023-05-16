package com.simple.commonutils.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class ViewPager2FragmentSwitcher private constructor(
    private val viewPager2: ViewPager2,
    private val fm: FragmentManager,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val lifecycle: Lifecycle,
    private val fragments: MutableList<Class<out Fragment>>,
    private val isUserInputEnable: Boolean,
    private val limit: Int,
    private val onPageChangeCallback: ViewPager2.OnPageChangeCallback?,
) : IFragmentSwitcher {
    private val fragmentIndexViewModel by lazy {
        FragmentIndexViewModel.getInstance(viewModelStoreOwner)
    }

    override val currentItem: Int
        get() = fragmentIndexViewModel.getFragmentIndex()

    override fun init() {
        viewPager2.apply {
            this.adapter = MyFragmentStateViewPager2Adapter(
                fragments,
                fm,
                lifecycle
            )
            this.isUserInputEnabled = isUserInputEnable
            /**
             * 1.实际上是保持offscreenPageLimit+2的间隔，然后item才执行相应fragment的销毁系列方法:
             *   onSaveInstanceState
             *   onStop
             *   onDestroyView
             *   onDestroy
             *   onDetach
             * 2.不设置--lazing load
             *   相邻的fragment预加载生命周期不会执行
             *   滑动过程中执行到onStart，滑动结束后执行onResume
             * 3.这个设置会造成Fragment声明周期的提前执行
             */
            this.offscreenPageLimit = limit
            this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {// 初始化会调用
                    fragmentIndexViewModel.saveFragmentIndex(position)
                    onPageChangeCallback?.onPageSelected(position)
                }

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

                override fun onPageScrollStateChanged(state: Int) {
                    onPageChangeCallback?.onPageScrollStateChanged(state)
                }
            })
            if (this@ViewPager2FragmentSwitcher.currentItem == -1) {
                fragmentIndexViewModel.saveFragmentIndex(0)
            }
        }
    }

    override fun switch(position: Int, smoothScroll: Boolean) {
        viewPager2.setCurrentItem(position, smoothScroll)
    }

    override fun switch(fragment: Class<Fragment>, smoothScroll: Boolean) {
        val index = fragments.indexOfFirst {
            it.name == fragment.name// todo is ok?
        }
        viewPager2.setCurrentItem(index, smoothScroll)
    }

    private class MyFragmentStateViewPager2Adapter(
        private val fragments: MutableList<Class<out Fragment>>,
        fragmentManager: FragmentManager,
        lifecycleOwner: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycleOwner) {
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position].newInstance()
        }
    }

    class Builder {
        private var enable: Boolean = false
        private var limit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        private var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null

        fun setUserInputEnabled(enable: Boolean) = apply {
            this.enable = enable
        }

        fun setOffscreenPageLimit(limit: Int) = apply {
            this.limit = limit
        }

        fun setOnPageChangeCallback(onPageChangeCallback: ViewPager2.OnPageChangeCallback) = apply {
            this.onPageChangeCallback = onPageChangeCallback
        }

        fun build(
            viewPager2: ViewPager2,
            fragmentManager: FragmentManager,
            viewModelStoreOwner: ViewModelStoreOwner,
            lifecycle: Lifecycle,
            fragments: MutableList<Class<out Fragment>>
        ) = ViewPager2FragmentSwitcher(
            viewPager2 = viewPager2,
            fm = fragmentManager,
            viewModelStoreOwner = viewModelStoreOwner,
            lifecycle = lifecycle,
            fragments = fragments,
            isUserInputEnable = enable,
            limit = limit,
            onPageChangeCallback = onPageChangeCallback
        )
    }
}