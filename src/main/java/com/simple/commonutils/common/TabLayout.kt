package com.simple.commonutils.common

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

fun TabLayout.setupViewPager2(viewPager2: ViewPager2, titleProvider: (position: Int) -> String) {
    TabLayoutMediator(this, viewPager2) { tab, position ->
        tab.text = titleProvider.invoke(position)
    }.apply {
        this.attach()
    }
}

/**
 * 需要设置app:tabMinWidth="0dp"，不然会有默认的最小宽度
 */
fun TabLayout.tabItemPaddingHorizontal(paddingStart: Int, paddingEnd: Int) {
    clearClip()
    repeat(this.tabCount) {
        // (view)TabView(AppCompatImageView,AppCompatTextView)->SlidingTabIndicator->TabLayout
        this.getTabAt(it)?.apply {
            view.updatePadding(left = paddingStart, right = paddingEnd)
        }
    }
}

fun TabLayout.tabLayoutPadding(paddingStart: Int, paddingEnd: Int) {
    this[0].apply {
        setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
    }
}


fun TabLayout.registerAnimatorListener(listenerAdapter: AnimatorListenerAdapter) {
//    val method = javaClass.getDeclaredMethod("ensureScrollAnimator")
//    method.isAccessible = true
//    method.invoke(this)
    val filed = this[0].javaClass.getDeclaredField("indicatorAnimator")
    filed.isAccessible = true
    val anim = filed.get(this[0]) as ValueAnimator
    anim.addListener(listenerAdapter)
}

private fun TabLayout.clearClip() {
    repeat(this.tabCount) {
        this.getTabAt(it)?.apply {
            (view.parent as ViewGroup).apply {
                clipChildren = false
                clipToPadding = false
            }
            view.clipChildren = false
            view.clipToPadding = false
        }
    }
}