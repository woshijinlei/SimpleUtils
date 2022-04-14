package com.simple.commonutils.transition

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.transition.Slide
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.Window
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

object ActivityTransitionHelper {

    private const val enterAnimationDuration = 350L
    private const val exitAnimationDuration = 350L

    /**
     * startActivity
     * A  exitTransition  B enterTransition
     * finishActivity
     * B  returnTransition  A  reenterTransition
     *
     * If you have set an enter transition for the second activity, the transition is also
     * activated when the activity starts. To disable transitions when you start another
     * activity, provide a null options bundle
     *
     * To get the full effect of a transition, you must enable window content transitions on
     * both the calling and called activities. Otherwise, the calling activity will start
     * the exit transition, but then you'll see a window transition (like scale or fade)
     */
    fun initFirstWindowTransition(window: Window) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.exitTransition = com.google.android.material.transition.platform.Hold()
        window.reenterTransition = com.google.android.material.transition.platform.Hold()
        window.allowReturnTransitionOverlap = true//(让其他returnTransition完全执行完)
        window.allowEnterTransitionOverlap = true//(让自己enterTransition完全执行完毕）
    }

    fun initSecondWindowTransition(window: Window) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.returnTransition = Slide().apply {
            this.interpolator = LinearOutSlowInInterpolator()
            this.excludeTarget(android.R.id.statusBarBackground, true)
            this.excludeTarget(android.R.id.navigationBarBackground, true)
            this.duration = enterAnimationDuration
        }
        window.enterTransition = Slide().apply {
            this.interpolator = LinearOutSlowInInterpolator()
            this.slideEdge = Gravity.BOTTOM
            this.excludeTarget(android.R.id.statusBarBackground, true)
            this.excludeTarget(android.R.id.navigationBarBackground, true)
            this.duration = exitAnimationDuration
        }
        window.allowReturnTransitionOverlap = false//其他finish   (让其他returnTransition完全执行完)
        window.allowEnterTransitionOverlap = false//其他startActivity  （让自己enterTransition不覆盖）
    }

    fun initShareName(view: View, shareElement: String) {
        ViewCompat.setTransitionName(view, shareElement)
    }

    fun startActivityBasicScene(activity: Activity, intent: Intent) {
        activity.startActivity(
            intent,
            ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle()
        )
    }

    //when you finish the second activity, call the Activity.finishAfterTransition()
    fun startActivityWithElement(
        activity: Activity,
        intent: Intent,
        vararg viewPairs: Pair<View, String>
    ) {
        activity.startActivity(
            intent,
            ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *viewPairs).toBundle()
        )
    }

    fun startActivityWithCustomAnimation(
        activity: Activity,
        intent: Intent,
        enter: Int = android.R.anim.slide_in_left,
        exit: Int = android.R.anim.slide_out_right,
    ) {
        val options = ActivityOptionsCompat.makeCustomAnimation(
            activity, enter, exit
        )
        activity.startActivity(
            intent,
            options.toBundle()
        )
    }

    fun startActivityWithClipRevealAnimation(
        activity: Activity,
        intent: Intent,
        source: View = activity.window.decorView,
        centerSize: Size = Size(0, 0)
    ) {
        val width = source.width
        val height = source.height
        val options = ActivityOptionsCompat.makeClipRevealAnimation(
            source,
            width / 2 - centerSize.width / 2,
            height / 2 - centerSize.height / 2,
            centerSize.width, centerSize.height
        )
        activity.startActivity(intent, options.toBundle())
    }

    fun startActivityWithScaleUpAnimation(
        activity: Activity,
        intent: Intent,
        view: View,
        startSize: Size = Size(0, 0)
    ) {
        val options = ActivityOptionsCompat.makeScaleUpAnimation(
            view,
            view.width / 2 - startSize.width / 2,
            view.height / 2 - startSize.height / 2,
            startSize.width,
            startSize.height
        )
        activity.startActivity(
            intent,
            options.toBundle()
        )
    }

    fun startActivityWithThumbnailScaleUpAnimation(
        activity: Activity,
        intent: Intent,
        view: View,
        bitmap: Bitmap
    ) {
        val options = ActivityOptionsCompat.makeThumbnailScaleUpAnimation(
            view, bitmap, 0, 0
        )
        activity.startActivity(
            intent,
            options.toBundle()
        )
    }
}